package com.goreecloud.launcher.core.workspace.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WorkspaceAtomicHomeGroupMoverRuntimeTest {
    private lateinit var context: Context
    private lateinit var database: LauncherDatabase
    private lateinit var dataStoreFile: File
    private var dataStoreScope: CoroutineScope? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
        database = Room.databaseBuilder(context, LauncherDatabase::class.java, DATABASE_NAME)
            .setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        dataStoreFile = File(context.cacheDir, DATASTORE_FILE)
        dataStoreFile.delete()
    }

    @After
    fun tearDown() {
        runBlocking {
            dataStoreScope?.coroutineContext?.get(Job)?.cancelAndJoin()
            dataStoreScope = null
        }
        database.close()
        context.deleteDatabase(DATABASE_NAME)
        dataStoreFile.delete()
    }

    @Test
    fun movesCompleteSelectionInOneAcceptedBatch() = runBlocking {
        val authorityRepository = WorkspaceRepository(openDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = listOf(APP_ONE),
            dockKeys = emptyList(),
        )
        promoteRoomAuthority(authorityRepository)
        seedSecondaryPages()

        val mover = WorkspaceAtomicHomeGroupMover(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )

        assertEquals(
            WorkspaceAtomicHomeGroupMoveResult.Moved(count = 2, targetPageId = "home:2"),
            mover.moveAppsToPage(
                sourcePageId = "home:1",
                appKeys = listOf(APP_TWO, APP_THREE),
                targetPageId = "home:2",
            ),
        )

        assertTrue(database.workspaceDao().readItems(listOf("home:1")).isEmpty())
        val targetItems = database.workspaceDao().readItems(listOf("home:2")).sortedBy { it.rank }
        assertEquals(listOf(APP_FOUR, APP_TWO, APP_THREE), targetItems.map { it.appKey })
        assertEquals(listOf(0, 1, 2), targetItems.map { it.rank })
        assertEquals(3, targetItems.map { it.itemId }.toSet().size)
    }

    @Test
    fun staleExpectedSnapshotRejectsEntireBatchWithoutPartialWrite() = runBlocking {
        val authorityRepository = WorkspaceRepository(openDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = listOf(APP_ONE),
            dockKeys = emptyList(),
        )
        promoteRoomAuthority(authorityRepository)
        seedSecondaryPages()

        val dao = database.workspaceDao()
        val expectedPages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
        val expectedItems = dao.readItems(expectedPages.map { it.pageId })
        val originalTwo = expectedItems.single { it.appKey == APP_TWO }
        val originalThree = expectedItems.single { it.appKey == APP_THREE }

        // Simulate a concurrent accepted mutation after the planner observed its snapshot.
        val concurrent = originalTwo.copy(cellX = 2, cellY = 0)
        dao.upsertItems(listOf(concurrent))

        val plannedTwo = originalTwo.copy(pageId = "home:2", rank = 1, cellX = 1, cellY = 0)
        val plannedThree = originalThree.copy(pageId = "home:2", rank = 2, cellX = 2, cellY = 0)

        assertEquals(
            false,
            dao.replaceItemPlacementsIfSnapshotMatches(
                containerType = WorkspaceContainerType.HOME,
                expectedPages = expectedPages,
                expectedItems = expectedItems,
                updatedItems = listOf(plannedTwo, plannedThree),
            ),
        )

        val after = dao.readItems(expectedPages.map { it.pageId }).associateBy { it.appKey }
        assertEquals("home:1", after.getValue(APP_TWO).pageId)
        assertEquals(2, after.getValue(APP_TWO).cellX)
        assertEquals("home:1", after.getValue(APP_THREE).pageId)
        assertEquals(originalThree.cellX, after.getValue(APP_THREE).cellX)
        assertEquals(originalThree.cellY, after.getValue(APP_THREE).cellY)
    }

    @Test
    fun invalidSelectionsFailBeforeMutation() = runBlocking {
        val authorityRepository = WorkspaceRepository(openDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = listOf(APP_ONE),
            dockKeys = emptyList(),
        )
        promoteRoomAuthority(authorityRepository)
        seedSecondaryPages()
        val mover = WorkspaceAtomicHomeGroupMover(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )

        val before = database.workspaceDao().readItemsByContainer(WorkspaceContainerType.HOME)
        assertEquals(
            WorkspaceAtomicHomeGroupMoveResult.InvalidWorkspace,
            mover.moveAppsToPage("home:1", listOf(APP_TWO, APP_TWO), "home:2"),
        )
        assertEquals(
            WorkspaceAtomicHomeGroupMoveResult.ItemNotFound,
            mover.moveAppsToPage("home:1", listOf(APP_TWO, "missing"), "home:2"),
        )
        assertEquals(
            WorkspaceAtomicHomeGroupMoveResult.PrimaryPageProtected,
            mover.moveAppsToPage(
                "home:1",
                listOf(APP_TWO),
                WorkspaceLegacyImportMapper.HOME_PAGE_ID,
            ),
        )
        assertEquals(before, database.workspaceDao().readItemsByContainer(WorkspaceContainerType.HOME))
    }

    private suspend fun seedSecondaryPages() {
        database.workspaceDao().upsertPages(
            listOf(
                WorkspacePageEntity("home:1", WorkspaceContainerType.HOME, 1),
                WorkspacePageEntity("home:2", WorkspaceContainerType.HOME, 2),
            )
        )
        database.workspaceDao().upsertItems(
            listOf(
                WorkspaceItemEntity(
                    itemId = "native:item:two",
                    pageId = "home:1",
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_TWO,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
                WorkspaceItemEntity(
                    itemId = "native:item:three",
                    pageId = "home:1",
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_THREE,
                    rank = 1,
                    cellX = 1,
                    cellY = 0,
                ),
                WorkspaceItemEntity(
                    itemId = "native:item:four",
                    pageId = "home:2",
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_FOUR,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
            )
        )
    }

    private suspend fun promoteRoomAuthority(authorityRepository: WorkspaceRepository) {
        var state = authorityRepository.state.first { it.initialized }
        assertEquals(
            WorkspaceMirrorResult.Verified,
            WorkspaceRelationalMirror(database.workspaceDao()).sync(state),
        )
        assertTrue(authorityRepository.markRoomVerified(state))
        state = authorityRepository.state.first { it.authority == WorkspaceAuthority.ROOM_VERIFIED }
        assertEquals(
            WorkspaceDualReadResult.Match,
            WorkspaceRelationalReader(database.workspaceDao()).reconcile(state),
        )
        assertTrue(authorityRepository.promoteRoomAuthority(state))
        authorityRepository.state.first { it.authority == WorkspaceAuthority.ROOM }
    }

    private fun openDataStore(): DataStore<Preferences> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStoreScope = scope
        return PreferenceDataStoreFactory.create(scope = scope, produceFile = { dataStoreFile })
    }

    private companion object {
        const val DATABASE_NAME = "launcher-atomic-home-group-move-test.db"
        const val DATASTORE_FILE = "launcher-atomic-home-group-move.preferences_pb"
        const val APP_ONE = "10:com.example.one/.MainActivity"
        const val APP_TWO = "10:com.example.two/.MainActivity"
        const val APP_THREE = "10:com.example.three/.MainActivity"
        const val APP_FOUR = "10:com.example.four/.MainActivity"
    }
}
