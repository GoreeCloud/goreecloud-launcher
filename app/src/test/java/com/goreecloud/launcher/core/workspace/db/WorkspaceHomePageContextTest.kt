package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspaceHomePageContextTest {
    @Test
    fun compactLabelSummarizesAppsOnly() {
        val page = WorkspaceRenderedHomePage(
            pageId = "home-1",
            rank = 0,
            appKeys = listOf("a", "b"),
            unsupportedItemCount = 0,
        )

        assertEquals("2 apps", page.context().compactLabel)
    }

    @Test
    fun compactLabelKeepsUnsupportedItemsVisible() {
        val page = WorkspaceRenderedHomePage(
            pageId = "home-2",
            rank = 1,
            appKeys = listOf("a"),
            unsupportedItemCount = 2,
        )

        assertEquals("1 app · 2 other", page.context().compactLabel)
    }
}
