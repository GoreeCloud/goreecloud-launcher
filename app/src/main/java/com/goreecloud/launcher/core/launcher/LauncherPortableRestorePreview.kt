package com.goreecloud.launcher.core.launcher

import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot

/**
 * Read-only preflight for the currently approved combined Launcher portability subsets.
 *
 * This object has no Room, DataStore, package/profile, widget, HOME, or writer authority. It
 * validates the same complete snapshots used by [LauncherPortableRestoreImport] and exposes only
 * aggregate workspace geometry/counts plus the seven reviewed preference values.
 */
object LauncherPortableRestorePreview {
    data class Summary(
        val gridColumns: Int,
        val gridRows: Int,
        val pageCount: Int,
        val itemCount: Int,
        val homeColumns: Int,
        val homeRows: Int,
        val drawerColumns: Int,
        val showLabels: Boolean,
        val iconScale: Float,
        val layoutLocked: Boolean,
        val indexHomeMode: GoreeCloudIndexHomeMode,
    )

    sealed interface Result {
        data class Ready(val summary: Summary) : Result

        data class Rejected(
            val source: LauncherPortableRestoreImport.RejectionSource,
            val reason: String,
        ) : Result
    }

    fun inspect(
        workspaceEncoded: String,
        preferencesEncoded: String,
    ): Result {
        val workspace = when (val decoded = WorkspacePortableSnapshot.decode(workspaceEncoded)) {
            is WorkspacePortableSnapshot.DecodeResult.Invalid -> {
                return Result.Rejected(
                    LauncherPortableRestoreImport.RejectionSource.WORKSPACE,
                    decoded.reason,
                )
            }
            is WorkspacePortableSnapshot.DecodeResult.Success -> decoded.snapshot
        }

        val preferences = when (val decoded = LauncherPortablePreferences.decode(preferencesEncoded)) {
            is LauncherPortablePreferences.DecodeResult.Invalid -> {
                return Result.Rejected(
                    LauncherPortableRestoreImport.RejectionSource.PREFERENCES,
                    decoded.reason,
                )
            }
            is LauncherPortablePreferences.DecodeResult.Success -> decoded.preferences
        }

        return Result.Ready(
            Summary(
                gridColumns = workspace.grid.columns,
                gridRows = workspace.grid.rows,
                pageCount = workspace.pages.size,
                itemCount = workspace.pages.sumOf { it.placements.size },
                homeColumns = preferences.homeColumns,
                homeRows = preferences.homeRows,
                drawerColumns = preferences.drawerColumns,
                showLabels = preferences.showLabels,
                iconScale = preferences.iconScale,
                layoutLocked = preferences.layoutLocked,
                indexHomeMode = preferences.indexHomeMode,
            )
        )
    }
}
