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
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WorkspaceHomePageCompactionRuntimeTest {
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
    fun compactPacksSecondaryAppsRowMajorWithoutChangingPageMembership() = runBlocking {
        val authorityRepository = prepareRoomAuthority()
        seedScatteredSecondaryPage()
        val service = service(authorityRepository)

        val result = service.compact(GRID, PAGE_TWO)
        assertEquals(WorkspaceHomePageCompactionResult.Applied(PAGE_TWO, 5), result)

        val compacted = database.workspaceDao().readItems(listOf(PAGE_TWO)).sortedBy { it.rank }
        assertEquals(listOf(APP_TWO, APP_THREE, APP_FOUR, APP_FIVE, APP_SIX), compacted.map { it.appKey })
        assertEquals(listOf(0, 1, 2, 3, 4), compacted.map { it.rank })
        assertEquals(listOf(0, 1, 2, 0, 1), compacted.map { it.cellX })
        assertEquals(listOf(0, 0, 0, 1, 1), compacted.map { it.cellY })
        assertTrue(database.workspaceDao().readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID)).isNotEmpty())

        assertEquals(WorkspaceHomePageCompactionResult.AlreadyCompact, service.compact(GRID, PAGE_TWO))
    }

    @Test
    fun compactionTransactionRefusesStaleCompleteHomeSnapshot() = runBlocking {
        val authorityRepository = prepareRoomAuthority()
        seedScatteredSecondaryPage()
        val dao = database.workspaceDao()
        val pages = dao.readPagesByContainer(WorkspaceContainerType.HOME)
        val items = dao.readItemsByContainer(WorkspaceContainerType.HOME)
        val plan = WorkspaceHomePageCompactionPolicy.plan(GRID, PAGE_TWO, items)
            as WorkspaceHomePageCompactionPolicy.Plan.Updated

        dao.upsertItems(
            listOf(
                WorkspaceItemEntity(
                    itemId = INTERVENING_ITEM,
                    pageId = PAGE_THREE,
                    itemType = WorkspaceItemType.APP,
                    appKey = APP_SEVEN,
                    rank = 0,
                    cellX = 0,
                    cellY = 0,
                )
            )
        )

        assertFalse(
            database.workspaceHomeBatchMoveDao().compactPageIfSnapshotMatches(
                pageId = PAGE_TWO,
                expectedPages = pages,
                expectedItems = items,
                compactedItems = plan.items,
            )
        )

        val pageTwo = dao.readItems(listOf(PAGE_TWO)).sortedBy { it.rank }
        assertEquals(listOf(2, 0, 2, 1, 2), pageTwo.map { it.cellX })
        assertEquals(listOf(3, 2, 1, 4, 4), pageTwo.map { it.cellY })
        assertEquals(listOf(APP_SEVEN), dao.readItems(listOf(PAGE_THREE)).map { it.appKey })
        // Keep a live reference so authority setup remains part of this runtime path.
        assertEquals(WorkspaceAuthority.ROOM, authorityRepository.state.first().authority)
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

    private suspend fun seedScatteredSecondaryPage() {
        database.workspaceDao().upsertPages(
            listOf(
                WorkspacePageEntity(PAGE_TWO, WorkspaceContainerType.HOME, 1),
                WorkspacePageEntity(PAGE_THREE, WorkspaceContainerType.HOME, 2),
            )
        )
        database.workspaceDao().upsertItems(
            listOf(
                appItem(ITEM_TWO, PAGE_TWO, APP_TWO, 0, 2, 3),
                appItem(ITEM_THREE, PAGE_TWO, APP_THREE, 1, 0, 2),
                appItem(ITEM_FOUR, PAGE_TWO, APP_FOUR, 2, 2, 1),
                appItem(ITEM_FIVE, PAGE_TWO, APP_FIVE, 3, 1, 4),
                appItem(ITEM_SIX, PAGE_TWO, APP_SIX, 4, 2, 4),
            )
        )
    }

    private fun appItem(
        itemId: String,
        pageId: String,
        appKey: String,
        rank: Int,
        x: Int,
        y: Int,
    ) = WorkspaceItemEntity(
        itemId = itemId,
        pageId = pageId,
        itemType = WorkspaceItemType.APP,
        appKey = appKey,
        rank = rank,
        cellX = x,
        cellY = y,
        spanX = 1,
        spanY = 1,
    )

    private fun service(authorityRepository: WorkspaceRepository) = WorkspaceHomePageCompactionService(
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
        const val DATABASE_NAME = "launcher-home-page-compaction-test.db"
        const val DATASTORE_FILE = "launcher-home-page-compaction.preferences_pb"
        val GRID = WorkspaceGridPlacement.Grid(columns = 3, rows = 5)
        const val PAGE_TWO = "home:compact:2"
        const val PAGE_THREE = "home:compact:3"
        const val ITEM_TWO = "compact:item:two"
        const val ITEM_THREE = "compact:item:three"
        const val ITEM_FOUR = "compact:item:four"
        const val ITEM_FIVE = "compact:item:five"
        const val ITEM_SIX = "compact:item:six"
        const val INTERVENING_ITEM = "compact:item:intervening"
        const val APP_ONE = "10:com.example.one/.MainActivity"
        const val APP_TWO = "10:com.example.two/.MainActivity"
        const val APP_THREE = "10:com.example.three/.MainActivity"
        const val APP_FOUR = "10:com.example.four/.MainActivity"
        const val APP_FIVE = "10:com.example.five/.MainActivity"
        const val APP_SIX = "10:com.example.six/.MainActivity"
        const val APP_SEVEN = "10:com.example.seven/.MainActivity"
    }
}
