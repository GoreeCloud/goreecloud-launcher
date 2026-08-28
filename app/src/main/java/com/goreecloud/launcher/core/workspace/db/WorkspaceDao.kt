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
}
