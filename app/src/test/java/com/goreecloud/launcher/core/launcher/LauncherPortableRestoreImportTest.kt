package com.goreecloud.launcher.core.launcher

import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspacePagedPlacement
import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LauncherPortableRestoreImportTest {
    private val workspace = WorkspacePortableSnapshot.Snapshot(
        grid = WorkspaceGridPlacement.Grid(columns = 4, rows = 5),
        pages = listOf(
            WorkspacePagedPlacement.Page(
                pageId = "home",
                rank = 0,
                placements = listOf(
                    WorkspaceGridPlacement.Placement(
                        itemId = "profile:10/com.example.notes",
                        cellX = 0,
                        cellY = 0,
                    )
                ),
            )
        ),
    )

    private val preferences = LauncherPreferences(
        homeColumns = 4,
        homeRows = 5,
        drawerColumns = 5,
        showLabels = true,
        iconScale = 1.0f,
        layoutLocked = false,
        indexHomeMode = GoreeCloudIndexHomeMode.PERMANENT,
    )

    private data class Write(
        val workspace: WorkspacePortableSnapshot.Snapshot,
        val preferences: LauncherPreferences,
    )

    private class RecordingWriter : LauncherPortableRestoreWriter {
        val writes = mutableListOf<Write>()

        override suspend fun replacePortableState(
            workspace: WorkspacePortableSnapshot.Snapshot,
            preferences: LauncherPreferences,
        ) {
            writes += Write(workspace, preferences)
        }
    }

    @Test
    fun twoValidSnapshotsProduceExactlyOneCombinedWrite() = runBlocking {
        val writer = RecordingWriter()

        val result = LauncherPortableRestoreImport.apply(
            workspaceEncoded = WorkspacePortableSnapshot.encode(workspace),
            preferencesEncoded = LauncherPortablePreferences.encode(preferences),
            writer = writer,
        )

        assertTrue(result is LauncherPortableRestoreImport.ApplyResult.Applied)
        assertEquals(listOf(Write(workspace, preferences)), writer.writes)
    }

    @Test
    fun invalidWorkspacePerformsZeroCombinedWrites() = runBlocking {
        val writer = RecordingWriter()
        val tamperedWorkspace = WorkspacePortableSnapshot.encode(workspace)
            .replaceFirst("item=", "item=X")

        val result = LauncherPortableRestoreImport.apply(
            workspaceEncoded = tamperedWorkspace,
            preferencesEncoded = LauncherPortablePreferences.encode(preferences),
            writer = writer,
        )

        assertTrue(result is LauncherPortableRestoreImport.ApplyResult.Rejected)
        val rejected = result as LauncherPortableRestoreImport.ApplyResult.Rejected
        assertEquals(LauncherPortableRestoreImport.RejectionSource.WORKSPACE, rejected.source)
        assertTrue(writer.writes.isEmpty())
    }

    @Test
    fun invalidPreferencesPerformZeroCombinedWritesAfterWorkspaceValidation() = runBlocking {
        val writer = RecordingWriter()
        val tamperedPreferences = LauncherPortablePreferences.encode(preferences)
            .replace("show_labels=true", "show_labels=false")

        val result = LauncherPortableRestoreImport.apply(
            workspaceEncoded = WorkspacePortableSnapshot.encode(workspace),
            preferencesEncoded = tamperedPreferences,
            writer = writer,
        )

        assertTrue(result is LauncherPortableRestoreImport.ApplyResult.Rejected)
        val rejected = result as LauncherPortableRestoreImport.ApplyResult.Rejected
        assertEquals(LauncherPortableRestoreImport.RejectionSource.PREFERENCES, rejected.source)
        assertTrue(writer.writes.isEmpty())
    }

    @Test
    fun persistenceFailureIsNeverReportedAsApplied() {
        val writer = object : LauncherPortableRestoreWriter {
            override suspend fun replacePortableState(
                workspace: WorkspacePortableSnapshot.Snapshot,
                preferences: LauncherPreferences,
            ) {
                throw IllegalStateException("synthetic combined persistence failure")
            }
        }

        try {
            runBlocking {
                LauncherPortableRestoreImport.apply(
                    workspaceEncoded = WorkspacePortableSnapshot.encode(workspace),
                    preferencesEncoded = LauncherPortablePreferences.encode(preferences),
                    writer = writer,
                )
            }
            fail("persistence failure must propagate")
        } catch (expected: IllegalStateException) {
            assertEquals("synthetic combined persistence failure", expected.message)
        }
    }
}
