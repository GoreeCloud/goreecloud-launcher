package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspacePrimaryHomeGridMigrationReadinessTest {
    @Test
    fun canonicalCompatibilityPageIsReadyWithDeterministicGridDimensions() {
        val readiness = WorkspacePrimaryHomeGridMigrationReadinessEvaluator.evaluate(
            page = primaryPage(),
            items = (0..4).map { rank -> primaryItem(rank, "app-$rank") },
        )

        assertEquals(
            WorkspacePrimaryHomeGridMigrationReadiness.Ready(
                itemCount = 5,
                columns = 4,
                rows = 2,
            ),
            readiness,
        )
    }

    @Test
    fun emptyPrimaryPageNeedsNoMigration() {
        assertEquals(
            WorkspacePrimaryHomeGridMigrationReadiness.NotNeededEmpty,
            WorkspacePrimaryHomeGridMigrationReadinessEvaluator.evaluate(primaryPage(), emptyList()),
        )
    }

    @Test
    fun alreadySpatialPrimaryPageNeedsNoMigration() {
        val items = listOf(
            primaryItem(0, "app-a").copy(cellX = 0, cellY = 0),
            primaryItem(1, "app-b").copy(cellX = 1, cellY = 0),
        )
        assertEquals(
            WorkspacePrimaryHomeGridMigrationReadiness.NotNeededAlreadySpatial,
            WorkspacePrimaryHomeGridMigrationReadinessEvaluator.evaluate(primaryPage(), items),
        )
    }

    @Test
    fun invalidPrimaryShapeIsBlockedWithoutWriteAuthority() {
        assertEquals(
            WorkspacePrimaryHomeGridMigrationReadiness.Blocked(
                WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_PAGE,
            ),
            WorkspacePrimaryHomeGridMigrationReadinessEvaluator.evaluate(
                primaryPage().copy(pageId = "home:1"),
                listOf(primaryItem(0, "app-a")),
            ),
        )
        assertEquals(
            WorkspacePrimaryHomeGridMigrationReadiness.Blocked(
                WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_ITEMS,
            ),
            WorkspacePrimaryHomeGridMigrationReadinessEvaluator.evaluate(
                primaryPage(),
                listOf(primaryItem(1, "app-a")),
            ),
        )
    }

    private fun primaryPage() = WorkspacePageEntity(
        pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
        containerType = WorkspaceContainerType.HOME,
        rank = 0,
    )

    private fun primaryItem(rank: Int, appKey: String) = WorkspaceItemEntity(
        itemId = "legacy:home:$appKey",
        pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
        itemType = WorkspaceItemType.APP,
        appKey = appKey,
        rank = rank,
        cellX = null,
        cellY = null,
    )
}
