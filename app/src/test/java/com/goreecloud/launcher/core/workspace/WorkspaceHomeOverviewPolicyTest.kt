package com.goreecloud.launcher.core.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceHomeOverviewPolicyTest {
    @Test
    fun layoutLockDisablesStructuralMutations() {
        val actions = WorkspaceHomeOverviewPolicy.pageActions(
            pageIndex = 2,
            pageCount = 4,
            isPrimaryPage = false,
            isCompletelyEmpty = true,
            primaryRankHealthy = true,
            layoutLocked = true,
        )

        assertEquals(
            WorkspaceHomeOverviewPolicy.PageActions(false, false, false),
            actions,
        )
        assertFalse(WorkspaceHomeOverviewPolicy.canCreatePage(pageCount = 4, layoutLocked = true))
    }

    @Test
    fun primaryPageCannotMoveOrDelete() {
        val actions = WorkspaceHomeOverviewPolicy.pageActions(
            pageIndex = 0,
            pageCount = 3,
            isPrimaryPage = true,
            isCompletelyEmpty = true,
            primaryRankHealthy = true,
            layoutLocked = false,
        )

        assertEquals(
            WorkspaceHomeOverviewPolicy.PageActions(false, false, false),
            actions,
        )
    }

    @Test
    fun firstSecondaryPageCannotMoveBeforePrimary() {
        val actions = WorkspaceHomeOverviewPolicy.pageActions(
            pageIndex = 1,
            pageCount = 3,
            isPrimaryPage = false,
            isCompletelyEmpty = false,
            primaryRankHealthy = true,
            layoutLocked = false,
        )

        assertFalse(actions.canMoveEarlier)
        assertTrue(actions.canMoveLater)
        assertFalse(actions.canDelete)
    }

    @Test
    fun laterEmptySecondaryPageCanMoveAndDelete() {
        val actions = WorkspaceHomeOverviewPolicy.pageActions(
            pageIndex = 2,
            pageCount = 4,
            isPrimaryPage = false,
            isCompletelyEmpty = true,
            primaryRankHealthy = true,
            layoutLocked = false,
        )

        assertTrue(actions.canMoveEarlier)
        assertTrue(actions.canMoveLater)
        assertTrue(actions.canDelete)
        assertTrue(WorkspaceHomeOverviewPolicy.canCreatePage(pageCount = 4, layoutLocked = false))
    }

    @Test
    fun unhealthyPrimaryRankFailsClosed() {
        val actions = WorkspaceHomeOverviewPolicy.pageActions(
            pageIndex = 2,
            pageCount = 4,
            isPrimaryPage = false,
            isCompletelyEmpty = true,
            primaryRankHealthy = false,
            layoutLocked = false,
        )

        assertEquals(
            WorkspaceHomeOverviewPolicy.PageActions(false, false, false),
            actions,
        )
    }
}
