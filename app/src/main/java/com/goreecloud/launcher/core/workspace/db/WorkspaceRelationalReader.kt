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
        val pageById = pages.associateBy { it.pageId }
        if (pageById.size != pages.size) return null
        if (
            pageById[WorkspaceLegacyImportMapper.HOME_PAGE_ID] != WorkspacePageEntity(
                pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
                containerType = WorkspaceContainerType.HOME,
                rank = 0,
            ) ||
            pageById[WorkspaceLegacyImportMapper.DOCK_PAGE_ID] != WorkspacePageEntity(
                pageId = WorkspaceLegacyImportMapper.DOCK_PAGE_ID,
                containerType = WorkspaceContainerType.DOCK,
                rank = 0,
            )
        ) return null

        val homeItems = items.filter { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }.sortedBy { it.rank }
        val dockItems = items.filter { it.pageId == WorkspaceLegacyImportMapper.DOCK_PAGE_ID }.sortedBy { it.rank }
        if (homeItems.size + dockItems.size != items.size) return null
        if (homeItems.map { it.rank } != homeItems.indices.toList()) return null
        if (dockItems.map { it.rank } != dockItems.indices.toList()) return null

        fun validCanonicalApp(item: WorkspaceItemEntity, prefix: String): Boolean {
            val appKey = item.appKey ?: return false
            return appKey.isNotBlank() &&
                item.itemType == WorkspaceItemType.APP &&
                item.itemId == "$prefix$appKey" &&
                item.spanX == 1 &&
                item.spanY == 1
        }

        if (homeItems.any { !validCanonicalApp(it, "legacy:home:") }) return null
        if (dockItems.any { !validCanonicalApp(it, "legacy:dock:") }) return null
        if (homeItems.mapNotNull { it.appKey }.distinct().size != homeItems.size) return null
        if (dockItems.mapNotNull { it.appKey }.distinct().size != dockItems.size) return null
        if (dockItems.any { it.cellX != null || it.cellY != null }) return null

        val homeCompatibility = homeItems.all { it.cellX == null && it.cellY == null }
        val homeSpatial = homeItems.all { it.cellX != null && it.cellY != null }
        if (!homeCompatibility && !homeSpatial) return null
        if (homeSpatial) {
            val occupied = mutableSetOf<Pair<Int, Int>>()
            for (item in homeItems) {
                val x = checkNotNull(item.cellX)
                val y = checkNotNull(item.cellY)
                if (x < 0 || y < 0 || !occupied.add(x to y)) return null
            }
        }

        return WorkspaceRelationalSnapshot(
            favoriteKeys = homeItems.map { checkNotNull(it.appKey) },
            dockKeys = dockItems.map { checkNotNull(it.appKey) },
        )
    }
}

internal object WorkspaceCanonicalRoomPlacementReader {
    suspend fun read(workspaceDao: WorkspaceDao): WorkspaceRelationalSnapshot? {
        val pageIds = listOf(
            WorkspaceLegacyImportMapper.HOME_PAGE_ID,
            WorkspaceLegacyImportMapper.DOCK_PAGE_ID,
        )
        return WorkspaceRelationalReadMapper.map(
            pages = workspaceDao.readPages(pageIds),
            items = workspaceDao.readItems(pageIds),
        )
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
            val relationalState = WorkspaceCanonicalRoomPlacementReader.read(workspaceDao)
                ?: return WorkspaceDualReadResult.Mismatch

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
