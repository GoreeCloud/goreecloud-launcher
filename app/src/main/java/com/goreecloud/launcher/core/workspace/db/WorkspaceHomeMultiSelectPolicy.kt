package com.goreecloud.launcher.core.workspace.db

/**
 * Pure selection policy for bounded multi-select HOME editing.
 *
 * Multi-select currently operates only on application items rendered on non-primary HOME pages.
 * The protected primary compatibility page stays outside this editing path until the separately
 * accepted primary-grid migration is complete. Selection order always follows the authoritative
 * rendered page order rather than tap order so a group move is deterministic.
 */
object WorkspaceHomeMultiSelectPolicy {
    fun targetPages(
        pages: List<WorkspaceRenderedHomePage>,
        sourcePageId: String,
    ): List<WorkspaceRenderedHomePage> = pages
        .sortedBy { it.rank }
        .filterNot { page ->
            page.pageId == sourcePageId ||
                page.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        }

    fun canSelectApps(
        page: WorkspaceRenderedHomePage,
        pages: List<WorkspaceRenderedHomePage>,
        layoutLocked: Boolean,
    ): Boolean =
        !layoutLocked &&
            page.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID &&
            page.appKeys.isNotEmpty() &&
            targetPages(pages, page.pageId).isNotEmpty()

    fun orderedSelection(
        page: WorkspaceRenderedHomePage,
        selectedKeys: Set<String>,
    ): List<String> = page.appKeys
        .asSequence()
        .filter { it in selectedKeys }
        .distinct()
        .toList()
}
