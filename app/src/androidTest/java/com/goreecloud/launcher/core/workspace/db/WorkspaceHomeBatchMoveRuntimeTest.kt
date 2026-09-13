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
class WorkspaceHomeBatchMoveRuntimeTest {
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
    fun batchMoveCommitsAllItemsTogetherAndExactStateUndoRestoresSource() = runBlocking {
        val authorityRepository = prepareRoomAuthority()
        seedSecondaryPages()
        val service = service(authorityRepository)

        val move = service.moveAppsToPage(
            sourcePageId = SOURCE_PAGE,
            appKeys = listOf(APP_THREE, APP_TWO),
            targetPageId = TARGET_PAGE,
        )
        assertTrue(move is WorkspaceHomeBatchMoveResult.Applied)
        val commit = (move as WorkspaceHomeBatchMoveResult.Applied).commit
        assertEquals(listOf(ITEM_TWO, ITEM_THREE), commit.movedItemIds)

        val sourceAfter = database.workspaceDao().readItems(listOf(SOURCE_PAGE))
        assertTrue(sourceAfter.isEmpty())
        val targetAfter = database.workspaceDao().readItems(listOf(TARGET_PAGE)).sortedBy { it.rank }
        assertEquals(listOf(APP_FOUR, APP_TWO, APP_THREE), targetAfter.map { it.appKey })
        assertEquals(listOf(0, 1, 2), targetAfter.map { it.rank })
        assertEquals(listOf(0, 1, 2), targetAfter.map { it.cellX })
        assertEquals(listOf(0, 0, 0), targetAfter.map { it.cellY })

        val undo = service.undo(commit)
        assertEquals(WorkspaceHomeBatchMoveResult.Undone(commit), undo)

        val sourceRestored = database.workspaceDao().readItems(listOf(SOURCE_PAGE)).sortedBy { it.rank }
        assertEquals(listOf(APP_TWO, APP_THREE), sourceRestored.map { it.appKey })
        assertEquals(listOf(0, 1), sourceRestored.map { it.rank })
        assertEquals(listOf(0, 1), sourceRestored.map { it.cellX })
        assertEquals(listOf(0, 0), sourceRestored.map { it.cellY })
        val targetRestored = database.workspaceDao().readItems(listOf(TARGET_PAGE)).sortedBy { it.rank }
        assertEquals(listOf(APP_FOUR), targetRestored.map { it.appKey })
    }

    @Test
    fun undoFailsClosedAfterAnyInterveningHomeMutation() = runBlocking {
        val authorityRepository = prepareRoomAuthority()
        seedSecondaryPages()
        val service = service(authorityRepository)

        val move = service.moveAppsToPage(
            sourcePageId = SOURCE_PAGE,
            appKeys = listOf(APP_TWO, APP_THREE),
            targetPageId = TARGET_PAGE,
        ) as WorkspaceHomeBatchMoveResult.Applied

        database.workspaceDao().upsertItems(
            listOf(
                WorkspaceItemEntity(
                    itemId = ITEM_FIVE,
                    pageId = TARGET_PAGE,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_FIVE,
                    rank = 3,
                    cellX = 3,
                    cellY = 0,
                )
            )
        )

        assertEquals(
            WorkspaceHomeBatchMoveResult.StoredWorkspaceChanged,
            service.undo(move.commit),
        )

        val targetItems = database.workspaceDao().readItems(listOf(TARGET_PAGE)).sortedBy { it.rank }
        assertEquals(
            listOf(APP_FOUR, APP_TWO, APP_THREE, APP_FIVE),
            targetItems.map { it.appKey },
        )
        assertTrue(database.workspaceDao().readItems(listOf(SOURCE_PAGE)).isEmpty())
    }

    private suspend fun prepareRoomAuthority(): WorkspaceRepository {
        val authorityRepository = WorkspaceRepository(openDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = listOf(APP_ONE),
            dockKeys = emptyList(),
        )
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
        return authorityRepository
    }

    private suspend fun seedSecondaryPages() {
        database.workspaceDao().upsertPages(
            listOf(
                WorkspacePageEntity(SOURCE_PAGE, WorkspaceContainerType.HOME, 1),
                WorkspacePageEntity(TARGET_PAGE, WorkspaceContainerType.HOME, 2),
            )
        )
        database.workspaceDao().upsertItems(
            listOf(
                WorkspaceItemEntity(
                    itemId = ITEM_TWO,
                    pageId = SOURCE_PAGE,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_TWO,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
                WorkspaceItemEntity(
                    itemId = ITEM_THREE,
                    pageId = SOURCE_PAGE,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_THREE,
                    rank = 1,
                    cellX = 1,
                    cellY = 0,
                ),
                WorkspaceItemEntity(
                    itemId = ITEM_FOUR,
                    pageId = TARGET_PAGE,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_FOUR,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                ),
            )
        )
    }

    private fun service(authorityRepository: WorkspaceRepository) = WorkspaceHomeBatchMoveService(
        authorityRepository = authorityRepository,
        workspaceDaoProvider = { database.workspaceDao() },
        batchDaoProvider = { database.workspaceHomeBatchMoveDao() },
    )

    private fun openDataStore(): DataStore<Preferences> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStoreScope = scope
        return PreferenceDataStoreFactory.create(scope = scope, produceFile = { dataStoreFile })
    }

    private companion object {
        const val DATABASE_NAME = "launcher-home-batch-move-test.db"
        const val DATASTORE_FILE = "launcher-home-batch-move.preferences_pb"
        const val SOURCE_PAGE = "home:1"
        const val TARGET_PAGE = "home:2"
        const val ITEM_TWO = "native:item:two"
        const val ITEM_THREE = "native:item:three"
        const val ITEM_FOUR = "native:item:four"
        const val ITEM_FIVE = "native:item:five"
        const val APP_ONE = "10:com.example.one/.MainActivity"
        const val APP_TWO = "10:com.example.two/.MainActivity"
        const val APP_THREE = "10:com.example.three/.MainActivity"
        const val APP_FOUR = "10:com.example.four/.MainActivity"
        const val APP_FIVE = "10:com.example.five/.MainActivity"
    }
}
