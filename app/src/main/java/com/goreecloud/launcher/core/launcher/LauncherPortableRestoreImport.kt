package com.goreecloud.launcher.core.launcher

import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot

/**
 * Minimal combined persistence authority for the two currently approved Launcher portability
 * subsets.
 *
 * A concrete writer receives both already-validated values in one call so Room/DataStore
 * implementations can later provide a real transaction boundary without this coordinator gaining
 * package/profile discovery, widget rebinding, folder/dock reconstruction, or broader HOME state
 * authority.
 */
interface LauncherPortableRestoreWriter {
    suspend fun replacePortableState(
        workspace: WorkspacePortableSnapshot.Snapshot,
        preferences: LauncherPreferences,
    )
}

object LauncherPortableRestoreImport {
    enum class RejectionSource {
        WORKSPACE,
        PREFERENCES,
    }

    sealed interface ApplyResult {
        data class Applied(
            val workspace: WorkspacePortableSnapshot.Snapshot,
            val preferences: LauncherPreferences,
        ) : ApplyResult

        data class Rejected(
            val source: RejectionSource,
            val reason: String,
        ) : ApplyResult
    }

    /**
     * Validate both complete snapshots before granting any persistence call.
     *
     * If either input is malformed, tampered, expanded, or unsupported, the writer is never
     * invoked. Storage failures deliberately propagate so callers cannot report a partial or
     * failed commit as an applied restore.
     */
    suspend fun apply(
        workspaceEncoded: String,
        preferencesEncoded: String,
        writer: LauncherPortableRestoreWriter,
    ): ApplyResult {
        val workspace = when (val decoded = WorkspacePortableSnapshot.decode(workspaceEncoded)) {
            is WorkspacePortableSnapshot.DecodeResult.Invalid -> {
                return ApplyResult.Rejected(RejectionSource.WORKSPACE, decoded.reason)
            }
            is WorkspacePortableSnapshot.DecodeResult.Success -> decoded.snapshot
        }

        val preferences = when (val decoded = LauncherPortablePreferences.decode(preferencesEncoded)) {
            is LauncherPortablePreferences.DecodeResult.Invalid -> {
                return ApplyResult.Rejected(RejectionSource.PREFERENCES, decoded.reason)
            }
            is LauncherPortablePreferences.DecodeResult.Success -> decoded.preferences
        }

        writer.replacePortableState(workspace, preferences)
        return ApplyResult.Applied(workspace, preferences)
    }
}
