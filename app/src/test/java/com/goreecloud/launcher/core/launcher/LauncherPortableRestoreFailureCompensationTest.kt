package com.goreecloud.launcher.core.launcher

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LauncherPortableRestoreFailureCompensationTest {
    @Test
    fun strictRestoreBaselineReturnsExactCanonicalPreferences() {
        val expected = LauncherPreferences(
            homeColumns = 5,
            homeRows = 6,
            drawerColumns = 4,
            showLabels = false,
            iconScale = 0.95f,
            layoutLocked = true,
            indexHomeMode = GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
        )

        val actual = requireCanonicalPortableRestoreBaseline(
            LauncherPortableRecoveryPreferenceReadResult.Success(expected),
        )

        assertEquals(expected, actual)
    }

    @Test
    fun strictRestoreBaselineRejectsNoncanonicalPersistedPreferences() {
        val reason = "stored portable preferences are outside the canonical recovery domain"

        try {
            requireCanonicalPortableRestoreBaseline(
                LauncherPortableRecoveryPreferenceReadResult.Invalid(reason),
            )
            fail("expected recovery-required failure")
        } catch (failure: LauncherPortableRestoreRecoveryRequiredException) {
            assertTrue(failure.message.orEmpty().contains("noncanonical persisted preferences"))
            assertTrue(failure.message.orEmpty().contains(reason))
        }
    }

    @Test
    fun cancelledApplyCompletesSuspendingRollbackAndRethrowsOriginalCancellation() = runBlocking {
        var workspaceRolledBack = false
        var preferencesRolledBack = false
        var journalCleared = false
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
                    clearJournal = {
                        yield()
                        journalCleared = true
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
        assertTrue(journalCleared)
        assertSame(cancellation, observedFailure)
    }

    @Test
    fun ordinaryApplyFailureIsWrappedOnlyAfterVerifiedRollbackAndJournalCleanup() = runBlocking {
        var workspaceRolledBack = false
        var preferencesRolledBack = false
        var journalCleared = false
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
                clearJournal = {
                    journalCleared = true
                    true
                },
            )
        } catch (failure: Throwable) {
            observedFailure = failure
        }

        assertTrue(workspaceRolledBack)
        assertTrue(preferencesRolledBack)
        assertTrue(journalCleared)
        assertTrue(observedFailure is LauncherPortableRestoreApplyException)
        assertSame(applyFailure, observedFailure?.cause)
    }

    @Test
    fun rollbackFailureKeepsJournalAndAllFailureEvidence() = runBlocking {
        val applyFailure = IllegalArgumentException("apply failed")
        val workspaceFailure = IllegalStateException("workspace rollback failed")
        var journalClearAttempted = false
        var observedFailure: Throwable? = null

        try {
            finishPortableRestoreFailure(
                applyFailure = applyFailure,
                rollbackWorkspace = {
                    throw workspaceFailure
                },
                rollbackPreferences = { false },
                clearJournal = {
                    journalClearAttempted = true
                    true
                },
            )
        } catch (failure: Throwable) {
            observedFailure = failure
        }

        assertFalse(journalClearAttempted)
        assertTrue(observedFailure is LauncherPortableRestoreRollbackException)
        assertSame(applyFailure, observedFailure?.cause)
        assertEquals(2, observedFailure?.suppressed?.size)
        assertSame(workspaceFailure, observedFailure?.suppressed?.first())
    }

    @Test
    fun journalCleanupFailureIsReportedAfterOtherRollbackSucceeds() = runBlocking {
        val applyFailure = IllegalStateException("apply failed")
        var observedFailure: Throwable? = null

        try {
            finishPortableRestoreFailure(
                applyFailure = applyFailure,
                rollbackWorkspace = {},
                rollbackPreferences = { true },
                clearJournal = { false },
            )
        } catch (failure: Throwable) {
            observedFailure = failure
        }

        assertTrue(observedFailure is LauncherPortableRestoreRollbackException)
        assertSame(applyFailure, observedFailure?.cause)
        assertEquals(1, observedFailure?.suppressed?.size)
    }
}
