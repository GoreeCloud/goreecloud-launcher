package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspacePrimaryHomeGridMigrationPresentationTest {
    @Test
    fun readyPresentationReportsBoundedPlanWithoutExecutionAuthority() {
        val presentation = WorkspacePrimaryHomeGridMigrationPresenter.present(
            WorkspacePrimaryHomeGridMigrationReadiness.Ready(itemCount = 3, columns = 4, rows = 2),
        )

        assertEquals(WorkspacePrimaryHomeGridMigrationPresentation.Status.READY, presentation.status)
        assertTrue(presentation.detail.contains("3 apps"))
        assertTrue(presentation.detail.contains("4 × 2"))
        assertFalse(presentation.executionAvailable)
    }

    @Test
    fun notNeededStatesRemainNonExecutable() {
        val empty = WorkspacePrimaryHomeGridMigrationPresenter.present(
            WorkspacePrimaryHomeGridMigrationReadiness.NotNeededEmpty,
        )
        val spatial = WorkspacePrimaryHomeGridMigrationPresenter.present(
            WorkspacePrimaryHomeGridMigrationReadiness.NotNeededAlreadySpatial,
        )

        assertEquals(WorkspacePrimaryHomeGridMigrationPresentation.Status.NOT_NEEDED, empty.status)
        assertEquals(WorkspacePrimaryHomeGridMigrationPresentation.Status.NOT_NEEDED, spatial.status)
        assertFalse(empty.executionAvailable)
        assertFalse(spatial.executionAvailable)
    }

    @Test
    fun blockedPresentationDoesNotExposeExecutionPath() {
        val presentation = WorkspacePrimaryHomeGridMigrationPresenter.present(
            WorkspacePrimaryHomeGridMigrationReadiness.Blocked(
                WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_ITEMS,
            ),
        )

        assertEquals(WorkspacePrimaryHomeGridMigrationPresentation.Status.BLOCKED, presentation.status)
        assertTrue(presentation.detail.contains("not canonical"))
        assertFalse(presentation.executionAvailable)
    }
}
