package com.goreecloud.launcher.core.launcher

import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot

/**
 * Minimal combined persistence authority for the two currently approved Launcher portability
 * subsets.
 *
 * A concrete writer receives both already-validated and pair-compatible values in one call so
 * Room/DataStore implementations can later provide a real transaction boundary without this
 * coordinator gaining package/profile discovery, widget rebinding, folder/dock reconstruction, or
 * broader HOME state authority.
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
        COMPATIBILITY,
    }

    sealed interface ValidationResult {
        data class Ready(
            val workspace: WorkspacePortableSnapshot.Snapshot,
            val preferences: LauncherPreferences,
        ) : ValidationResult

        data class Rejected(
            val source: RejectionSource,
            val reason: String,
        ) : ValidationResult
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
     * Decode both complete snapshots and validate their shared Home-grid contract before any
     * persistence authority is granted.
     *
     * The workspace codec intentionally supports a broad framework-independent grid bound while
     * the current Launcher preference format supports the product's reviewed Home grid range. A
     * combined restore must therefore reject two individually valid payloads when their Home-grid
     * dimensions disagree instead of allowing persistence to create contradictory state.
     */
    fun validate(
        workspaceEncoded: String,
        preferencesEncoded: String,
    ): ValidationResult {
        val workspace = when (val decoded = WorkspacePortableSnapshot.decode(workspaceEncoded)) {
            is WorkspacePortableSnapshot.DecodeResult.Invalid -> {
                return ValidationResult.Rejected(RejectionSource.WORKSPACE, decoded.reason)
            }
            is WorkspacePortableSnapshot.DecodeResult.Success -> decoded.snapshot
        }

        val preferences = when (val decoded = LauncherPortablePreferences.decode(preferencesEncoded)) {
            is LauncherPortablePreferences.DecodeResult.Invalid -> {
                return ValidationResult.Rejected(RejectionSource.PREFERENCES, decoded.reason)
            }
            is LauncherPortablePreferences.DecodeResult.Success -> decoded.preferences
        }

        if (
            workspace.grid.columns != preferences.homeColumns ||
            workspace.grid.rows != preferences.homeRows
        ) {
            return ValidationResult.Rejected(
                RejectionSource.COMPATIBILITY,
                "workspace grid ${workspace.grid.columns}x${workspace.grid.rows} does not match " +
                    "portable Home grid ${preferences.homeColumns}x${preferences.homeRows}",
            )
        }

        return ValidationResult.Ready(workspace, preferences)
    }

    /**
     * Validate both complete snapshots and their shared contract before granting any persistence
     * call.
     *
     * If either input is malformed, tampered, expanded, unsupported, or pair-incompatible, the
     * writer is never invoked. Storage failures deliberately propagate so callers cannot report a
     * partial or failed commit as an applied restore.
     */
    suspend fun apply(
        workspaceEncoded: String,
        preferencesEncoded: String,
        writer: LauncherPortableRestoreWriter,
    ): ApplyResult = when (val validation = validate(workspaceEncoded, preferencesEncoded)) {
        is ValidationResult.Rejected -> ApplyResult.Rejected(validation.source, validation.reason)
        is ValidationResult.Ready -> {
            writer.replacePortableState(validation.workspace, validation.preferences)
            ApplyResult.Applied(validation.workspace, validation.preferences)
        }
    }
}
