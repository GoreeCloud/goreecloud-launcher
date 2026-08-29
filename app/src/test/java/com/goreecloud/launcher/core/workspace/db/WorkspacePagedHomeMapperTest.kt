package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspacePagedHomeMapperTest {
    @Test
    fun mapsOnlyHomePagesInRankOrderAndCountsUnsupportedItems() {
        val pages = listOf(
            WorkspacePageEntity("home:1", WorkspaceContainerType.HOME, 1),
            WorkspacePageEntity(WorkspaceLegacyImportMapper.DOCK_PAGE_ID, WorkspaceContainerType.DOCK, 0),
            WorkspacePageEntity(WorkspaceLegacyImportMapper.HOME_PAGE_ID, WorkspaceContainerType.HOME, 0),
        )
        val items = listOf(
            WorkspaceItemEntity("item:b", "home:1", WorkspaceItemType.APP, APP_TWO, 1, 1, 0),
            WorkspaceItemEntity("dock:item", WorkspaceLegacyImportMapper.DOCK_PAGE_ID, WorkspaceItemType.APP, DOCK_APP, 0, null, null),
            WorkspaceItemEntity("item:a", "home:1", WorkspaceItemType.APP, APP_ONE, 0, 0, 0),
            WorkspaceItemEntity("item:widget", "home:1", WorkspaceItemType.WIDGET, null, 2, 2, 0),
        )

        val result = WorkspacePagedHomeMapper.map(pages, items)
        assertTrue(result is WorkspacePagedHomeState.Ready)
        val ready = result as WorkspacePagedHomeState.Ready

        assertEquals(
            listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID, "home:1"),
            ready.pages.map { it.pageId },
        )
        assertEquals(emptyList<String>(), ready.pages[0].appKeys)
        assertEquals(listOf(APP_ONE, APP_TWO), ready.pages[1].appKeys)
        assertEquals(1, ready.pages[1].unsupportedItemCount)
    }

    @Test
    fun malformedHomeApplicationFailsClosed() {
        val result = WorkspacePagedHomeMapper.map(
            pages = listOf(WorkspacePageEntity("home:0", WorkspaceContainerType.HOME, 0)),
            items = listOf(
                WorkspaceItemEntity("item:bad", "home:0", WorkspaceItemType.APP, null, 0, 0, 0),
            ),
        )

        assertEquals(
            WorkspacePagedHomeState.RecoveryRequired("MalformedHomeAppItem"),
            result,
        )
    }

    @Test
    fun missingHomePagesFailsClosedInsteadOfInventingAPage() {
        val result = WorkspacePagedHomeMapper.map(
            pages = listOf(WorkspacePageEntity("dock:0", WorkspaceContainerType.DOCK, 0)),
            items = emptyList(),
        )

        assertEquals(
            WorkspacePagedHomeState.RecoveryRequired("MissingHomePages"),
            result,
        )
    }

    private companion object {
        const val APP_ONE = "10:com.example.one/.MainActivity"
        const val APP_TWO = "10:com.example.two/.MainActivity"
        const val DOCK_APP = "10:com.example.dock/.MainActivity"
    }
}
