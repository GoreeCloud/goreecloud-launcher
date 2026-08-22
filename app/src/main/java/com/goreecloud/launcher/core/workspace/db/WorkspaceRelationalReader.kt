package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceState
import kotlinx.coroutines.CancellationException

data class WorkspaceRelationalSnapshot(
    val favoriteKeys: List<String>,
    val dockKeys: List<String>,
)

sealed interface WorkspaceDualReadResult {
    data object Skipped : WorkspaceDualReadResult
    data object Match : WorkspaceDualReadResult
    data object Mismatch : WorkspaceDualReadResult
    data class Failed(val failureType: String) : WorkspaceDualReadResult
}

internal object WorkspaceRelationalReadMapper {
    fun map(
        pages: List<WorkspacePageEntity>,
        items: List<WorkspaceItemEntity>,
    ): WorkspaceRelationalSnapshot? {
        if (pages.size != 2) return null

        val favoriteKeys = items
            .filter { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
            .sortedBy { it.rank }
            .mapNotNull { it.appKey }
        val dockKeys = items
            .filter { it.pageId == WorkspaceLegacyImportMapper.DOCK_PAGE_ID }
            .sortedBy { it.rank }
            .mapNotNull { it.appKey }

        if (favoriteKeys.size + dockKeys.size != items.size) return null

        val candidate = WorkspaceRelationalSnapshot(
            favoriteKeys = favoriteKeys,
            dockKeys = dockKeys,
        )
        val canonical = WorkspaceLegacyImportMapper.map(
            favoriteKeys = candidate.favoriteKeys,
            dockKeys = candidate.dockKeys,
        )

        return candidate.takeIf {
            WorkspaceRelationalVerifier.matches(
                expected = canonical,
                actualPages = pages,
                actualItems = items,
            )
        }
    }
}

class WorkspaceRelationalReader(
    private val workspaceDao: WorkspaceDao,
) {
    suspend fun reconcile(authoritativeState: WorkspaceState): WorkspaceDualReadResult {
        if (
            !authoritativeState.initialized ||
            authoritativeState.authority != WorkspaceAuthority.ROOM_VERIFIED
        ) {
            return WorkspaceDualReadResult.Skipped
        }

        return try {
            val pageIds = listOf(
                WorkspaceLegacyImportMapper.HOME_PAGE_ID,
                WorkspaceLegacyImportMapper.DOCK_PAGE_ID,
            )
            val relationalState = WorkspaceRelationalReadMapper.map(
                pages = workspaceDao.readPages(pageIds),
                items = workspaceDao.readItems(pageIds),
            ) ?: return WorkspaceDualReadResult.Mismatch

            if (
                relationalState.favoriteKeys == authoritativeState.favoriteKeys &&
                relationalState.dockKeys == authoritativeState.dockKeys
            ) {
                WorkspaceDualReadResult.Match
            } else {
                WorkspaceDualReadResult.Mismatch
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceDualReadResult.Failed(exception::class.java.simpleName)
        }
    }
}
