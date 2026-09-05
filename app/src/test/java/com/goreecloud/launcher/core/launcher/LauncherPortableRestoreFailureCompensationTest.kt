package com.goreecloud.launcher.core.launcher

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherPortableRestoreFailureCompensationTest {
    @Test
    fun cancelledApplyCompletesSuspendingRollbackAndRethrowsOriginalCancellation() = runBlocking {
        var workspaceRolledBack = false
        var preferencesRolledBack = false
        var observedFailure: Throwable? = null
        val cancellation = CancellationException("restore cancelled")

        val child = launch(start = CoroutineStart.UNDISPATCHED) {
            coroutineContext.cancel(cancellation)
            try {
                finishPortableRestoreFailure(
                    applyFailure = cancellation,
                    rollbackWorkspace = {
                        yield()
                        workspaceRolledBack = true
                    },
                    rollbackPreferences = {
                        yield()
                        preferencesRolledBack = true
                        true
                    },
                )
            } catch (failure: Throwable) {
                observedFailure = failure
            }
        }
        child.join()

        assertTrue(workspaceRolledBack)
        assertTrue(preferencesRolledBack)
        assertSame(cancellation, observedFailure)
    }

    @Test
    fun ordinaryApplyFailureIsWrappedOnlyAfterVerifiedRollback() = runBlocking {
        var workspaceRolledBack = false
        var preferencesRolledBack = false
        val applyFailure = IllegalStateException("preference readback failed")
        var observedFailure: Throwable? = null

        try {
            finishPortableRestoreFailure(
                applyFailure = applyFailure,
                rollbackWorkspace = {
                    workspaceRolledBack = true
                },
                rollbackPreferences = {
                    preferencesRolledBack = true
                    true
                },
            )
        } catch (failure: Throwable) {
            observedFailure = failure
        }

        assertTrue(workspaceRolledBack)
        assertTrue(preferencesRolledBack)
        assertTrue(observedFailure is LauncherPortableRestoreApplyException)
        assertSame(applyFailure, observedFailure?.cause)
    }

    @Test
    fun rollbackFailureRemainsExplicitAndKeepsAllFailureEvidence() = runBlocking {
        val applyFailure = IllegalArgumentException("apply failed")
        val workspaceFailure = IllegalStateException("workspace rollback failed")
        var observedFailure: Throwable? = null

        try {
            finishPortableRestoreFailure(
                applyFailure = applyFailure,
                rollbackWorkspace = {
                    throw workspaceFailure
                },
                rollbackPreferences = { false },
            )
        } catch (failure: Throwable) {
            observedFailure = failure
        }

        assertTrue(observedFailure is LauncherPortableRestoreRollbackException)
        assertSame(applyFailure, observedFailure?.cause)
        assertEquals(2, observedFailure?.suppressed?.size)
        assertSame(workspaceFailure, observedFailure?.suppressed?.first())
    }
}
