package com.goreecloud.launcher.core.workspace.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceHomeMultiSelectPolicyTest {
    private val primary = WorkspaceRenderedHomePage(
        pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
        rank = 0,
        appKeys = listOf("primary/app"),
        unsupportedItemCount = 0,
    )
    private val pageTwo = WorkspaceRenderedHomePage(
        pageId = "home:user:two",
        rank = 1,
        appKeys = listOf("app/c", "app/a", "app/b"),
        unsupportedItemCount = 0,
    )
    private val pageThree = WorkspaceRenderedHomePage(
        pageId = "home:user:three",
        rank = 2,
        appKeys = listOf("app/d"),
        unsupportedItemCount = 0,
    )

    @Test
    fun primaryPageCannotEnterMultiSelect() {
        val pages = listOf(primary, pageTwo, pageThree)

        assertFalse(
            WorkspaceHomeMultiSelectPolicy.canSelectApps(
                page = primary,
                pages = pages,
                layoutLocked = false,
            )
        )
    }

    @Test
    fun layoutLockDisablesSecondaryMultiSelect() {
        val pages = listOf(primary, pageTwo, pageThree)

        assertFalse(
            WorkspaceHomeMultiSelectPolicy.canSelectApps(
                page = pageTwo,
                pages = pages,
                layoutLocked = true,
            )
        )
    }

    @Test
    fun targetPagesExcludePrimaryAndSource() {
        val targets = WorkspaceHomeMultiSelectPolicy.targetPages(
            pages = listOf(pageThree, primary, pageTwo),
            sourcePageId = pageTwo.pageId,
        )

        assertEquals(listOf(pageThree), targets)
        assertTrue(
            WorkspaceHomeMultiSelectPolicy.canSelectApps(
                page = pageTwo,
                pages = listOf(primary, pageTwo, pageThree),
                layoutLocked = false,
            )
        )
    }

    @Test
    fun orderedSelectionUsesAuthoritativePageOrderAndDropsStaleKeys() {
        val selection = WorkspaceHomeMultiSelectPolicy.orderedSelection(
            page = pageTwo.copy(appKeys = listOf("app/c", "app/a", "app/c", "app/b")),
            selectedKeys = setOf("app/b", "app/c", "missing/app"),
        )

        assertEquals(listOf("app/c", "app/b"), selection)
    }

    @Test
    fun selectionRequiresAnotherSecondaryTarget() {
        assertFalse(
            WorkspaceHomeMultiSelectPolicy.canSelectApps(
                page = pageTwo,
                pages = listOf(primary, pageTwo),
                layoutLocked = false,
            )
        )
    }
}
