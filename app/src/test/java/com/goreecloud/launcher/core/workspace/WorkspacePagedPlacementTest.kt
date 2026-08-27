package com.goreecloud.launcher.core.workspace

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspacePagedPlacementTest {
    private val grid = WorkspaceGridPlacement.Grid(columns = 4, rows = 5)

    @Test
    fun acceptsDistinctPagesAndOrdersByRank() {
        val pages = listOf(
            page("page-b", 1, placement("app-b", 1, 0)),
            page("page-a", 0, placement("app-a", 0, 0)),
        )

        assertEquals(WorkspacePagedPlacement.Validation.Valid, WorkspacePagedPlacement.validate(grid, pages))
        assertEquals(listOf("page-a", "page-b"), WorkspacePagedPlacement.ordered(pages).map { it.pageId })
    }

    @Test
    fun rejectsDuplicatePageIdentity() {
        val result = WorkspacePagedPlacement.validate(
            grid,
            listOf(page("page-a", 0), page("page-a", 1)),
        )

        assertEquals(WorkspacePagedPlacement.Validation.DuplicatePageId("page-a"), result)
    }

    @Test
    fun rejectsDuplicatePageRank() {
        val result = WorkspacePagedPlacement.validate(
            grid,
            listOf(page("page-a", 0), page("page-b", 0)),
        )

        assertEquals(WorkspacePagedPlacement.Validation.DuplicatePageRank(0), result)
    }

    @Test
    fun rejectsItemIdentityAcrossPages() {
        val result = WorkspacePagedPlacement.validate(
            grid,
            listOf(
                page("page-a", 0, placement("shared-app", 0, 0)),
                page("page-b", 1, placement("shared-app", 2, 2)),
            ),
        )

        assertEquals(
            WorkspacePagedPlacement.Validation.DuplicateItemAcrossPages(
                itemId = "shared-app",
                firstPageId = "page-a",
                secondPageId = "page-b",
            ),
            result,
        )
    }

    @Test
    fun reportsPerPageGridCollision() {
        val result = WorkspacePagedPlacement.validate(
            grid,
            listOf(
                page(
                    "page-a",
                    0,
                    placement("widget", 0, 0, spanX = 2, spanY = 2),
                    placement("app", 1, 1),
                ),
            ),
        )

        assertEquals(
            WorkspacePagedPlacement.Validation.InvalidPage(
                pageId = "page-a",
                reason = WorkspaceGridPlacement.Validation.Collision("widget", "app"),
            ),
            result,
        )
    }

    private fun page(
        pageId: String,
        rank: Int,
        vararg placements: WorkspaceGridPlacement.Placement,
    ) = WorkspacePagedPlacement.Page(pageId, rank, placements.toList())

    private fun placement(
        itemId: String,
        cellX: Int,
        cellY: Int,
        spanX: Int = 1,
        spanY: Int = 1,
    ) = WorkspaceGridPlacement.Placement(itemId, cellX, cellY, spanX, spanY)
}
