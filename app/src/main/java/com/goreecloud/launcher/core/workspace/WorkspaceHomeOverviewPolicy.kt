package com.goreecloud.launcher.core.workspace

/**
 * Pure policy for the Home page overview/edit surface.
 *
 * The policy intentionally mirrors the Room mutation safety boundary: the canonical primary page
 * stays at rank zero, destructive deletion is offered only for a completely empty non-primary
 * page, and layout lock disables every structural mutation without blocking read-only page
 * selection.
 */
object WorkspaceHomeOverviewPolicy {
    data class PageActions(
        val canMoveEarlier: Boolean,
        val canMoveLater: Boolean,
        val canDelete: Boolean,
    )

    fun canCreatePage(
        pageCount: Int,
        layoutLocked: Boolean,
    ): Boolean = pageCount > 0 && !layoutLocked

    fun pageActions(
        pageIndex: Int,
        pageCount: Int,
        isPrimaryPage: Boolean,
        isCompletelyEmpty: Boolean,
        primaryRankHealthy: Boolean,
        layoutLocked: Boolean,
    ): PageActions {
        val validIndex = pageIndex in 0 until pageCount
        val mutable = validIndex && pageCount > 0 && !layoutLocked && primaryRankHealthy
        if (!mutable || isPrimaryPage) {
            return PageActions(
                canMoveEarlier = false,
                canMoveLater = false,
                canDelete = false,
            )
        }

        return PageActions(
            // Rank zero is reserved for the canonical primary Home page. A secondary page at
            // index one is already as early as it may safely move.
            canMoveEarlier = pageIndex > 1,
            canMoveLater = pageIndex in 1 until pageCount - 1,
            canDelete = pageCount > 1 && isCompletelyEmpty,
        )
    }
}
