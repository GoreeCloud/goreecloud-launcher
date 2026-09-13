package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WorkspaceHomePageCompactionResult {
    data class Applied(
        val pageId: String,
        val itemCount: Int,
    ) : WorkspaceHomePageCompactionResult

    data object AlreadyCompact : WorkspaceHomePageCompactionResult
    data object PrimaryPageProtected : WorkspaceHomePageCompactionResult
    data object PageNotFound : WorkspaceHomePageCompactionResult
    data object UnsupportedPageItems : WorkspaceHomePageCompactionResult
    data object CapacityExceeded : WorkspaceHomePageCompactionResult
    data object InvalidWorkspace : WorkspaceHomePageCompactionResult
    data object Reserved : WorkspaceHomePageCompactionResult
    data object Unavailable : WorkspaceHomePageCompactionResult
    data object StoredWorkspaceChanged : WorkspaceHomePageCompactionResult
    data class Failed(val failureType: String) : WorkspaceHomePageCompactionResult
}

/**
 * Bounded secondary-HOME page cleanup service.
 *
 * Planning reads the complete Room-authoritative HOME snapshot. Eligible 1x1 application items are
 * packed into row-major cells in their existing rank order. The transaction then rechecks that same
 * complete HOME snapshot before writing every changed row together. No item is moved across pages,
 * the primary compatibility page is never touched, and unsupported folder/widget/shortcut rows make
 * the operation fail closed rather than being guessed at or silently rearranged.
 */
class WorkspaceHomePageCompactionService(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
    private val batchDaoProvider: () -> WorkspaceHomeBatchMoveDao?,
) {
    suspend fun compact(
        grid: WorkspaceGridPlacement.Grid,
        pageId: String,
    ): WorkspaceHomePageCompactionResult {
        val authority = authorityRepository.state.first()
        if (!authority.initialized || authority.authority != WorkspaceAuthority.ROOM) {
            return WorkspaceHomePageCompactionResult.Reserved
        }
        if (pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID) {
            return WorkspaceHomePageCompactionResult.PrimaryPageProtected
        }
        if (pageId.isBlank()) return WorkspaceHomePageCompactionResult.InvalidWorkspace

        val dao = workspaceDaoOrNull() ?: return WorkspaceHomePageCompactionResult.Unavailable
        val batchDao = batchDaoOrNull() ?: return WorkspaceHomePageCompactionResult.Unavailable

        return try {
            val storedPages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
            if (storedPages.none { it.pageId == pageId }) {
                return WorkspaceHomePageCompactionResult.PageNotFound
            }
            if (
                storedPages.isEmpty() ||
                storedPages.map { it.rank } != storedPages.indices.toList() ||
                storedPages.firstOrNull()?.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID
            ) {
                return WorkspaceHomePageCompactionResult.InvalidWorkspace
            }

            val storedItems = dao.readItemsByContainer(WorkspaceContainerType.HOME)
            if (storedItems.map { it.itemId }.distinct().size != storedItems.size) {
                return WorkspaceHomePageCompactionResult.InvalidWorkspace
            }

            when (val plan = WorkspaceHomePageCompactionPolicy.plan(grid, pageId, storedItems)) {
                WorkspaceHomePageCompactionPolicy.Plan.AlreadyCompact -> {
                    WorkspaceHomePageCompactionResult.AlreadyCompact
                }
                WorkspaceHomePageCompactionPolicy.Plan.PrimaryPageProtected -> {
                    WorkspaceHomePageCompactionResult.PrimaryPageProtected
                }
                WorkspaceHomePageCompactionPolicy.Plan.UnsupportedPageItems -> {
                    WorkspaceHomePageCompactionResult.UnsupportedPageItems
                }
                WorkspaceHomePageCompactionPolicy.Plan.CapacityExceeded -> {
                    WorkspaceHomePageCompactionResult.CapacityExceeded
                }
                WorkspaceHomePageCompactionPolicy.Plan.InvalidPage -> {
                    WorkspaceHomePageCompactionResult.InvalidWorkspace
                }
                is WorkspaceHomePageCompactionPolicy.Plan.Updated -> {
                    if (!batchDao.compactPageIfSnapshotMatches(
                            pageId = pageId,
                            expectedPages = storedPages,
                            expectedItems = storedItems,
                            compactedItems = plan.items,
                        )
                    ) {
                        WorkspaceHomePageCompactionResult.StoredWorkspaceChanged
                    } else {
                        WorkspaceHomePageCompactionResult.Applied(
                            pageId = pageId,
                            itemCount = plan.items.size,
                        )
                    }
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceHomePageCompactionResult.Failed(exception::class.java.simpleName)
        }
    }

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
}
