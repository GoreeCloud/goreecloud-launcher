package com.goreecloud.launcher.core.launcher

import android.content.Context
import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceDao

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
        // Validate the complete preference value before any Room state is changed.
        LauncherPortablePreferences.encode(preferences)
        val previousPreferences = preferencesRepository.readPortablePreferences()
        val roomCommit = workspaceDao.replacePortableHomePlacements(workspace)

        try {
            preferencesRepository.replacePortablePreferences(preferences)
            check(preferencesRepository.readPortablePreferences() == preferences) {
                "portable Launcher preference readback verification failed"
            }
        } catch (applyFailure: Throwable) {
            val rollbackFailures = mutableListOf<Throwable>()

            try {
                workspaceDao.rollbackPortableHomePlacements(roomCommit)
            } catch (rollbackFailure: Throwable) {
                rollbackFailures += rollbackFailure
            }

            try {
                check(
                    preferencesRepository.rollbackPortablePreferencesAfterFailedApply(
                        expectedApplied = preferences,
                        previous = previousPreferences,
                    )
                ) {
                    "portable preference rollback refused because preferences changed after restore apply"
                }
            } catch (rollbackFailure: Throwable) {
                rollbackFailures += rollbackFailure
            }

            if (rollbackFailures.isNotEmpty()) {
                val failure = LauncherPortableRestoreRollbackException(
                    message = "portable Launcher restore failed and compensating rollback could not be fully verified",
                    cause = applyFailure,
                )
                rollbackFailures.forEach(failure::addSuppressed)
                throw failure
            }

            throw LauncherPortableRestoreApplyException(
                message = "portable Launcher restore failed; the previously persisted portable state was restored",
                cause = applyFailure,
            )
        }
    }
}

class LauncherPortableRestoreApplyException(
    message: String,
    cause: Throwable,
) : IllegalStateException(message, cause)

class LauncherPortableRestoreRollbackException(
    message: String,
    cause: Throwable,
) : IllegalStateException(message, cause)
