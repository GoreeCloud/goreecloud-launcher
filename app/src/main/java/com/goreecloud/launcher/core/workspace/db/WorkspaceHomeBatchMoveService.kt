package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WorkspaceHomeBatchMoveResult {
    data object Reserved : WorkspaceHomeBatchMoveResult
    data object Unavailable : WorkspaceHomeBatchMoveResult
    data object InvalidRequest : WorkspaceHomeBatchMoveResult
    data object PrimaryPageProtected : WorkspaceHomeBatchMoveResult
    data object PageNotFound : WorkspaceHomeBatchMoveResult
    data object ItemNotFound : WorkspaceHomeBatchMoveResult
    data object InvalidWorkspace : WorkspaceHomeBatchMoveResult
    data object StoredWorkspaceChanged : WorkspaceHomeBatchMoveResult
    data class Applied(val commit: WorkspaceHomeBatchMoveCommit) : WorkspaceHomeBatchMoveResult
    data class Undone(val commit: WorkspaceHomeBatchMoveCommit) : WorkspaceHomeBatchMoveResult
    data class Failed(val failureType: String) : WorkspaceHomeBatchMoveResult
}

/**
 * Plans and applies deterministic all-or-nothing moves of multiple applications between secondary
 * HOME pages. Room remains authoritative after cutover. The service validates the complete HOME
 * snapshot, computes every destination placement first, then asks [WorkspaceHomeBatchMoveDao] to
 * apply all changed rows in one transaction only if that exact snapshot is still current.
 */
class WorkspaceHomeBatchMoveService(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
    private val batchDaoProvider: () -> WorkspaceHomeBatchMoveDao?,
) {
    suspend fun moveAppsToPage(
        sourcePageId: String,
        appKeys: List<String>,
        targetPageId: String,
    ): WorkspaceHomeBatchMoveResult {
        if (!isRoomAuthoritative()) return WorkspaceHomeBatchMoveResult.Reserved
        if (
            sourcePageId.isBlank() ||
            targetPageId.isBlank() ||
            sourcePageId == targetPageId ||
            appKeys.isEmpty() ||
            appKeys.any { it.isBlank() } ||
            appKeys.size != appKeys.distinct().size
        ) {
            return WorkspaceHomeBatchMoveResult.InvalidRequest
        }
        if (
            sourcePageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID ||
            targetPageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        ) {
            return WorkspaceHomeBatchMoveResult.PrimaryPageProtected
        }

        val workspaceDao = workspaceDaoOrNull() ?: return WorkspaceHomeBatchMoveResult.Unavailable
        val batchDao = batchDaoOrNull() ?: return WorkspaceHomeBatchMoveResult.Unavailable

        return try {
            if (WorkspaceCanonicalRoomPlacementReader.read(workspaceDao) == null) {
                return WorkspaceHomeBatchMoveResult.InvalidWorkspace
            }

            val storedPages = workspaceDao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (
                storedPages.isEmpty() ||
                storedPages.map { it.rank } != storedPages.indices.toList() ||
                storedPages.firstOrNull()?.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID
            ) {
                return WorkspaceHomeBatchMoveResult.InvalidWorkspace
            }
            if (storedPages.none { it.pageId == sourcePageId } || storedPages.none { it.pageId == targetPageId }) {
                return WorkspaceHomeBatchMoveResult.PageNotFound
            }

            val pageIds = storedPages.map { it.pageId }
            val storedItems = workspaceDao.readItems(pageIds).canonicalItems()
            val spatialItems = storedItems.filterNot {
                it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
            }
            if (spatialItems.any {
                    it.cellX == null ||
                        it.cellY == null ||
                        checkNotNull(it.cellX) < 0 ||
                        checkNotNull(it.cellY) < 0 ||
                        it.spanX <= 0 ||
                        it.spanY <= 0
                }
            ) {
                return WorkspaceHomeBatchMoveResult.InvalidWorkspace
            }

            val requestedKeys = appKeys.toSet()
            val selected = spatialItems
                .filter {
                    it.pageId == sourcePageId &&
                        it.itemType == WorkspaceItemType.APP &&
                        it.appKey in requestedKeys
                }
                .sortedBy { it.rank }
            if (
                selected.size != appKeys.size ||
                selected.mapNotNull { it.appKey }.toSet() != requestedKeys ||
                selected.mapNotNull { it.appKey }.size != selected.size
            ) {
                return WorkspaceHomeBatchMoveResult.ItemNotFound
            }

            val selectedIds = selected.map { it.itemId }.toSet()
            val targetExisting = spatialItems.filter {
                it.pageId == targetPageId && it.itemId !in selectedIds
            }
            val grid = deriveGrid(spatialItems, selected)
                ?: return WorkspaceHomeBatchMoveResult.InvalidWorkspace
            val occupied = targetExisting.map(::toPlacement).toMutableList()

            val planned = mutableListOf<WorkspaceItemEntity>()
            var nextRank = targetExisting.maxOfOrNull { it.rank }?.let { maxRank ->
                if (maxRank == Int.MAX_VALUE) return WorkspaceHomeBatchMoveResult.InvalidWorkspace
                maxRank + 1
            } ?: 0

            for (item in selected) {
                val placement = firstAvailablePlacement(grid, occupied, item)
                    ?: return WorkspaceHomeBatchMoveResult.InvalidWorkspace
                if (nextRank < 0) return WorkspaceHomeBatchMoveResult.InvalidWorkspace
                planned += item.copy(
                    pageId = targetPageId,
                    rank = nextRank,
                    cellX = placement.cellX,
                    cellY = placement.cellY,
                    spanX = placement.spanX,
                    spanY = placement.spanY,
                )
                occupied += placement
                if (planned.size < selected.size) {
                    if (nextRank == Int.MAX_VALUE) return WorkspaceHomeBatchMoveResult.InvalidWorkspace
                    nextRank += 1
                }
            }

            val plannedById = planned.associateBy { it.itemId }
            val appliedItems = storedItems.map { item ->
                plannedById[item.itemId] ?: item
            }.canonicalItems()
            val commit = WorkspaceHomeBatchMoveCommit(
                sourcePageId = sourcePageId,
                targetPageId = targetPageId,
                movedItemIds = selected.map { it.itemId },
                previousPages = storedPages,
                previousItems = storedItems,
                appliedItems = appliedItems,
            )

            if (!batchDao.applyIfSnapshotMatches(commit)) {
                return WorkspaceHomeBatchMoveResult.StoredWorkspaceChanged
            }
            WorkspaceHomeBatchMoveResult.Applied(commit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceHomeBatchMoveResult.Failed(exception::class.java.simpleName)
        }
    }

    /**
     * One-level exact-state rollback primitive for a previously applied batch. It succeeds only if
     * no HOME page or item state changed after the batch was applied.
     */
    suspend fun undo(commit: WorkspaceHomeBatchMoveCommit): WorkspaceHomeBatchMoveResult {
        if (!isRoomAuthoritative()) return WorkspaceHomeBatchMoveResult.Reserved
        val workspaceDao = workspaceDaoOrNull() ?: return WorkspaceHomeBatchMoveResult.Unavailable
        val batchDao = batchDaoOrNull() ?: return WorkspaceHomeBatchMoveResult.Unavailable

        return try {
            if (WorkspaceCanonicalRoomPlacementReader.read(workspaceDao) == null) {
                return WorkspaceHomeBatchMoveResult.InvalidWorkspace
            }
            if (!batchDao.rollbackIfSnapshotMatches(commit)) {
                return WorkspaceHomeBatchMoveResult.StoredWorkspaceChanged
            }
            WorkspaceHomeBatchMoveResult.Undone(commit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceHomeBatchMoveResult.Failed(exception::class.java.simpleName)
        }
    }

    private suspend fun isRoomAuthoritative(): Boolean {
        val state = authorityRepository.state.first()
        return state.initialized && state.authority == WorkspaceAuthority.ROOM
    }

    private fun deriveGrid(
        items: List<WorkspaceItemEntity>,
        selected: List<WorkspaceItemEntity>,
    ): WorkspaceGridPlacement.Grid? {
        val existingColumns = items.maxOfOrNull {
            val cellX = it.cellX ?: return@maxOfOrNull 0
            cellX.toLong() + it.spanX.toLong()
        } ?: 0L
        val existingRows = items.maxOfOrNull {
            val cellY = it.cellY ?: return@maxOfOrNull 0
            cellY.toLong() + it.spanY.toLong()
        } ?: 0L
        val selectedSpanRows = selected.sumOf { it.spanY.toLong() }
        val widestSelected = selected.maxOfOrNull { it.spanX.toLong() } ?: 1L
        val columns = maxOf(MIN_HOME_COLUMNS.toLong(), existingColumns, widestSelected)
        val rows = maxOf(1L, existingRows + selectedSpanRows)
        if (columns > Int.MAX_VALUE || rows > Int.MAX_VALUE) return null
        return WorkspaceGridPlacement.Grid(columns = columns.toInt(), rows = rows.toInt())
    }

    private fun firstAvailablePlacement(
        grid: WorkspaceGridPlacement.Grid,
        occupied: List<WorkspaceGridPlacement.Placement>,
        item: WorkspaceItemEntity,
    ): WorkspaceGridPlacement.Placement? {
        if (item.spanX > grid.columns || item.spanY > grid.rows) return null
        for (cellY in 0..grid.rows - item.spanY) {
            for (cellX in 0..grid.columns - item.spanX) {
                val candidate = WorkspaceGridPlacement.Placement(
                    itemId = item.itemId,
                    cellX = cellX,
                    cellY = cellY,
                    spanX = item.spanX,
                    spanY = item.spanY,
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

    private fun toPlacement(item: WorkspaceItemEntity): WorkspaceGridPlacement.Placement =
        WorkspaceGridPlacement.Placement(
            itemId = item.itemId,
            cellX = checkNotNull(item.cellX),
            cellY = checkNotNull(item.cellY),
            spanX = item.spanX,
            spanY = item.spanY,
        )

    private fun List<WorkspaceItemEntity>.canonicalItems(): List<WorkspaceItemEntity> =
        sortedWith(compareBy({ it.pageId }, { it.rank }, { it.itemId }))

    private fun workspaceDaoOrNull(): WorkspaceDao? = try {
        workspaceDaoProvider()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }

    private fun batchDaoOrNull(): WorkspaceHomeBatchMoveDao? = try {
        batchDaoProvider()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val MIN_HOME_COLUMNS = 4
    }
}
