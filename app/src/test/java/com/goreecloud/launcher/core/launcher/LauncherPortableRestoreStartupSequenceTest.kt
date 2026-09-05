package com.goreecloud.launcher.core.launcher

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherPortableRestoreStartupSequenceTest {
    @Test
    fun reconciledRecoveryRunsWorkspaceBeforeCallerCanPublishGateOpenResult() = runBlocking {
        val events = mutableListOf<String>()

        val result = LauncherPortableRestoreStartupSequence.reconcileBeforeMutation(
            recoverPortableRestore = {
                events += "portable-recovery"
                LauncherPortableRestoreRecoveryCoordinator.Result.Clean
            },
            reconcileWorkspace = {
                events += "workspace-reconcile"
            },
        )
        events += "publish-gate-result"

        assertEquals(LauncherPortableRestoreRecoveryCoordinator.Result.Clean, result)
        assertEquals(
            listOf("portable-recovery", "workspace-reconcile", "publish-gate-result"),
            events,
        )
    }

    @Test
    fun recoveryRequiredSkipsWorkspaceReconciliation() = runBlocking {
        var workspaceRan = false
        val expected = LauncherPortableRestoreRecoveryCoordinator.Result.RecoveryRequired(
            LauncherPortableRestoreRecoveryCoordinator.RecoveryReason.STATE_MISMATCH,
        )

        val result = LauncherPortableRestoreStartupSequence.reconcileBeforeMutation(
            recoverPortableRestore = { expected },
            reconcileWorkspace = { workspaceRan = true },
        )

        assertEquals(expected, result)
        assertFalse(workspaceRan)
    }

    @Test
    fun workspaceFailurePreventsSuccessfulGateResultFromReturning() = runBlocking {
        var returned = false
        var failed = false

        try {
            LauncherPortableRestoreStartupSequence.reconcileBeforeMutation(
                recoverPortableRestore = {
                    LauncherPortableRestoreRecoveryCoordinator.Result.ConfirmedCommitted
                },
                reconcileWorkspace = {
                    error("workspace reconciliation failed")
                },
            )
            returned = true
        } catch (failure: IllegalStateException) {
            failed = failure.message == "workspace reconciliation failed"
        }

        assertTrue(failed)
        assertFalse(returned)
    }
}
