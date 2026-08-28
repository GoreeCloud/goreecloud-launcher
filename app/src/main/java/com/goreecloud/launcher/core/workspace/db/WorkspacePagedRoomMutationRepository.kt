package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspacePagedPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WorkspacePagedRoomMutationResult {
    data object Reserved : WorkspacePagedRoomMutationResult
    data object Unavailable : WorkspacePagedRoomMutationResult
    data object PageNotFound : WorkspacePagedRoomMutationResult
    data object ItemNotFound : WorkspacePagedRoomMutationResult
    data object ItemNotGridPlaced : WorkspacePagedRoomMutationResult
    data object TargetRankOutOfRange : WorkspacePagedRoomMutationResult
    data object StoredPageSetChanged : WorkspacePagedRoomMutationResult
    data object StoredItemSetChanged : WorkspacePagedRoomMutationResult
    data class Updated(val orderedPageIds: List<String>) : WorkspacePagedRoomMutationResult
    data class ItemUpdated(val itemId: String, val pageId: String) : WorkspacePagedRoomMutationResult
    data class Rejected(val reason: String) : WorkspacePagedRoomMutationResult
    data class Failed(val failureType: String) : WorkspacePagedRoomMutationResult
}

/** Authoritative Room wiring for validated multi-page HOME mutations. */
class WorkspacePagedRoomMutationRepository(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    suspend fun moveHomePage(
        pageId: String,
        targetRank: Int,
    ): WorkspacePagedRoomMutationResult {
        if (!isRoomAuthoritative()) return WorkspacePagedRoomMutationResult.Reserved
        val dao = workspaceDaoOrNull() ?: return WorkspacePagedRoomMutationResult.Unavailable

        return try {
            val storedPages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (storedPages.none { it.pageId == pageId }) {
                return WorkspacePagedRoomMutationResult.PageNotFound
            }
            if (targetRank !in storedPages.indices) {
                return WorkspacePagedRoomMutationResult.TargetRankOutOfRange
            }

            val domainPages = storedPages.map { page ->
                WorkspacePagedPlacement.Page(
                    pageId = page.pageId,
                    rank = page.rank,
                    placements = emptyList(),
                )
            }
            val mutation = WorkspacePagedPlacement.movePage(
                grid = WorkspaceGridPlacement.Grid(columns = 1, rows = 1),
                pages = domainPages,
                pageId = pageId,
                targetRank = targetRank,
            )
            val updated = mutation as? WorkspacePagedPlacement.Mutation.Updated
                ?: return when (mutation) {
                    is WorkspacePagedPlacement.Mutation.PageNotFound -> WorkspacePagedRoomMutationResult.PageNotFound
                    is WorkspacePagedPlacement.Mutation.TargetRankOutOfRange -> WorkspacePagedRoomMutationResult.TargetRankOutOfRange
                    else -> WorkspacePagedRoomMutationResult.StoredPageSetChanged
                }

            val orderedPageIds = updated.pages.map { it.pageId }
            if (!dao.replacePageOrder(WorkspaceContainerType.HOME, orderedPageIds)) {
                return WorkspacePagedRoomMutationResult.StoredPageSetChanged
            }
            WorkspacePagedRoomMutationResult.Updated(orderedPageIds)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspacePagedRoomMutationResult.Failed(exception::class.java.simpleName)
        }
    }

    suspend fun moveHomeItem(
        itemId: String,
        targetPageId: String,
        targetPlacement: WorkspaceGridPlacement.Placement,
        grid: WorkspaceGridPlacement.Grid = WorkspaceGridPlacement.Grid(columns = 4, rows = 6),
    ): WorkspacePagedRoomMutationResult {
        if (!isRoomAuthoritative()) return WorkspacePagedRoomMutationResult.Reserved
        val dao = workspaceDaoOrNull() ?: return WorkspacePagedRoomMutationResult.Unavailable

        return try {
            val storedPages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (storedPages.none { it.pageId == targetPageId }) {
                return WorkspacePagedRoomMutationResult.PageNotFound
            }
            val pageIds = storedPages.map { it.pageId }
            val storedItems = dao.readItems(pageIds)
            val source = storedItems.firstOrNull { it.itemId == itemId }
                ?: return WorkspacePagedRoomMutationResult.ItemNotFound
            if (source.cellX == null || source.cellY == null) {
                return WorkspacePagedRoomMutationResult.ItemNotGridPlaced
            }

            val itemsByPage = storedItems.groupBy { it.pageId }
            val domainPages = storedPages.map { page ->
                WorkspacePagedPlacement.Page(
                    pageId = page.pageId,
                    rank = page.rank,
                    placements = itemsByPage[page.pageId].orEmpty().map { item ->
                        val x = item.cellX ?: return WorkspacePagedRoomMutationResult.ItemNotGridPlaced
                        val y = item.cellY ?: return WorkspacePagedRoomMutationResult.ItemNotGridPlaced
                        WorkspaceGridPlacement.Placement(
                            itemId = item.itemId,
                            cellX = x,
                            cellY = y,
                            spanX = item.spanX,
                            spanY = item.spanY,
                        )
                    },
                )
            }

            val mutation = WorkspacePagedPlacement.moveItem(
                grid = grid,
                pages = domainPages,
                itemId = itemId,
                targetPageId = targetPageId,
                targetPlacement = targetPlacement,
            )
            val updated = mutation as? WorkspacePagedPlacement.Mutation.Updated
                ?: return when (mutation) {
                    is WorkspacePagedPlacement.Mutation.PageNotFound -> WorkspacePagedRoomMutationResult.PageNotFound
                    is WorkspacePagedPlacement.Mutation.ItemNotFound -> WorkspacePagedRoomMutationResult.ItemNotFound
                    is WorkspacePagedPlacement.Mutation.ItemIdentityMismatch -> WorkspacePagedRoomMutationResult.Rejected("item identity mismatch")
                    is WorkspacePagedPlacement.Mutation.InvalidWorkspace -> WorkspacePagedRoomMutationResult.Rejected(mutation.reason.toString())
                    else -> WorkspacePagedRoomMutationResult.Rejected(mutation.toString())
                }

            val originalById = storedItems.associateBy { it.itemId }
            val replacements = updated.pages.flatMap { page ->
                page.placements.mapIndexed { rank, placement ->
                    val original = checkNotNull(originalById[placement.itemId])
                    original.copy(
                        pageId = page.pageId,
                        rank = rank,
                        cellX = placement.cellX,
                        cellY = placement.cellY,
                        spanX = placement.spanX,
                        spanY = placement.spanY,
                    )
                }
            }
            if (!dao.replaceItemPlacements(pageIds, storedItems, replacements)) {
                return WorkspacePagedRoomMutationResult.StoredItemSetChanged
            }
            WorkspacePagedRoomMutationResult.ItemUpdated(itemId, targetPageId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspacePagedRoomMutationResult.Failed(exception::class.java.simpleName)
        }
    }

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
