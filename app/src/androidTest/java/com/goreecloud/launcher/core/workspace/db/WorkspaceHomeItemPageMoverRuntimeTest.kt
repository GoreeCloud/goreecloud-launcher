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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WorkspaceHomeItemPageMoverRuntimeTest {
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
    fun secondaryMovePreservesCanonicalPrimaryAndChoosesFirstFreeCell() = runBlocking {
        val authorityRepository = WorkspaceRepository(openDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = listOf(APP_ONE),
            dockKeys = emptyList(),
        )
        val mutations = WorkspacePagedRoomMutationRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )
        val mover = WorkspaceHomeItemPageMover(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
            mutationRepository = mutations,
        )

        assertEquals(
            WorkspacePagedRoomMutationResult.Reserved,
            mover.moveAppToPage("home:1", APP_TWO, "home:2"),
        )

        promoteRoomAuthority(authorityRepository)
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
                    pageId = "home:2",
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_THREE,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
            )
        )

        assertEquals(
            WorkspacePagedRoomMutationResult.UpdatedItem(
                itemId = "native:item:two",
                pageId = "home:2",
                cellX = 1,
                cellY = 0,
                spanX = 1,
                spanY = 1,
            ),
            mover.moveAppToPage("home:1", APP_TWO, "home:2"),
        )

        assertTrue(database.workspaceDao().readItems(listOf("home:1")).isEmpty())
        val targetItems = database.workspaceDao().readItems(listOf("home:2")).sortedBy { it.rank }
        assertEquals(listOf(APP_THREE, APP_TWO), targetItems.map { it.appKey })
        assertEquals(1, targetItems.last().cellX)
        assertEquals(0, targetItems.last().cellY)

        val primaryItems = database.workspaceDao()
            .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
        assertEquals(listOf(APP_ONE), primaryItems.map { it.appKey })
        assertNull(primaryItems.single().cellX)
        assertNull(primaryItems.single().cellY)
        assertEquals(
            WorkspaceRelationalSnapshot(
                favoriteKeys = listOf(APP_ONE),
                dockKeys = emptyList(),
            ),
            WorkspaceCanonicalRoomPlacementReader.read(database.workspaceDao()),
        )
        assertEquals(
            WorkspacePostCutoverHealthResult.Healthy,
            WorkspacePostCutoverHealthEvaluator(
                repository = authorityRepository,
                workspaceDaoProvider = { database.workspaceDao() },
            ).evaluate(),
        )

        assertEquals(
            WorkspacePagedRoomMutationResult.PrimaryPageProtected,
            mover.moveAppToPage("home:2", APP_TWO, WorkspaceLegacyImportMapper.HOME_PAGE_ID),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.PrimaryPageProtected,
            mover.moveAppToPage(WorkspaceLegacyImportMapper.HOME_PAGE_ID, APP_ONE, "home:2"),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.PageNotFound,
            mover.moveAppToPage("home:2", APP_TWO, "missing"),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.InvalidWorkspace,
            mover.moveAppToPage("home:2", APP_TWO, "home:2"),
        )
    }

    @Test
    fun oneCellMovementRejectsOccupiedAndOutOfBoundsTargetsWithoutDisplacement() = runBlocking {
        val authorityRepository = WorkspaceRepository(openDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = listOf(APP_ONE),
            dockKeys = emptyList(),
        )
        promoteRoomAuthority(authorityRepository)
        val mover = WorkspaceHomeItemPageMover(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
            mutationRepository = WorkspacePagedRoomMutationRepository(
                authorityRepository = authorityRepository,
                workspaceDaoProvider = { database.workspaceDao() },
            ),
        )

        database.workspaceDao().upsertPages(
            listOf(WorkspacePageEntity("home:1", WorkspaceContainerType.HOME, 1))
        )
        database.workspaceDao().upsertItems(
            listOf(
                WorkspaceItemEntity(
                    itemId = "native:item:two",
                    pageId = "home:1",
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_TWO,
                    rank = 0,
                    cellX = 1,
                    cellY = 0,
                ),
                WorkspaceItemEntity(
                    itemId = "native:item:three",
                    pageId = "home:1",
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_THREE,
                    rank = 1,
                    cellX = 2,
                    cellY = 0,
                ),
            )
        )

        assertEquals(
            WorkspacePagedRoomMutationResult.UpdatedItem(
                itemId = "native:item:two",
                pageId = "home:1",
                cellX = 0,
                cellY = 0,
                spanX = 1,
                spanY = 1,
            ),
            mover.moveAppOneCellWithinPage("home:1", APP_TWO, WorkspaceHomeSpatialDirection.LEFT),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.InvalidWorkspace,
            mover.moveAppOneCellWithinPage("home:1", APP_TWO, WorkspaceHomeSpatialDirection.LEFT),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.UpdatedItem(
                itemId = "native:item:two",
                pageId = "home:1",
                cellX = 1,
                cellY = 0,
                spanX = 1,
                spanY = 1,
            ),
            mover.moveAppOneCellWithinPage("home:1", APP_TWO, WorkspaceHomeSpatialDirection.RIGHT),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.InvalidWorkspace,
            mover.moveAppOneCellWithinPage("home:1", APP_TWO, WorkspaceHomeSpatialDirection.RIGHT),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.InvalidWorkspace,
            mover.moveAppOneCellWithinPage("home:1", APP_TWO, WorkspaceHomeSpatialDirection.UP),
        )
        assertEquals(
            WorkspacePagedRoomMutationResult.UpdatedItem(
                itemId = "native:item:two",
                pageId = "home:1",
                cellX = 1,
                cellY = 1,
                spanX = 1,
                spanY = 1,
            ),
            mover.moveAppOneCellWithinPage("home:1", APP_TWO, WorkspaceHomeSpatialDirection.DOWN),
        )

        val items = database.workspaceDao().readItems(listOf("home:1")).associateBy { it.appKey }
        assertEquals(1, items.getValue(APP_TWO).cellX)
        assertEquals(1, items.getValue(APP_TWO).cellY)
        assertEquals(2, items.getValue(APP_THREE).cellX)
        assertEquals(0, items.getValue(APP_THREE).cellY)
        assertEquals(
            WorkspacePostCutoverHealthResult.Healthy,
            WorkspacePostCutoverHealthEvaluator(
                repository = authorityRepository,
                workspaceDaoProvider = { database.workspaceDao() },
            ).evaluate(),
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
        const val DATABASE_NAME = "launcher-home-item-page-mover-test.db"
        const val DATASTORE_FILE = "launcher-home-item-page-mover.preferences_pb"
        const val APP_ONE = "10:com.example.one/.MainActivity"
        const val APP_TWO = "10:com.example.two/.MainActivity"
        const val APP_THREE = "10:com.example.three/.MainActivity"
    }
}
