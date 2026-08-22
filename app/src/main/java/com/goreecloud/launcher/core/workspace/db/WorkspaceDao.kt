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
}
