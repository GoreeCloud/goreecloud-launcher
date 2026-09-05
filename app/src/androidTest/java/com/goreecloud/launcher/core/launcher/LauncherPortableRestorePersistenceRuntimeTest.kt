package com.goreecloud.launcher.core.launcher

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspacePagedPlacement
import com.goreecloud.launcher.core.workspace.WorkspacePortableSnapshot
import com.goreecloud.launcher.core.workspace.db.LauncherDatabase
import com.goreecloud.launcher.core.workspace.db.WorkspaceContainerType
import com.goreecloud.launcher.core.workspace.db.WorkspaceItemEntity
import com.goreecloud.launcher.core.workspace.db.WorkspaceItemType
import com.goreecloud.launcher.core.workspace.db.WorkspacePageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherPortableRestorePersistenceRuntimeTest {
    private lateinit var context: Context
    private lateinit var database: LauncherDatabase
    private lateinit var preferencesRepository: LauncherPreferencesRepository
    private lateinit var originalPreferences: LauncherPreferences

    @Before
    fun setUp() = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
        database = Room.databaseBuilder(
            context.applicationContext,
            LauncherDatabase::class.java,
            DATABASE_NAME,
        )
            .setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        preferencesRepository = LauncherPreferencesRepository(context.applicationContext)
        originalPreferences = preferencesRepository.readPortablePreferences()
    }

    @After
    fun tearDown() {
        runBlocking {
            preferencesRepository.replacePortablePreferences(originalPreferences)
            database.close()
            context.deleteDatabase(DATABASE_NAME)
        }
    }

    @Test
    fun roomPlacementRestoreIsTransactionalAndRollbackRestoresPreviousHomeState() = runBlocking {
        val dao = database.workspaceDao()
        seedTwoHomeAppsAndDock()
        val previousPages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
        val previousItems = dao.readItemsByContainer(WorkspaceContainerType.HOME)

        val commit = dao.replacePortableHomePlacements(restoredWorkspace())

        assertEquals(listOf("restored:0"), dao.readPagesByContainer(WorkspaceContainerType.HOME).map { it.pageId })
        val restoredItems = dao.readItemsByContainer(WorkspaceContainerType.HOME)
        assertEquals(listOf(APP_B, APP_A), restoredItems.map { it.itemId })
        assertEquals(listOf(0, 1), restoredItems.map { it.rank })
        assertEquals(listOf(0, 1), restoredItems.map { it.cellX })
        assertEquals(listOf(APP_KEY_B, APP_KEY_A), restoredItems.map { it.appKey })
        assertEquals(listOf(DOCK_PAGE), dao.readPagesByContainer(WorkspaceContainerType.DOCK).map { it.pageId })

        dao.rollbackPortableHomePlacements(commit)

        assertEquals(previousPages, dao.readPagesByContainer(WorkspaceContainerType.HOME))
        assertEquals(previousItems, dao.readItemsByContainer(WorkspaceContainerType.HOME))
        assertEquals(listOf(DOCK_PAGE), dao.readPagesByContainer(WorkspaceContainerType.DOCK).map { it.pageId })
    }

    @Test
    fun cleanTargetCannotInventMissingAppIdentityRebinding() = runBlocking {
        val dao = database.workspaceDao()
        dao.upsertPages(
            listOf(
                WorkspacePageEntity(
                    pageId = HOME_PAGE_A,
                    containerType = WorkspaceContainerType.HOME,
                    rank = 0,
                )
            )
        )

        try {
            dao.replacePortableHomePlacements(restoredWorkspace())
            fail("non-empty portable restore must not invent app/package/profile identities on a clean target")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message.orEmpty().contains("cannot be rebound"))
        }

        assertEquals(listOf(HOME_PAGE_A), dao.readPagesByContainer(WorkspaceContainerType.HOME).map { it.pageId })
        assertTrue(dao.readItemsByContainer(WorkspaceContainerType.HOME).isEmpty())
    }

    @Test
    fun concreteWriterAppliesCompatibleRoomAndDataStorePair() = runBlocking {
        val dao = database.workspaceDao()
        seedTwoHomeAppsAndDock()
        val before = LauncherPreferences(
            homeColumns = 4,
            homeRows = 5,
            drawerColumns = 5,
            showLabels = true,
            iconScale = 1.0f,
            layoutLocked = false,
            indexHomeMode = GoreeCloudIndexHomeMode.PERMANENT,
        )
        val target = before.copy(
            drawerColumns = 4,
            showLabels = false,
            iconScale = 0.95f,
            layoutLocked = true,
            indexHomeMode = GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
        )
        preferencesRepository.replacePortablePreferences(before)

        LauncherTransactionalPortableRestoreWriter(dao, preferencesRepository)
            .replacePortableState(restoredWorkspace(), target)

        assertEquals(target, preferencesRepository.readPortablePreferences())
        assertEquals(listOf("restored:0"), dao.readPagesByContainer(WorkspaceContainerType.HOME).map { it.pageId })
        assertEquals(listOf(APP_B, APP_A), dao.readItemsByContainer(WorkspaceContainerType.HOME).map { it.itemId })
    }

    @Test
    fun concreteWriterRejectsGridMismatchBeforeRoomMutation() = runBlocking {
        val dao = database.workspaceDao()
        seedTwoHomeAppsAndDock()
        val beforePages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
        val beforeItems = dao.readItemsByContainer(WorkspaceContainerType.HOME)

        try {
            LauncherTransactionalPortableRestoreWriter(dao, preferencesRepository)
                .replacePortableState(
                    restoredWorkspace(),
                    LauncherPreferences(homeColumns = 5, homeRows = 5),
                )
            fail("incompatible workspace/preference pair must be rejected before persistence")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message.orEmpty().contains("grid must match"))
        }

        assertEquals(beforePages, dao.readPagesByContainer(WorkspaceContainerType.HOME))
        assertEquals(beforeItems, dao.readItemsByContainer(WorkspaceContainerType.HOME))
    }

    private suspend fun seedTwoHomeAppsAndDock() {
        val dao = database.workspaceDao()
        dao.upsertPages(
            listOf(
                WorkspacePageEntity(HOME_PAGE_A, WorkspaceContainerType.HOME, 0),
                WorkspacePageEntity(HOME_PAGE_B, WorkspaceContainerType.HOME, 1),
                WorkspacePageEntity(DOCK_PAGE, WorkspaceContainerType.DOCK, 0),
            )
        )
        dao.upsertItems(
            listOf(
                WorkspaceItemEntity(
                    itemId = APP_A,
                    pageId = HOME_PAGE_A,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_KEY_A,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
                WorkspaceItemEntity(
                    itemId = APP_B,
                    pageId = HOME_PAGE_B,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_KEY_B,
                    rank = 0,
                    cellX = 2,
                    cellY = 1,
                ),
                WorkspaceItemEntity(
                    itemId = DOCK_APP,
                    pageId = DOCK_PAGE,
                    itemType = WorkspaceItemType.APP,
                    appKey = DOCK_APP_KEY,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
            )
        )
    }

    private fun restoredWorkspace(): WorkspacePortableSnapshot.Snapshot =
        WorkspacePortableSnapshot.Snapshot(
            grid = WorkspaceGridPlacement.Grid(columns = 4, rows = 5),
            pages = listOf(
                WorkspacePagedPlacement.Page(
                    pageId = "restored:0",
                    rank = 0,
                    placements = listOf(
                        WorkspaceGridPlacement.Placement(APP_B, cellX = 0, cellY = 0),
                        WorkspaceGridPlacement.Placement(APP_A, cellX = 1, cellY = 0),
                    ),
                )
            ),
        )

    private companion object {
        const val DATABASE_NAME = "launcher-portable-restore-persistence-runtime.db"
        const val HOME_PAGE_A = "home:0"
        const val HOME_PAGE_B = "home:1"
        const val DOCK_PAGE = "dock:0"
        const val APP_A = "item:alpha"
        const val APP_B = "item:beta"
        const val DOCK_APP = "item:dock"
        const val APP_KEY_A = "10:com.example.alpha/.MainActivity"
        const val APP_KEY_B = "10:com.example.beta/.MainActivity"
        const val DOCK_APP_KEY = "10:com.example.dock/.MainActivity"
    }
}
