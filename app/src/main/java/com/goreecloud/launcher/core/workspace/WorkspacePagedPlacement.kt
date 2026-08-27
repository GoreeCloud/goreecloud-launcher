package com.goreecloud.launcher.core.workspace

/**
 * Multi-page placement contract layered on [WorkspaceGridPlacement].
 *
 * This remains Android-framework independent so page identity, rank ordering, global item
 * uniqueness, and per-page grid validity can be enforced before Room persistence or rendering.
 */
object WorkspacePagedPlacement {
    data class Page(
        val pageId: String,
        val rank: Int,
        val placements: List<WorkspaceGridPlacement.Placement>,
    ) {
        init {
            require(pageId.isNotBlank()) { "pageId must not be blank" }
            require(rank >= 0) { "page rank must be non-negative" }
        }
    }

    sealed interface Validation {
        data object Valid : Validation
        data class DuplicatePageId(val pageId: String) : Validation
        data class DuplicatePageRank(val rank: Int) : Validation
        data class DuplicateItemAcrossPages(
            val itemId: String,
            val firstPageId: String,
            val secondPageId: String,
        ) : Validation
        data class InvalidPage(
            val pageId: String,
            val reason: WorkspaceGridPlacement.Validation,
        ) : Validation
    }

    fun validate(
        grid: WorkspaceGridPlacement.Grid,
        pages: List<Page>,
    ): Validation {
        val pageIds = mutableSetOf<String>()
        val pageRanks = mutableSetOf<Int>()
        val itemPages = mutableMapOf<String, String>()

        for (page in pages) {
            if (!pageIds.add(page.pageId)) return Validation.DuplicatePageId(page.pageId)
            if (!pageRanks.add(page.rank)) return Validation.DuplicatePageRank(page.rank)

            val pageValidation = WorkspaceGridPlacement.validate(grid, page.placements)
            if (pageValidation != WorkspaceGridPlacement.Validation.Valid) {
                return Validation.InvalidPage(page.pageId, pageValidation)
            }

            for (placement in page.placements) {
                val firstPageId = itemPages.putIfAbsent(placement.itemId, page.pageId)
                if (firstPageId != null) {
                    return Validation.DuplicateItemAcrossPages(
                        itemId = placement.itemId,
                        firstPageId = firstPageId,
                        secondPageId = page.pageId,
                    )
                }
            }
        }

        return Validation.Valid
    }

    fun ordered(pages: List<Page>): List<Page> = pages.sortedBy { it.rank }
}
