package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Chooses deterministic placements for existing HOME applications, then delegates every write to
 * [WorkspacePagedRoomMutationRepository.moveHomeItem]. The preflight read never carries write
 * authority: the delegated mutation re-reads and validates the complete HOME snapshot.
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
        if (sourcePageId == targetPageId) {
            return WorkspacePagedRoomMutationResult.InvalidWorkspace
        }
        val context = readMoveContext(sourcePageId, appKey) ?: return lastReadFailure
        if (context.pages.none { it.pageId == targetPageId }) {
            return WorkspacePagedRoomMutationResult.PageNotFound
        }

        val grid = deriveGrid(context.items, context.source)
        val targetPlacements = context.items
            .filter { it.pageId == targetPageId && it.itemId != context.source.itemId }
            .map(::toPlacement)
        val target = firstAvailablePlacement(grid, targetPlacements, context.source)
            ?: return WorkspacePagedRoomMutationResult.InvalidWorkspace

        return mutationRepository.moveHomeItem(
            grid = grid,
            itemId = context.source.itemId,
            targetPageId = targetPageId,
            targetPlacement = target,
        )
    }

    suspend fun moveAppWithinPage(
        pageId: String,
        appKey: String,
        direction: WorkspaceMoveDirection,
    ): WorkspacePagedRoomMutationResult {
        if (pageId.isBlank() || appKey.isBlank()) {
            return WorkspacePagedRoomMutationResult.InvalidWorkspace
        }
        val context = readMoveContext(pageId, appKey) ?: return lastReadFailure
        val grid = deriveGrid(context.items, context.source)
        val occupied = context.items
            .filter { it.pageId == pageId && it.itemId != context.source.itemId }
            .map(::toPlacement)
        val target = relativeAvailablePlacement(grid, occupied, context.source, direction)
            ?: return WorkspacePagedRoomMutationResult.InvalidWorkspace

        return mutationRepository.moveHomeItem(
            grid = grid,
            itemId = context.source.itemId,
            targetPageId = pageId,
            targetPlacement = target,
        )
    }

    private var lastReadFailure: WorkspacePagedRoomMutationResult = WorkspacePagedRoomMutationResult.InvalidWorkspace

    private suspend fun readMoveContext(sourcePageId: String, appKey: String): MoveContext? {
        val state = authorityRepository.state.first()
        if (!state.initialized || state.authority != WorkspaceAuthority.ROOM) {
            lastReadFailure = WorkspacePagedRoomMutationResult.Reserved
            return null
        }
        val dao = workspaceDaoOrNull()
        if (dao == null) {
            lastReadFailure = WorkspacePagedRoomMutationResult.Unavailable
            return null
        }

        return try {
            val pages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (pages.none { it.pageId == sourcePageId }) {
                lastReadFailure = WorkspacePagedRoomMutationResult.PageNotFound
                return null
            }
            val items = dao.readItems(pages.map { it.pageId })
            if (items.any { it.cellX == null || it.cellY == null }) {
                lastReadFailure = WorkspacePagedRoomMutationResult.InvalidWorkspace
                return null
            }
            val candidates = items.filter {
                it.pageId == sourcePageId &&
                    it.itemType == WorkspaceItemType.APP &&
                    it.appKey == appKey
            }
            if (candidates.isEmpty()) {
                lastReadFailure = WorkspacePagedRoomMutationResult.ItemNotFound
                return null
            }
            if (candidates.size != 1) {
                lastReadFailure = WorkspacePagedRoomMutationResult.InvalidWorkspace
                return null
            }
            MoveContext(pages = pages, items = items, source = candidates.single())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            lastReadFailure = WorkspacePagedRoomMutationResult.Failed(exception::class.java.simpleName)
            null
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
                val candidate = placementAt(source, cellX, cellY)
                if (WorkspaceGridPlacement.validate(grid, occupied + candidate) == WorkspaceGridPlacement.Validation.Valid) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun relativeAvailablePlacement(
        grid: WorkspaceGridPlacement.Grid,
        occupied: List<WorkspaceGridPlacement.Placement>,
        source: WorkspaceItemEntity,
        direction: WorkspaceMoveDirection,
    ): WorkspaceGridPlacement.Placement? {
        val sourceX = checkNotNull(source.cellX)
        val sourceY = checkNotNull(source.cellY)
        val sourceIndex = sourceY * grid.columns + sourceX
        val candidates = buildList {
            for (cellY in 0..grid.rows - source.spanY) {
                for (cellX in 0..grid.columns - source.spanX) {
                    val index = cellY * grid.columns + cellX
                    val inDirection = when (direction) {
                        WorkspaceMoveDirection.EARLIER -> index < sourceIndex
                        WorkspaceMoveDirection.LATER -> index > sourceIndex
                    }
                    if (!inDirection) continue
                    val candidate = placementAt(source, cellX, cellY)
                    if (WorkspaceGridPlacement.validate(grid, occupied + candidate) == WorkspaceGridPlacement.Validation.Valid) {
                        add(index to candidate)
                    }
                }
            }
        }
        return when (direction) {
            WorkspaceMoveDirection.EARLIER -> candidates.maxByOrNull { it.first }?.second
            WorkspaceMoveDirection.LATER -> candidates.minByOrNull { it.first }?.second
        }
    }

    private fun placementAt(source: WorkspaceItemEntity, cellX: Int, cellY: Int) =
        WorkspaceGridPlacement.Placement(
            itemId = source.itemId,
            cellX = cellX,
            cellY = cellY,
            spanX = source.spanX,
            spanY = source.spanY,
        )

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

    private data class MoveContext(
        val pages: List<WorkspacePageEntity>,
        val items: List<WorkspaceItemEntity>,
        val source: WorkspaceItemEntity,
    )

    private companion object {
        const val MIN_HOME_COLUMNS = 4
    }
}
