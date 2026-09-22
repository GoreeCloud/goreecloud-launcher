package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceRelationalReadMapperTest {
    @Test
    fun canonicalRowsReconstructFavoriteAndDockOrder() {
        val expected = WorkspaceLegacyImportMapper.map(
            favoriteKeys = listOf("profile:alpha", "profile:beta"),
            dockKeys = listOf("profile:dock"),
        )

        assertEquals(
            WorkspaceRelationalSnapshot(
                favoriteKeys = listOf("profile:alpha", "profile:beta"),
                dockKeys = listOf("profile:dock"),
            ),
            WorkspaceRelationalReadMapper.map(
                pages = expected.pages.reversed(),
                items = expected.items.reversed(),
            ),
        )
    }

    @Test
    fun spatialPrimaryRowsPreserveFavoriteRankOrder() {
        val expected = WorkspaceLegacyImportMapper.map(
            favoriteKeys = listOf("profile:alpha", "profile:beta"),
            dockKeys = listOf("profile:dock"),
        )
        val spatialItems = expected.items.map { item ->
            when (item.appKey) {
                "profile:alpha" -> item.copy(cellX = 3, cellY = 4)
                "profile:beta" -> item.copy(cellX = 0, cellY = 0)
                else -> item
            }
        }

        assertEquals(
            WorkspaceRelationalSnapshot(
                favoriteKeys = listOf("profile:alpha", "profile:beta"),
                dockKeys = listOf("profile:dock"),
            ),
            WorkspaceRelationalReadMapper.map(
                pages = expected.pages,
                items = spatialItems,
            ),
        )
    }

    @Test
    fun spatialPrimaryRowsFailClosedOnCollisionOrUnsupportedBounds() {
        val expected = WorkspaceLegacyImportMapper.map(
            favoriteKeys = listOf("profile:alpha", "profile:beta"),
            dockKeys = emptyList(),
        )
        val collision = expected.items.map {
            if (it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID) {
                it.copy(cellX = 1, cellY = 1)
            } else {
                it
            }
        }
        val outOfBounds = expected.items.map { item ->
            if (item.appKey == "profile:alpha") item.copy(cellX = 6, cellY = 0)
            else if (item.appKey == "profile:beta") item.copy(cellX = 0, cellY = 0)
            else item
        }

        assertNull(WorkspaceRelationalReadMapper.map(expected.pages, collision))
        assertNull(WorkspaceRelationalReadMapper.map(expected.pages, outOfBounds))
    }

    @Test
    fun nonCanonicalRankFailsClosed() {
        val expected = WorkspaceLegacyImportMapper.map(
            favoriteKeys = listOf("profile:alpha", "profile:beta"),
            dockKeys = emptyList(),
        )
        val malformedItems = expected.items.map { item ->
            if (item.appKey == "profile:beta") item.copy(rank = 4) else item
        }

        assertNull(
            WorkspaceRelationalReadMapper.map(
                pages = expected.pages,
                items = malformedItems,
            )
        )
    }

    @Test
    fun missingCompatibilityPageFailsClosed() {
        val expected = WorkspaceLegacyImportMapper.map(
            favoriteKeys = listOf("profile:alpha"),
            dockKeys = listOf("profile:dock"),
        )

        assertNull(
            WorkspaceRelationalReadMapper.map(
                pages = expected.pages.filterNot {
                    it.pageId == WorkspaceLegacyImportMapper.DOCK_PAGE_ID
                },
                items = expected.items,
            )
        )
    }

    @Test
    fun malformedApplicationRecordFailsClosed() {
        val expected = WorkspaceLegacyImportMapper.map(
            favoriteKeys = listOf("profile:alpha"),
            dockKeys = emptyList(),
        )
        val malformedItems = expected.items.map { it.copy(appKey = null) }

        assertNull(
            WorkspaceRelationalReadMapper.map(
                pages = expected.pages,
                items = malformedItems,
            )
        )
    }
}
