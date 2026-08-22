package com.goreecloud.launcher.core.workspace.db

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.launcher.core.workspace.WorkspaceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkspaceRoomRuntimeTest {
    private lateinit var context: Context
    private lateinit var database: LauncherDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
        database = openDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun mirrorPersistsReopensReplacesAndClearsCompatibilitySnapshot() = runBlocking {
        val firstState = WorkspaceState(
            initialized = true,
            favoriteKeys = listOf(FAVORITE_ALPHA, FAVORITE_BETA, FAVORITE_GAMMA),
            dockKeys = listOf(DOCK_ALPHA, DOCK_BETA),
        )

        assertEquals(
            WorkspaceMirrorResult.Verified,
            WorkspaceRelationalMirror(database.workspaceDao()).sync(firstState),
        )
        assertSnapshot(
            expectedFavorites = firstState.favoriteKeys,
            expectedDock = firstState.dockKeys,
        )

        database.close()
        database = openDatabase()
        assertSnapshot(
            expectedFavorites = firstState.favoriteKeys,
            expectedDock = firstState.dockKeys,
        )

        val secondState = WorkspaceState(
            initialized = true,
            favoriteKeys = listOf(FAVORITE_GAMMA, FAVORITE_ALPHA),
            dockKeys = listOf(DOCK_BETA),
        )

        assertEquals(
            WorkspaceMirrorResult.Verified,
            WorkspaceRelationalMirror(database.workspaceDao()).sync(secondState),
        )
        assertSnapshot(
            expectedFavorites = secondState.favoriteKeys,
            expectedDock = secondState.dockKeys,
        )

        val replacedKeys = database.workspaceDao()
            .readItems(COMPATIBILITY_PAGE_IDS)
            .mapNotNull { it.appKey }
            .toSet()
        assertFalse(replacedKeys.contains(FAVORITE_BETA))
        assertFalse(replacedKeys.contains(DOCK_ALPHA))

        val emptyState = WorkspaceState(initialized = true)
        assertEquals(
            WorkspaceMirrorResult.Verified,
            WorkspaceRelationalMirror(database.workspaceDao()).sync(emptyState),
        )
        assertSnapshot(expectedFavorites = emptyList(), expectedDock = emptyList())
    }

    private suspend fun assertSnapshot(
        expectedFavorites: List<String>,
        expectedDock: List<String>,
    ) {
        val dao = database.workspaceDao()
        val pages = dao.readPages(COMPATIBILITY_PAGE_IDS)
        assertEquals(COMPATIBILITY_PAGE_IDS.toSet(), pages.map { it.pageId }.toSet())

        val items = dao.readItems(COMPATIBILITY_PAGE_IDS)
        val actualFavorites = items
            .filter { it.pageId == HOME_PAGE_ID }
            .sortedBy { it.rank }
            .mapNotNull { it.appKey }
        val actualDock = items
            .filter { it.pageId == DOCK_PAGE_ID }
            .sortedBy { it.rank }
            .mapNotNull { it.appKey }

        assertEquals(expectedFavorites, actualFavorites)
        assertEquals(expectedDock, actualDock)
        assertEquals(expectedFavorites.size + expectedDock.size, dao.itemCount())
    }

    private fun openDatabase(): LauncherDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            LauncherDatabase::class.java,
            DATABASE_NAME,
        )
            .setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    private companion object {
        const val DATABASE_NAME = "launcher-workspace-runtime-test.db"
        const val HOME_PAGE_ID = "home:0"
        const val DOCK_PAGE_ID = "dock:0"
        val COMPATIBILITY_PAGE_IDS = listOf(HOME_PAGE_ID, DOCK_PAGE_ID)

        const val FAVORITE_ALPHA = "10:com.example.alpha/.MainActivity"
        const val FAVORITE_BETA = "10:com.example.beta/.MainActivity"
        const val FAVORITE_GAMMA = "10:com.example.gamma/.MainActivity"
        const val DOCK_ALPHA = "10:com.example.dockalpha/.MainActivity"
        const val DOCK_BETA = "10:com.example.dockbeta/.MainActivity"
    }
}
