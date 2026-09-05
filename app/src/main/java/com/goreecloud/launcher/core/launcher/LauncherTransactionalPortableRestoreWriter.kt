package com.goreecloud.launcher.core.launcher

import android.content.Context
import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Concrete Development persistence authority for the currently approved Launcher portability pair.
 *
 * Room and Preferences DataStore cannot share one crash-atomic transaction. This writer therefore
 * gives Room a real transaction boundary, performs the DataStore replacement immediately after it,
 * verifies DataStore readback, and uses guarded compensating rollback if the second store fails.
 * It deliberately does not claim Everkeep recovery, cross-device rebinding, or crash atomicity.
 */
class LauncherTransactionalPortableRestoreWriter(
    private val workspaceDao: WorkspaceDao,
    private val preferencesRepository: LauncherPreferencesRepository,
) : LauncherPortableRestoreWriter {

    constructor(context: Context) : this(
        workspaceDao = LauncherDatabaseProvider.get(context).workspaceDao(),
        preferencesRepository = LauncherPreferencesRepository(context.applicationContext),
    )

    override suspend fun replacePortableState(
        workspace: WorkspacePortableSnapshot.Snapshot,
        preferences: LauncherPreferences,
    ) {
        // Defend the concrete persistence seam as well as the higher-level import coordinator.
        WorkspacePortableSnapshot.encode(workspace)
        LauncherPortablePreferences.encode(preferences)
        check(
            workspace.grid.columns == preferences.homeColumns &&
                workspace.grid.rows == preferences.homeRows
        ) {
            "portable workspace grid must match the portable Home-grid preferences"
        }

        val previousPreferences = preferencesRepository.readPortablePreferences()
        val roomCommit = workspaceDao.replacePortableHomePlacements(workspace)

        try {
            preferencesRepository.replacePortablePreferences(preferences)
            check(preferencesRepository.readPortablePreferences() == preferences) {
                "portable Launcher preference readback verification failed"
            }
        } catch (applyFailure: Throwable) {
            finishPortableRestoreFailure(
                applyFailure = applyFailure,
                rollbackWorkspace = {
                    workspaceDao.rollbackPortableHomePlacements(roomCommit)
                },
                rollbackPreferences = {
                    preferencesRepository.rollbackPortablePreferencesAfterFailedApply(
                        expectedApplied = preferences,
                        previous = previousPreferences,
                    )
                },
            )
        }
    }
}

/**
 * Complete compensating rollback even when the apply coroutine has already been cancelled.
 *
 * Cancellation can arrive after the Room commit but before the DataStore stage finishes. Running
 * rollback in the cancelled context would let the first suspension abort compensation and strand a
 * split cross-store state. NonCancellable is intentionally scoped only to the bounded rollback.
 * Once rollback verifies, the original CancellationException is rethrown unchanged so callers keep
 * normal structured-concurrency semantics rather than receiving a misleading ordinary apply error.
 */
internal suspend fun finishPortableRestoreFailure(
    applyFailure: Throwable,
    rollbackWorkspace: suspend () -> Unit,
    rollbackPreferences: suspend () -> Boolean,
): Nothing {
    val rollbackFailures = withContext(NonCancellable) {
        buildList {
            try {
                rollbackWorkspace()
            } catch (rollbackFailure: Throwable) {
                add(rollbackFailure)
            }

            try {
                check(rollbackPreferences()) {
                    "portable preference rollback refused because preferences changed after restore apply"
                }
            } catch (rollbackFailure: Throwable) {
                add(rollbackFailure)
            }
        }
    }

    if (rollbackFailures.isNotEmpty()) {
        val failure = LauncherPortableRestoreRollbackException(
            message = "portable Launcher restore failed and compensating rollback could not be fully verified",
            cause = applyFailure,
        )
        rollbackFailures.forEach(failure::addSuppressed)
        throw failure
    }

    if (applyFailure is CancellationException) {
        throw applyFailure
    }

    throw LauncherPortableRestoreApplyException(
        message = "portable Launcher restore failed; the previously persisted portable state was restored",
        cause = applyFailure,
    )
}

class LauncherPortableRestoreApplyException(
    message: String,
    cause: Throwable,
) : IllegalStateException(message, cause)

class LauncherPortableRestoreRollbackException(
    message: String,
    cause: Throwable,
) : IllegalStateException(message, cause)
