package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.WorkspaceWidgetPlacementPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WorkspaceFolderMutationResult {
    data object Reserved : WorkspaceFolderMutationResult
    data object Unavailable : WorkspaceFolderMutationResult
    data object InvalidWorkspace : WorkspaceFolderMutationResult
    data object NoSpace : WorkspaceFolderMutationResult
    data object NotFound : WorkspaceFolderMutationResult
    data object StoredWorkspaceChanged : WorkspaceFolderMutationResult
    data class Added(
        val itemId: String,
        val folderId: String,
        val cellX: Int,
        val cellY: Int,
    ) : WorkspaceFolderMutationResult
    data class Moved(
        val itemId: String,
        val folderId: String,
        val cellX: Int,
        val cellY: Int,
    ) : WorkspaceFolderMutationResult
    data class Removed(
        val itemId: String,
        val folderId: String,
    ) : WorkspaceFolderMutationResult
    data class Failed(val failureType: String) : WorkspaceFolderMutationResult
}

class WorkspaceFolderRepository(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    suspend fun addFolderToHome(
        itemId: String,
        folderId: String,
        columns: Int,
        rows: Int,
    ): WorkspaceFolderMutationResult {
        if (!isRoomAuthoritative()) return WorkspaceFolderMutationResult.Reserved
        if (itemId.isBlank() || folderId.isBlank() || columns <= 0 || rows <= 0) {
            return WorkspaceFolderMutationResult.InvalidWorkspace
        }
        val dao = workspaceDaoOrNull() ?: return WorkspaceFolderMutationResult.Unavailable
        return try {
            val page = primaryPage(dao) ?: return WorkspaceFolderMutationResult.InvalidWorkspace
            val items = primaryItems(dao)
            if (
                items.any { it.itemId == itemId } ||
                items.any {
                    it.itemType == WorkspaceItemType.FOLDER &&
                        it.appKey == folderId
                }
            ) {
                return WorkspaceFolderMutationResult.InvalidWorkspace
            }
            val placements = items.mapNotNull(WorkspaceItemEntity::toFolderSpatialPlacement)
            if (placements.size != items.size) {
                return WorkspaceFolderMutationResult.InvalidWorkspace
            }
            val placement = WorkspaceWidgetPlacementPolicy.firstAvailable(
                grid = WorkspaceGridPlacement.Grid(columns, rows),
                existing = placements,
                itemId = itemId,
                spanX = 1,
                spanY = 1,
            ) ?: return WorkspaceFolderMutationResult.NoSpace

            val folder = WorkspaceItemEntity(
                itemId = itemId,
                pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
                itemType = WorkspaceItemType.FOLDER,
                appKey = folderId,
                rank = items.size,
                cellX = placement.cellX,
                cellY = placement.cellY,
                spanX = 1,
                spanY = 1,
            )
            val updated = items + folder
            if (!dao.replacePrimaryHomeItemsIncludingIdentityChangesIfSnapshotMatches(
                    expectedPage = page,
                    expectedItems = items,
                    updatedItems = updated,
                )
            ) {
                return WorkspaceFolderMutationResult.StoredWorkspaceChanged
            }
            WorkspaceFolderMutationResult.Added(
                itemId = itemId,
                folderId = folderId,
                cellX = placement.cellX,
                cellY = placement.cellY,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceFolderMutationResult.Failed(exception::class.java.simpleName)
        }
    }

    /**
     * Move an existing HOME folder within the current primary grid without recreating its
     * identity, contents, or user-specified name. Every target is checked against all saved
     * widgets, apps, and folders; Room performs a snapshot-conditional atomic write.
     */
    suspend fun moveFolderToCell(
        folderId: String,
        columns: Int,
        rows: Int,
        cellX: Int,
        cellY: Int,
    ): WorkspaceFolderMutationResult {
        if (!isRoomAuthoritative()) return WorkspaceFolderMutationResult.Reserved
        if (
            folderId.isBlank() || columns <= 0 || rows <= 0 ||
            cellX !in 0 until columns || cellY !in 0 until rows
        ) return WorkspaceFolderMutationResult.InvalidWorkspace
        val dao = workspaceDaoOrNull() ?: return WorkspaceFolderMutationResult.Unavailable
        return try {
            val page = primaryPage(dao) ?: return WorkspaceFolderMutationResult.InvalidWorkspace
            val items = primaryItems(dao)
            val folder = items.singleOrNull {
                it.itemType == WorkspaceItemType.FOLDER && it.appKey == folderId
            } ?: return WorkspaceFolderMutationResult.NotFound
            val placements = items.mapNotNull(WorkspaceItemEntity::toFolderSpatialPlacement)
            if (placements.size != items.size) {
                return WorkspaceFolderMutationResult.InvalidWorkspace
            }
            val target = WorkspaceWidgetPlacementPolicy.move(
                grid = WorkspaceGridPlacement.Grid(columns, rows),
                existing = placements,
                itemId = folder.itemId,
                cellX = cellX,
                cellY = cellY,
            ) ?: return WorkspaceFolderMutationResult.NoSpace
            if (folder.cellX == target.cellX && folder.cellY == target.cellY) {
                return WorkspaceFolderMutationResult.Moved(
                    folder.itemId, folderId, target.cellX, target.cellY,
                )
            }
            val updated = items.map { item ->
                if (item.itemId == folder.itemId) item.copy(
                    cellX = target.cellX,
                    cellY = target.cellY,
                ) else item
            }
            if (!dao.replacePrimaryHomeItemsIfSnapshotMatches(
                    expectedPage = page,
                    expectedItems = items,
                    updatedItems = updated,
                )
            ) return WorkspaceFolderMutationResult.StoredWorkspaceChanged
            WorkspaceFolderMutationResult.Moved(
                folder.itemId, folderId, target.cellX, target.cellY,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceFolderMutationResult.Failed(exception::class.java.simpleName)
        }
    }

    suspend fun removeFolderFromHome(folderId: String): WorkspaceFolderMutationResult {
        if (!isRoomAuthoritative()) return WorkspaceFolderMutationResult.Reserved
        if (folderId.isBlank()) return WorkspaceFolderMutationResult.InvalidWorkspace
        val dao = workspaceDaoOrNull() ?: return WorkspaceFolderMutationResult.Unavailable
        return try {
            val page = primaryPage(dao) ?: return WorkspaceFolderMutationResult.InvalidWorkspace
            val items = primaryItems(dao)
            val folder = items.singleOrNull {
                it.itemType == WorkspaceItemType.FOLDER && it.appKey == folderId
            } ?: return WorkspaceFolderMutationResult.NotFound
            val updated = items
                .filterNot { it.itemId == folder.itemId }
                .mapIndexed { rank, item -> item.copy(rank = rank) }
            if (!dao.replacePrimaryHomeItemsIncludingIdentityChangesIfSnapshotMatches(
                    expectedPage = page,
                    expectedItems = items,
                    updatedItems = updated,
                )
            ) {
                return WorkspaceFolderMutationResult.StoredWorkspaceChanged
            }
            WorkspaceFolderMutationResult.Removed(folder.itemId, folderId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceFolderMutationResult.Failed(exception::class.java.simpleName)
        }
    }

    private suspend fun primaryPage(dao: WorkspaceDao): WorkspacePageEntity? =
        dao.readPages(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID)).singleOrNull()?.takeIf {
            it.containerType == WorkspaceContainerType.HOME && it.rank == 0
        }

    private suspend fun primaryItems(dao: WorkspaceDao): List<WorkspaceItemEntity> =
        dao.readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID)).sortedBy { it.rank }

    private suspend fun isRoomAuthoritative(): Boolean {
        val state = authorityRepository.state.first()
        return state.initialized && state.authority == WorkspaceAuthority.ROOM
    }

    private fun workspaceDaoOrNull(): WorkspaceDao? = try {
        workspaceDaoProvider()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }
}

private fun WorkspaceItemEntity.toFolderSpatialPlacement(): WorkspaceGridPlacement.Placement? {
    val x = cellX ?: return null
    val y = cellY ?: return null
    return WorkspaceGridPlacement.Placement(
        itemId = itemId,
        cellX = x,
        cellY = y,
        spanX = spanX,
        spanY = spanY,
    )
}
