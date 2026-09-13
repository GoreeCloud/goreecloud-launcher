package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WorkspaceAtomicHomeGroupMoveResult {
    data object Reserved : WorkspaceAtomicHomeGroupMoveResult
    data object Unavailable : WorkspaceAtomicHomeGroupMoveResult
    data object PageNotFound : WorkspaceAtomicHomeGroupMoveResult
    data object PrimaryPageProtected : WorkspaceAtomicHomeGroupMoveResult
    data object ItemNotFound : WorkspaceAtomicHomeGroupMoveResult
    data object InvalidWorkspace : WorkspaceAtomicHomeGroupMoveResult
    data object StoredWorkspaceChanged : WorkspaceAtomicHomeGroupMoveResult
    data class Moved(
        val count: Int,
        val targetPageId: String,
    ) : WorkspaceAtomicHomeGroupMoveResult
    data class Failed(val failureType: String) : WorkspaceAtomicHomeGroupMoveResult
}

/**
 * Plans a deterministic multi-app HOME move from one secondary page to another and commits every
 * resulting placement through one snapshot-guarded Room transaction.
 *
 * The preflight read has no write authority. The DAO transaction rechecks the complete HOME page
 * and item snapshot immediately before the bulk upsert. If any page/item changes between planning
 * and commit, no selected item is written.
 */
class WorkspaceAtomicHomeGroupMover(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    suspend fun moveAppsToPage(
        sourcePageId: String,
        appKeys: List<String>,
        targetPageId: String,
    ): WorkspaceAtomicHomeGroupMoveResult {
        if (
            sourcePageId.isBlank() ||
            targetPageId.isBlank() ||
            appKeys.isEmpty() ||
            appKeys.any { it.isBlank() } ||
            appKeys.size != appKeys.distinct().size ||
            sourcePageId == targetPageId
        ) {
            return WorkspaceAtomicHomeGroupMoveResult.InvalidWorkspace
        }
        if (
            sourcePageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID ||
            targetPageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        ) {
            return WorkspaceAtomicHomeGroupMoveResult.PrimaryPageProtected
        }

        val state = authorityRepository.state.first()
        if (!state.initialized || state.authority != WorkspaceAuthority.ROOM) {
            return WorkspaceAtomicHomeGroupMoveResult.Reserved
        }
        val dao = workspaceDaoOrNull()
            ?: return WorkspaceAtomicHomeGroupMoveResult.Unavailable

        return try {
            val pages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (
                pages.isEmpty() ||
                pages.map { it.rank } != pages.indices.toList() ||
                pages.firstOrNull()?.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID
            ) {
                return WorkspaceAtomicHomeGroupMoveResult.InvalidWorkspace
            }
            if (pages.none { it.pageId == sourcePageId } || pages.none { it.pageId == targetPageId }) {
                return WorkspaceAtomicHomeGroupMoveResult.PageNotFound
            }

            val items = dao.readItems(pages.map { it.pageId })
            val spatialItems = items.filterNot { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
            if (spatialItems.any { it.cellX == null || it.cellY == null }) {
                return WorkspaceAtomicHomeGroupMoveResult.InvalidWorkspace
            }

            val selectedByKey = spatialItems
                .filter { it.pageId == sourcePageId && it.itemType == WorkspaceItemType.APP }
                .groupBy { it.appKey }
            val sources = buildList {
                for (appKey in appKeys) {
                    val matches = selectedByKey[appKey].orEmpty()
                    if (matches.isEmpty()) return WorkspaceAtomicHomeGroupMoveResult.ItemNotFound
                    if (matches.size != 1) return WorkspaceAtomicHomeGroupMoveResult.InvalidWorkspace
                    add(matches.single())
                }
            }

            val grid = deriveGrid(spatialItems, sources)
            val selectedIds = sources.map { it.itemId }.toSet()
            val occupied = spatialItems
                .filter { it.pageId == targetPageId && it.itemId !in selectedIds }
                .map(::toPlacement)
                .toMutableList()
            val targetRankStart = items
                .asSequence()
                .filter { it.pageId == targetPageId && it.itemId !in selectedIds }
                .maxOfOrNull { it.rank }
                ?.let {
                    if (it == Int.MAX_VALUE) {
                        return WorkspaceAtomicHomeGroupMoveResult.Failed("TargetRankOverflow")
                    }
                    it + 1
                } ?: 0

            val updatedItems = sources.mapIndexed { index, source ->
                val placement = firstAvailablePlacement(grid, occupied, source)
                    ?: return WorkspaceAtomicHomeGroupMoveResult.InvalidWorkspace
                occupied += placement
                source.copy(
                    pageId = targetPageId,
                    rank = targetRankStart + index,
                    cellX = placement.cellX,
                    cellY = placement.cellY,
                    spanX = placement.spanX,
                    spanY = placement.spanY,
                )
            }

            if (!dao.replaceItemPlacementsIfSnapshotMatches(
                    containerType = WorkspaceContainerType.HOME,
                    expectedPages = pages,
                    expectedItems = items,
                    updatedItems = updatedItems,
                )
            ) {
                return WorkspaceAtomicHomeGroupMoveResult.StoredWorkspaceChanged
            }

            WorkspaceAtomicHomeGroupMoveResult.Moved(
                count = updatedItems.size,
                targetPageId = targetPageId,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceAtomicHomeGroupMoveResult.Failed(exception::class.java.simpleName)
        }
    }

    private fun deriveGrid(
        items: List<WorkspaceItemEntity>,
        sources: List<WorkspaceItemEntity>,
    ): WorkspaceGridPlacement.Grid {
        val existingColumns = items.maxOfOrNull { checkNotNull(it.cellX) + it.spanX } ?: 0
        val existingRows = items.maxOfOrNull { checkNotNull(it.cellY) + it.spanY } ?: 0
        val maxSourceSpanX = sources.maxOfOrNull { it.spanX } ?: 1
        val additionalRows = sources.sumOf { it.spanY }
        return WorkspaceGridPlacement.Grid(
            columns = maxOf(MIN_HOME_COLUMNS, existingColumns, maxSourceSpanX),
            rows = maxOf(1, existingRows + additionalRows),
        )
    }

    private fun firstAvailablePlacement(
        grid: WorkspaceGridPlacement.Grid,
        occupied: List<WorkspaceGridPlacement.Placement>,
        source: WorkspaceItemEntity,
    ): WorkspaceGridPlacement.Placement? {
        for (cellY in 0..grid.rows - source.spanY) {
            for (cellX in 0..grid.columns - source.spanX) {
                val candidate = WorkspaceGridPlacement.Placement(
                    itemId = source.itemId,
                    cellX = cellX,
                    cellY = cellY,
                    spanX = source.spanX,
                    spanY = source.spanY,
                )
                if (
                    WorkspaceGridPlacement.validate(grid, occupied + candidate) ==
                    WorkspaceGridPlacement.Validation.Valid
                ) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun toPlacement(item: WorkspaceItemEntity) = WorkspaceGridPlacement.Placement(
        itemId = item.itemId,
        cellX = checkNotNull(item.cellX),
        cellY = checkNotNull(item.cellY),
        spanX = item.spanX,
        spanY = item.spanY,
    )

    private fun workspaceDaoOrNull(): WorkspaceDao? = try {
        workspaceDaoProvider()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val MIN_HOME_COLUMNS = 4
    }
}
