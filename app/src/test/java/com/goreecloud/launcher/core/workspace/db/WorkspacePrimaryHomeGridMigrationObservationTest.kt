package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspacePrimaryHomeGridMigrationObservationTest {
    @Test
    fun compatibilityPrimaryHomeMapsToReadyNonExecutableEvidence() {
        val state = WorkspacePrimaryHomeGridMigrationObservationMapper.map(
            pages = listOf(primaryPage()),
            items = listOf(compatibilityItem("app:a", 0), compatibilityItem("app:b", 1)),
        )

        val evidence = state as WorkspacePrimaryHomeGridMigrationObservationState.Evidence
        assertEquals(
            WorkspacePrimaryHomeGridMigrationPresentation.Status.READY,
            evidence.presentation.status,
        )
        assertTrue(evidence.presentation.detail.contains("2 apps"))
        assertTrue(evidence.presentation.detail.contains("4 × 1"))
        assertFalse(evidence.presentation.executionAvailable)
    }

    @Test
    fun canonicalSpatialPrimaryHomeMapsToNotNeededEvidence() {
        val state = WorkspacePrimaryHomeGridMigrationObservationMapper.map(
            pages = listOf(primaryPage()),
            items = listOf(
                compatibilityItem("app:a", 0).copy(cellX = 0, cellY = 0),
                compatibilityItem("app:b", 1).copy(cellX = 1, cellY = 0),
            ),
        )

        val evidence = state as WorkspacePrimaryHomeGridMigrationObservationState.Evidence
        assertEquals(
            WorkspacePrimaryHomeGridMigrationPresentation.Status.NOT_NEEDED,
            evidence.presentation.status,
        )
        assertFalse(evidence.presentation.executionAvailable)
    }

    @Test
    fun missingPrimaryHomeFailsClosedAsUnavailable() {
        val state = WorkspacePrimaryHomeGridMigrationObservationMapper.map(
            pages = emptyList(),
            items = emptyList(),
        )

        assertEquals(
            WorkspacePrimaryHomeGridMigrationObservationState.Unavailable(
                WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.MISSING_PRIMARY_PAGE,
            ),
            state,
        )
    }

    private fun primaryPage() = WorkspacePageEntity(
        pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
        containerType = WorkspaceContainerType.HOME,
        rank = 0,
    )

    private fun compatibilityItem(appKey: String, rank: Int) = WorkspaceItemEntity(
        itemId = "legacy:home:$appKey",
        pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
        itemType = WorkspaceItemType.APP,
        appKey = appKey,
        rank = rank,
        cellX = null,
        cellY = null,
    )
}
