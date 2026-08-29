package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Read-only page projection for the rendered HOME surface.
 *
 * This observer never creates pages, rewrites ranks, or changes item placement. Before terminal
 * Room authority it exposes WaitingForRoom rather than inventing a second pre-cutover source of
 * truth. After cutover, inability to acquire/read Room fails closed as RecoveryRequired.
 */
sealed interface WorkspacePagedHomeState {
    data object WaitingForRoom : WorkspacePagedHomeState
    data class Ready(val pages: List<WorkspaceRenderedHomePage>) : WorkspacePagedHomeState
    data class RecoveryRequired(val reason: String) : WorkspacePagedHomeState
}

data class WorkspaceRenderedHomePage(
    val pageId: String,
    val rank: Int,
    val appKeys: List<String>,
    val unsupportedItemCount: Int,
)

object WorkspacePagedHomeMapper {
    fun map(
        pages: List<WorkspacePageEntity>,
        items: List<WorkspaceItemEntity>,
    ): WorkspacePagedHomeState {
        val homePages = pages
            .filter { it.containerType == WorkspaceContainerType.HOME }
            .sortedBy { it.rank }
        if (homePages.isEmpty()) {
            return WorkspacePagedHomeState.RecoveryRequired("MissingHomePages")
        }
        if (homePages.map { it.pageId }.distinct().size != homePages.size) {
            return WorkspacePagedHomeState.RecoveryRequired("DuplicateHomePageId")
        }
        if (homePages.map { it.rank }.distinct().size != homePages.size) {
            return WorkspacePagedHomeState.RecoveryRequired("DuplicateHomePageRank")
        }

        val homePageIds = homePages.map { it.pageId }.toSet()
        val homeItems = items.filter { it.pageId in homePageIds }
        if (homeItems.map { it.itemId }.distinct().size != homeItems.size) {
            return WorkspacePagedHomeState.RecoveryRequired("DuplicateHomeItemId")
        }

        val itemsByPage = homeItems.groupBy { it.pageId }
        val rendered = homePages.map { page ->
            val pageItems = itemsByPage[page.pageId].orEmpty().sortedBy { it.rank }
            val appItems = pageItems.filter { it.itemType == WorkspaceItemType.APP }
            if (appItems.any { it.appKey.isNullOrBlank() }) {
                return WorkspacePagedHomeState.RecoveryRequired("MalformedHomeAppItem")
            }
            WorkspaceRenderedHomePage(
                pageId = page.pageId,
                rank = page.rank,
                appKeys = appItems.map { checkNotNull(it.appKey) },
                unsupportedItemCount = pageItems.count { it.itemType != WorkspaceItemType.APP },
            )
        }
        return WorkspacePagedHomeState.Ready(rendered)
    }
}

class WorkspacePagedHomeObserver(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<WorkspacePagedHomeState> = authorityRepository.state
        .map { it.initialized to it.authority }
        .distinctUntilChanged()
        .flatMapLatest { (initialized, authority) ->
            if (!initialized || authority != WorkspaceAuthority.ROOM) {
                return@flatMapLatest flowOf(WorkspacePagedHomeState.WaitingForRoom)
            }

            val dao = try {
                workspaceDaoProvider()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                null
            }
            if (dao == null) {
                return@flatMapLatest flowOf(
                    WorkspacePagedHomeState.RecoveryRequired("RoomUnavailable")
                )
            }

            combine(dao.observePages(), dao.observeItems()) { pages, items ->
                WorkspacePagedHomeMapper.map(pages, items)
            }.catch { exception ->
                if (exception is CancellationException) throw exception
                emit(WorkspacePagedHomeState.RecoveryRequired(exception::class.java.simpleName))
            }
        }
        .distinctUntilChanged()
}
