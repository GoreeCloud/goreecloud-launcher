package com.goreecloud.launcher.core.workspace.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement

/**
 * Opaque one-level rollback checkpoint for an atomic secondary-HOME batch move.
 *
 * Construction is internal so callers outside the launcher workspace implementation cannot forge
 * arbitrary before/after snapshots. The move service creates a commit only after validating the
 * complete Room-authoritative HOME snapshot and deterministic target placements.
 */
data class WorkspaceHomeBatchMoveCommit internal constructor(
    val sourcePageId: String,
    val targetPageId: String,
    val movedItemIds: List<String>,
    val previousPages: List<WorkspacePageEntity>,
    val previousItems: List<WorkspaceItemEntity>,
    val appliedItems: List<WorkspaceItemEntity>,
)

/**
 * Transaction boundary dedicated to all-or-nothing secondary HOME batch movement, exact-state
 * rollback, and bounded secondary-page compaction. It deliberately does not broaden the portable
 * backup schema or create a second workspace authority: Room remains the only post-cutover source
 * of truth.
 */
@Dao
abstract class WorkspaceHomeBatchMoveDao {
    @Query("SELECT * FROM workspace_pages WHERE containerType = :containerType ORDER BY rank")
    protected abstract suspend fun readPagesByContainer(containerType: String): List<WorkspacePageEntity>

    @Query(
        "SELECT workspace_items.* FROM workspace_items " +
            "INNER JOIN workspace_pages ON workspace_pages.pageId = workspace_items.pageId " +
            "WHERE workspace_pages.containerType = :containerType " +
            "ORDER BY workspace_pages.rank, workspace_items.rank, workspace_items.itemId"
    )
    protected abstract suspend fun readItemsByContainer(containerType: String): List<WorkspaceItemEntity>

    @Upsert
    protected abstract suspend fun upsertItems(items: List<WorkspaceItemEntity>)

    /**
     * Apply every changed item in one Room transaction only when the complete observed HOME page
     * and item state still exactly matches the commit's previous snapshot. Any SQL/readback failure
     * throws inside the transaction, causing Room to roll back the entire batch.
     */
    @Transaction
    open suspend fun applyIfSnapshotMatches(commit: WorkspaceHomeBatchMoveCommit): Boolean {
        if (!commit.isStructurallyValid()) return false

        val currentPages = readPagesByContainer(WorkspaceContainerType.HOME)
        if (currentPages != commit.previousPages) return false
        val currentItems = readItemsByContainer(WorkspaceContainerType.HOME).canonicalBatchItems()
        if (currentItems != commit.previousItems.canonicalBatchItems()) return false

        val previousById = commit.previousItems.associateBy { it.itemId }
        val appliedById = commit.appliedItems.associateBy { it.itemId }
        val changed = commit.movedItemIds.map { itemId -> checkNotNull(appliedById[itemId]) }
        if (changed.isEmpty()) return false

        upsertItems(changed)

        val appliedReadback = readItemsByContainer(WorkspaceContainerType.HOME).canonicalBatchItems()
        check(appliedReadback == commit.appliedItems.canonicalBatchItems()) {
            "atomic HOME batch move readback verification failed"
        }
        check(previousById.keys == appliedById.keys) {
            "atomic HOME batch move changed the item identity set"
        }
        return true
    }

    /**
     * Restore the exact pre-move item snapshot only while the complete HOME state still matches the
     * batch's applied snapshot. Any intervening HOME mutation makes rollback fail closed rather than
     * overwriting newer user state.
     */
    @Transaction
    open suspend fun rollbackIfSnapshotMatches(commit: WorkspaceHomeBatchMoveCommit): Boolean {
        if (!commit.isStructurallyValid()) return false

        val currentPages = readPagesByContainer(WorkspaceContainerType.HOME)
        if (currentPages != commit.previousPages) return false
        val currentItems = readItemsByContainer(WorkspaceContainerType.HOME).canonicalBatchItems()
        if (currentItems != commit.appliedItems.canonicalBatchItems()) return false

        val previousById = commit.previousItems.associateBy { it.itemId }
        val restored = commit.movedItemIds.map { itemId -> checkNotNull(previousById[itemId]) }
        if (restored.isEmpty()) return false

        upsertItems(restored)

        val restoredReadback = readItemsByContainer(WorkspaceContainerType.HOME).canonicalBatchItems()
        check(restoredReadback == commit.previousItems.canonicalBatchItems()) {
            "atomic HOME batch move rollback readback verification failed"
        }
        return true
    }

    /**
     * Apply a complete deterministic replacement of one secondary HOME page's item placements only
     * while the complete HOME page/item snapshot still matches what the caller planned against.
     * This keeps page cleanup atomic and fail-closed without introducing a second history store.
     */
    @Transaction
    open suspend fun compactPageIfSnapshotMatches(
        pageId: String,
        expectedPages: List<WorkspacePageEntity>,
        expectedItems: List<WorkspaceItemEntity>,
        compactedItems: List<WorkspaceItemEntity>,
    ): Boolean {
        if (pageId.isBlank() || pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID) return false
        if (compactedItems.isEmpty()) return false
        if (expectedPages.isEmpty()) return false
        if (expectedPages.map { it.rank } != expectedPages.indices.toList()) return false
        if (expectedPages.firstOrNull()?.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID) return false
        if (expectedPages.none { it.pageId == pageId }) return false

        val currentPages = readPagesByContainer(WorkspaceContainerType.HOME)
        if (currentPages != expectedPages) return false
        val currentItems = readItemsByContainer(WorkspaceContainerType.HOME).canonicalBatchItems()
        val expectedCanonical = expectedItems.canonicalBatchItems()
        if (currentItems != expectedCanonical) return false

        val previousPageItems = expectedCanonical.filter { it.pageId == pageId }
        val previousById = previousPageItems.associateBy { it.itemId }
        val compactedById = compactedItems.associateBy { it.itemId }
        if (
            previousById.size != previousPageItems.size ||
            compactedById.size != compactedItems.size ||
            previousById.keys != compactedById.keys
        ) {
            return false
        }
        if (
            compactedItems.any {
                it.pageId != pageId ||
                    it.itemType != WorkspaceItemType.APP ||
                    it.appKey.isNullOrBlank() ||
                    it.cellX == null ||
                    it.cellY == null ||
                    checkNotNull(it.cellX) < 0 ||
                    checkNotNull(it.cellY) < 0 ||
                    it.spanX != 1 ||
                    it.spanY != 1 ||
                    it.rank < 0
            }
        ) {
            return false
        }
        if (compactedItems.map { it.rank }.distinct().size != compactedItems.size) return false

        val applied = expectedCanonical.map { item -> compactedById[item.itemId] ?: item }.canonicalBatchItems()
        if (applied == expectedCanonical) return false

        upsertItems(compactedItems)

        val appliedReadback = readItemsByContainer(WorkspaceContainerType.HOME).canonicalBatchItems()
        check(appliedReadback == applied) {
            "secondary HOME page compaction readback verification failed"
        }
        return true
    }

    private fun WorkspaceHomeBatchMoveCommit.isStructurallyValid(): Boolean {
        if (
            sourcePageId.isBlank() ||
            targetPageId.isBlank() ||
            sourcePageId == targetPageId ||
            sourcePageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID ||
            targetPageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        ) {
            return false
        }
        if (movedItemIds.isEmpty() || movedItemIds.any { it.isBlank() }) return false
        if (movedItemIds.size != movedItemIds.distinct().size) return false
        if (previousPages.isEmpty()) return false
        if (previousPages.map { it.rank } != previousPages.indices.toList()) return false
        if (previousPages.firstOrNull()?.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID) return false
        val pageIds = previousPages.map { it.pageId }.toSet()
        if (sourcePageId !in pageIds || targetPageId !in pageIds) return false

        val previousCanonical = previousItems.canonicalBatchItems()
        val appliedCanonical = appliedItems.canonicalBatchItems()
        val previousById = previousCanonical.associateBy { it.itemId }
        val appliedById = appliedCanonical.associateBy { it.itemId }
        if (previousById.size != previousCanonical.size || appliedById.size != appliedCanonical.size) return false
        if (previousById.keys != appliedById.keys) return false
        if (!movedItemIds.all { it in previousById }) return false
        if (previousCanonical.any { it.pageId !in pageIds }) return false
        if (appliedCanonical.any { it.pageId !in pageIds }) return false

        val changedIds = previousById.keys.filterTo(mutableSetOf()) { itemId ->
            previousById[itemId] != appliedById[itemId]
        }
        if (changedIds != movedItemIds.toSet()) return false
        if (movedItemIds.any { itemId ->
                val previous = checkNotNull(previousById[itemId])
                val applied = checkNotNull(appliedById[itemId])
                previous.pageId != sourcePageId ||
                    applied.pageId != targetPageId ||
                    previous.itemType != WorkspaceItemType.APP ||
                    previous.appKey.isNullOrBlank() ||
                    applied.itemType != previous.itemType ||
                    applied.appKey != previous.appKey
            }
        ) {
            return false
        }

        if (!previousCanonical.hasValidSecondarySpatialGeometry()) return false
        if (!appliedCanonical.hasValidSecondarySpatialGeometry()) return false
        if (!appliedCanonical.hasUniquePageRanks()) return false
        return true
    }

    private fun List<WorkspaceItemEntity>.canonicalBatchItems(): List<WorkspaceItemEntity> =
        sortedWith(compareBy({ it.pageId }, { it.rank }, { it.itemId }))

    private fun List<WorkspaceItemEntity>.hasUniquePageRanks(): Boolean =
        groupBy { it.pageId }.values.all { pageItems ->
            pageItems.map { it.rank }.let { ranks ->
                ranks.all { it >= 0 } && ranks.size == ranks.distinct().size
            }
        }

    private fun List<WorkspaceItemEntity>.hasValidSecondarySpatialGeometry(): Boolean {
        val spatial = filterNot { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
        if (spatial.any {
                it.cellX == null ||
                    it.cellY == null ||
                    checkNotNull(it.cellX) < 0 ||
                    checkNotNull(it.cellY) < 0 ||
                    it.spanX <= 0 ||
                    it.spanY <= 0
            }
        ) {
            return false
        }

        return spatial.groupBy { it.pageId }.values.all { pageItems ->
            if (pageItems.isEmpty()) return@all true
            val columns = pageItems.maxOf { checkNotNull(it.cellX) + it.spanX }
            val rows = pageItems.maxOf { checkNotNull(it.cellY) + it.spanY }
            if (columns <= 0 || rows <= 0) return@all false
            val grid = WorkspaceGridPlacement.Grid(columns = columns, rows = rows)
            val placements = pageItems.map { item ->
                WorkspaceGridPlacement.Placement(
                    itemId = item.itemId,
                    cellX = checkNotNull(item.cellX),
                    cellY = checkNotNull(item.cellY),
                    spanX = item.spanX,
                    spanY = item.spanY,
                )
            }
            WorkspaceGridPlacement.validate(grid, placements) == WorkspaceGridPlacement.Validation.Valid
        }
    }
}
