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
    data object TargetRankOutOfRange : WorkspacePagedRoomMutationResult
    data object StoredPageSetChanged : WorkspacePagedRoomMutationResult
    data class Updated(val orderedPageIds: List<String>) : WorkspacePagedRoomMutationResult
    data class Failed(val failureType: String) : WorkspacePagedRoomMutationResult
}

/**
 * Authoritative Room wiring for multi-page HOME ordering.
 *
 * The repository deliberately mutates page rank only. Item placement persistence remains a
 * separate milestone so page-order acceptance cannot silently imply cross-page item-write
 * acceptance.
 */
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
