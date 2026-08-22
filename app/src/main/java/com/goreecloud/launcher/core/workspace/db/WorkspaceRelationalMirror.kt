package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceState

class WorkspaceRelationalMirror(
    private val workspaceDao: WorkspaceDao,
) {
    suspend fun sync(state: WorkspaceState) {
        if (!state.initialized) return

        val import = WorkspaceLegacyImportMapper.map(
            favoriteKeys = state.favoriteKeys,
            dockKeys = state.dockKeys,
        )
        workspaceDao.replaceLegacySnapshot(
            pages = import.pages,
            items = import.items,
        )
    }
}
