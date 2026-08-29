package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Chooses a deterministic free cell for moving one existing HOME application to another page,
 * then delegates the write to [WorkspacePagedRoomMutationRepository.moveHomeItem].
 *
 * The preflight read never carries write authority. The delegated move re-reads and validates the
 * complete HOME page/item snapshot, so any concurrent workspace change still fails closed.
 */
class WorkspaceHomeItemPageMover(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
    private val mutationRepository: WorkspacePagedRoomMutationRepository,
) {
    suspend fun moveAppToPage(
        sourcePageId: String,
        appKey: String,
        targetPageId: String,
    ): WorkspacePagedRoomMutationResult {
        if (sourcePageId.isBlank() || appKey.isBlank() || targetPageId.isBlank()) {
            return WorkspacePagedRoomMutationResult.InvalidWorkspace
        }
        val state = authorityRepository.state.first()
        if (!state.initialized || state.authority != WorkspaceAuthority.ROOM) {
            return WorkspacePagedRoomMutationResult.Reserved
        }
        if (sourcePageId == targetPageId) {
            return WorkspacePagedRoomMutationResult.InvalidWorkspace
        }
        val dao = workspaceDaoOrNull() ?: return WorkspacePagedRoomMutationResult.Unavailable

        return try {
            val pages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (pages.none { it.pageId == sourcePageId } || pages.none { it.pageId == targetPageId }) {
                return WorkspacePagedRoomMutationResult.PageNotFound
            }
            val items = dao.readItems(pages.map { it.pageId })
            if (items.any { it.cellX == null || it.cellY == null }) {
                return WorkspacePagedRoomMutationResult.InvalidWorkspace
            }
            val candidates = items.filter {
                it.pageId == sourcePageId &&
                    it.itemType == WorkspaceItemType.APP &&
                    it.appKey == appKey
            }
            if (candidates.isEmpty()) return WorkspacePagedRoomMutationResult.ItemNotFound
            if (candidates.size != 1) return WorkspacePagedRoomMutationResult.InvalidWorkspace
            val source = candidates.single()

            val grid = deriveGrid(items, source)
            val targetPlacements = items
                .filter { it.pageId == targetPageId && it.itemId != source.itemId }
                .map(::toPlacement)
            val target = firstAvailablePlacement(grid, targetPlacements, source)
                ?: return WorkspacePagedRoomMutationResult.InvalidWorkspace

            mutationRepository.moveHomeItem(
                grid = grid,
                itemId = source.itemId,
                targetPageId = targetPageId,
                targetPlacement = target,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspacePagedRoomMutationResult.Failed(exception::class.java.simpleName)
        }
    }

    private fun deriveGrid(
        items: List<WorkspaceItemEntity>,
        source: WorkspaceItemEntity,
    ): WorkspaceGridPlacement.Grid {
        val existingColumns = items.maxOfOrNull { checkNotNull(it.cellX) + it.spanX } ?: 0
        val existingRows = items.maxOfOrNull { checkNotNull(it.cellY) + it.spanY } ?: 0
        val columns = maxOf(MIN_HOME_COLUMNS, existingColumns, source.spanX)
        val rows = maxOf(1, existingRows + source.spanY)
        return WorkspaceGridPlacement.Grid(columns = columns, rows = rows)
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
                if (WorkspaceGridPlacement.validate(grid, occupied + candidate) == WorkspaceGridPlacement.Validation.Valid) {
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
