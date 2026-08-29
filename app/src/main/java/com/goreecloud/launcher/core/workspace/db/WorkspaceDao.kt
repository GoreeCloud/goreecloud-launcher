package com.goreecloud.launcher.core.workspace.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkspaceDao {
    @Query("SELECT * FROM workspace_pages ORDER BY containerType, rank")
    abstract fun observePages(): Flow<List<WorkspacePageEntity>>

    @Query("SELECT * FROM workspace_items ORDER BY pageId, rank")
    abstract fun observeItems(): Flow<List<WorkspaceItemEntity>>

    @Query("SELECT * FROM workspace_pages WHERE pageId IN (:pageIds)")
    abstract suspend fun readPages(pageIds: List<String>): List<WorkspacePageEntity>

    @Query("SELECT * FROM workspace_pages WHERE containerType = :containerType ORDER BY rank")
    abstract suspend fun readPagesByContainer(containerType: String): List<WorkspacePageEntity>

    @Query("SELECT * FROM workspace_items WHERE pageId IN (:pageIds)")
    abstract suspend fun readItems(pageIds: List<String>): List<WorkspaceItemEntity>

    @Query("SELECT COUNT(*) FROM workspace_items")
    abstract suspend fun itemCount(): Int

    @Upsert
    abstract suspend fun upsertPages(pages: List<WorkspacePageEntity>)

    @Upsert
    abstract suspend fun upsertItems(items: List<WorkspaceItemEntity>)

    @Query("DELETE FROM workspace_pages WHERE pageId IN (:pageIds)")
    protected abstract suspend fun deletePages(pageIds: List<String>)

    @Query("UPDATE workspace_pages SET rank = -(rank + 1) WHERE containerType = :containerType")
    protected abstract suspend fun stagePageRanks(containerType: String)

    @Transaction
    open suspend fun replaceLegacySnapshot(
        pages: List<WorkspacePageEntity>,
        items: List<WorkspaceItemEntity>,
    ) {
        if (pages.isEmpty()) return
        deletePages(pages.map { it.pageId })
        upsertPages(pages)
        if (items.isNotEmpty()) upsertItems(items)
    }

    /**
     * Appends one empty page only when the caller's complete observed container-page snapshot is
     * still current. The page id must be globally unused and its rank must be exactly the next
     * contiguous rank. No child workspace item is created or moved by this transaction.
     */
    @Transaction
    open suspend fun appendPageIfSnapshotMatches(
        containerType: String,
        expectedPages: List<WorkspacePageEntity>,
        newPage: WorkspacePageEntity,
    ): Boolean {
        if (newPage.pageId.isBlank() || newPage.containerType != containerType) return false
        val currentPages = readPagesByContainer(containerType)
        if (currentPages != expectedPages) return false
        if (currentPages.map { it.rank } != currentPages.indices.toList()) return false
        if (newPage.rank != currentPages.size) return false
        if (readPages(listOf(newPage.pageId)).isNotEmpty()) return false

        upsertPages(listOf(newPage))
        return true
    }

    /**
     * Rewrites only page ranks for one container while preserving page rows and all child items.
     * The supplied page identity set must exactly match the current container identity set.
     */
    @Transaction
    open suspend fun replacePageOrder(
        containerType: String,
        orderedPageIds: List<String>,
    ): Boolean {
        if (orderedPageIds.isEmpty() || orderedPageIds.any { it.isBlank() }) return false
        if (orderedPageIds.size != orderedPageIds.distinct().size) return false

        val existing = readPagesByContainer(containerType)
        if (existing.size != orderedPageIds.size) return false
        if (existing.map { it.pageId }.toSet() != orderedPageIds.toSet()) return false

        stagePageRanks(containerType)
        val byId = existing.associateBy { it.pageId }
        upsertPages(
            orderedPageIds.mapIndexed { rank, pageId ->
                checkNotNull(byId[pageId]).copy(rank = rank)
            }
        )
        return true
    }

    /**
     * Applies one item placement write only if the complete HOME page/item snapshot observed by
     * the caller is still current when this transaction executes. This prevents a validated
     * cross-page placement from overwriting a concurrent workspace mutation.
     */
    @Transaction
    open suspend fun replaceItemPlacementIfSnapshotMatches(
        containerType: String,
        expectedPages: List<WorkspacePageEntity>,
        expectedItems: List<WorkspaceItemEntity>,
        updatedItem: WorkspaceItemEntity,
    ): Boolean {
        if (updatedItem.itemId.isBlank() || updatedItem.pageId.isBlank()) return false

        val currentPages = readPagesByContainer(containerType)
        if (currentPages != expectedPages) return false
        val currentPageIds = currentPages.map { it.pageId }
        if (updatedItem.pageId !in currentPageIds) return false

        val currentItems = readItems(currentPageIds)
        val currentById = currentItems.associateBy { it.itemId }
        val expectedById = expectedItems.associateBy { it.itemId }
        if (currentById.size != currentItems.size || expectedById.size != expectedItems.size) return false
        if (currentById != expectedById) return false
        if (updatedItem.itemId !in currentById) return false

        upsertItems(listOf(updatedItem))
        return true
    }
}
