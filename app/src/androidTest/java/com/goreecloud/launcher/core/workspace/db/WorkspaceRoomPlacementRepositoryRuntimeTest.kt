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
import com.goreecloud.launcher.core.workspace.WorkspaceWidgetCatalog
import com.goreecloud.launcher.core.workspace.WorkspaceWidgetDescriptor
import com.goreecloud.launcher.core.workspace.WorkspaceWidgetKeyCodec
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
class WorkspaceRoomPlacementRepositoryRuntimeTest {
    private lateinit var context: Context
    private lateinit var database: LauncherDatabase
    private lateinit var workspaceDataStoreFile: File
    private var workspaceDataStoreScope: CoroutineScope? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
        database = openDatabase()
        workspaceDataStoreFile = File(context.cacheDir, WORKSPACE_DATASTORE_FILE)
        workspaceDataStoreFile.delete()
    }

    @After
    fun tearDown() {
        runBlocking { closeWorkspaceDataStore() }
        database.close()
        context.deleteDatabase(DATABASE_NAME)
        workspaceDataStoreFile.delete()
    }

    @Test
    fun placementIoIsReservedBeforeCutoverAndAvailableOnlyAfterGuardedPromotion() = runBlocking {
        val authorityRepository = WorkspaceRepository(openWorkspaceDataStore())
        authorityRepository.ensureDefaults(
            favoriteKeys = INITIAL_FAVORITES,
            dockKeys = INITIAL_DOCK,
        )

        val placementRepository = WorkspaceRoomPlacementRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )

        assertEquals(WorkspaceRoomReadResult.Reserved, placementRepository.read())
        assertEquals(
            WorkspaceRoomWriteResult.Reserved,
            placementRepository.replace(
                favoriteKeys = REPLACEMENT_FAVORITES,
                dockKeys = REPLACEMENT_DOCK_WITH_DUPLICATES,
                homeGrid = WorkspaceGridPlacement.Grid(columns = 4, rows = 5),
            ),
        )

        var state = authorityRepository.state.first { it.initialized }
        assertEquals(
            WorkspaceMirrorResult.Verified,
            WorkspaceRelationalMirror(database.workspaceDao()).sync(state),
        )
        assertTrue(authorityRepository.markRoomVerified(state))
        state = authorityRepository.state.first {
            it.authority == WorkspaceAuthority.ROOM_VERIFIED
        }
        assertEquals(
            WorkspaceDualReadResult.Match,
            WorkspaceRelationalReader(database.workspaceDao()).reconcile(state),
        )
        assertTrue(authorityRepository.promoteRoomAuthority(state))

        state = authorityRepository.state.first { it.authority == WorkspaceAuthority.ROOM }
        assertEquals(INITIAL_FAVORITES, state.favoriteKeys)
        assertEquals(INITIAL_DOCK, state.dockKeys)
        assertEquals(
            WorkspaceRoomReadResult.Loaded(
                WorkspaceRelationalSnapshot(INITIAL_FAVORITES, INITIAL_DOCK)
            ),
            placementRepository.read(),
        )

        val spatialRepository = WorkspacePrimaryHomeSpatialRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )
        assertEquals(
            WorkspacePrimaryHomeSpatialResult.Ready(
                changed = true,
                columns = 4,
                rows = 5,
            ),
            spatialRepository.ensureGrid(columns = 4, rows = 5),
        )
        val migratedPrimary = database.workspaceDao()
            .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
            .sortedBy { it.rank }
        assertTrue(migratedPrimary.all { it.cellX != null && it.cellY != null })

        assertEquals(
            WorkspacePrimaryHomeSpatialResult.InvalidWorkspace,
            spatialRepository.ensureGrid(columns = 3, rows = 5),
        )
        assertEquals(
            WorkspacePrimaryHomeSpatialResult.InvalidWorkspace,
            spatialRepository.ensureGrid(columns = 4, rows = 8),
        )

        val widget = WorkspaceItemEntity(
            itemId = "widget:builtin:runtime",
            pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
            itemType = WorkspaceItemType.WIDGET,
            appKey = WorkspaceWidgetKeyCodec.encode(
                WorkspaceWidgetDescriptor.BuiltIn(WorkspaceWidgetCatalog.CLOCK),
            ),
            rank = migratedPrimary.size,
            cellX = 2,
            cellY = 0,
            spanX = 2,
            spanY = 2,
        )
        val primaryPage = database.workspaceDao()
            .readPages(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
            .single()
        assertTrue(
            database.workspaceDao().replacePrimaryHomeItemsIncludingIdentityChangesIfSnapshotMatches(
                expectedPage = primaryPage,
                expectedItems = migratedPrimary,
                updatedItems = migratedPrimary + widget,
            ),
        )
        val folderRepository = WorkspaceFolderRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )
        assertEquals(
            WorkspaceFolderMutationResult.Added(
                itemId = "folder:home:runtime",
                folderId = "folder-id-runtime",
                cellX = 0,
                cellY = 1,
            ),
            folderRepository.addFolderToHome(
                itemId = "folder:home:runtime",
                folderId = "folder-id-runtime",
                columns = 4,
                rows = 5,
            ),
        )
        val folder = database.workspaceDao()
            .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
            .single { it.itemType == WorkspaceItemType.FOLDER }
        assertEquals(
            WorkspaceRoomReadResult.Loaded(
                WorkspaceRelationalSnapshot(INITIAL_FAVORITES, INITIAL_DOCK)
            ),
            placementRepository.read(),
        )

        val expectedReplacement = WorkspaceRelationalSnapshot(
            favoriteKeys = REPLACEMENT_FAVORITES,
            dockKeys = listOf(DOCK_ONE, DOCK_TWO, DOCK_THREE, DOCK_FOUR, DOCK_FIVE),
        )
        assertEquals(
            WorkspaceRoomWriteResult.Written(expectedReplacement),
            placementRepository.replace(
                favoriteKeys = REPLACEMENT_FAVORITES,
                dockKeys = REPLACEMENT_DOCK_WITH_DUPLICATES,
                homeGrid = WorkspaceGridPlacement.Grid(columns = 4, rows = 5),
            ),
        )
        assertEquals(
            WorkspaceRoomReadResult.Loaded(expectedReplacement),
            placementRepository.read(),
        )
        val spatialReplacement = database.workspaceDao()
            .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
            .sortedBy { it.rank }
        assertEquals(
            REPLACEMENT_FAVORITES,
            spatialReplacement
                .filter { it.itemType == WorkspaceItemType.APP }
                .mapNotNull { it.appKey },
        )
        val retainedWidget = spatialReplacement.single {
            it.itemType == WorkspaceItemType.WIDGET
        }
        assertEquals(widget.appKey, retainedWidget.appKey)
        assertEquals(widget.cellX, retainedWidget.cellX)
        assertEquals(widget.cellY, retainedWidget.cellY)
        assertEquals(widget.spanX, retainedWidget.spanX)
        assertEquals(widget.spanY, retainedWidget.spanY)
        val retainedFolder = spatialReplacement.single {
            it.itemType == WorkspaceItemType.FOLDER
        }
        assertEquals(folder.itemId, retainedFolder.itemId)
        assertEquals(folder.appKey, retainedFolder.appKey)
        assertEquals(folder.cellX, retainedFolder.cellX)
        assertEquals(folder.cellY, retainedFolder.cellY)
        assertEquals(folder.spanX, retainedFolder.spanX)
        assertEquals(folder.spanY, retainedFolder.spanY)
        assertEquals(
            WorkspaceFolderMutationResult.Removed(folder.itemId, folder.appKey!!),
            folderRepository.removeFolderFromHome(folder.appKey!!),
        )
        assertTrue(
            database.workspaceDao()
                .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
                .none { it.itemType == WorkspaceItemType.FOLDER }
        )
        // Real Room writes must reject collisions and preserve the widget's identity and size.
        val widgetRepository = WorkspaceWidgetRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { database.workspaceDao() },
        )
        assertEquals(
            WorkspaceWidgetMutationResult.NoSpace,
            widgetRepository.moveWidget(retainedWidget.itemId, 4, 5, 0, 0),
        )
        assertEquals(
            WorkspaceWidgetMutationResult.Moved(retainedWidget.itemId, 1, 2),
            widgetRepository.moveWidget(retainedWidget.itemId, 4, 5, 1, 2),
        )
        val movedWidget = database.workspaceDao()
            .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
            .single { it.itemId == retainedWidget.itemId }
        assertEquals(retainedWidget.appKey, movedWidget.appKey)
        assertEquals(retainedWidget.spanX, movedWidget.spanX)
        assertEquals(retainedWidget.spanY, movedWidget.spanY)
        assertEquals(1, movedWidget.cellX)
        assertEquals(2, movedWidget.cellY)

        assertTrue(
            spatialReplacement.all {
                it.cellX != null &&
                    it.cellY != null &&
                    it.cellX in 0 until 4 &&
                    it.cellY in 0 until 5
            }
        )

        val legacyStateAfterRoomWrite = authorityRepository.state.first()
        assertEquals(WorkspaceAuthority.ROOM, legacyStateAfterRoomWrite.authority)
        assertEquals(INITIAL_FAVORITES, legacyStateAfterRoomWrite.favoriteKeys)
        assertEquals(INITIAL_DOCK, legacyStateAfterRoomWrite.dockKeys)

        val unavailableRepository = WorkspaceRoomPlacementRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = { null },
        )
        assertEquals(WorkspaceRoomReadResult.Unavailable, unavailableRepository.read())
        assertEquals(
            WorkspaceRoomWriteResult.Unavailable,
            unavailableRepository.replace(REPLACEMENT_FAVORITES, emptyList()),
        )
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

    private fun openWorkspaceDataStore(): DataStore<Preferences> {
        check(workspaceDataStoreScope == null)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        workspaceDataStoreScope = scope
        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { workspaceDataStoreFile },
        )
    }

    private suspend fun closeWorkspaceDataStore() {
        val scope = workspaceDataStoreScope ?: return
        workspaceDataStoreScope = null
        scope.coroutineContext[Job]?.cancelAndJoin()
    }

    private companion object {
        const val DATABASE_NAME = "launcher-workspace-room-placement-test.db"
        const val WORKSPACE_DATASTORE_FILE = "launcher-workspace-room-placement.preferences_pb"

        val INITIAL_FAVORITES = listOf(
            "10:com.example.alpha/.MainActivity",
            "10:com.example.beta/.MainActivity",
        )
        val INITIAL_DOCK = listOf("10:com.example.initialdock/.MainActivity")
        val REPLACEMENT_FAVORITES = listOf(
            "10:com.example.gamma/.MainActivity",
            "10:com.example.delta/.MainActivity",
        )

        const val DOCK_ONE = "10:com.example.dockone/.MainActivity"
        const val DOCK_TWO = "10:com.example.docktwo/.MainActivity"
        const val DOCK_THREE = "10:com.example.dockthree/.MainActivity"
        const val DOCK_FOUR = "10:com.example.dockfour/.MainActivity"
        const val DOCK_FIVE = "10:com.example.dockfive/.MainActivity"
        const val DOCK_SIX = "10:com.example.docksix/.MainActivity"
        val REPLACEMENT_DOCK_WITH_DUPLICATES = listOf(
            DOCK_ONE,
            DOCK_TWO,
            DOCK_ONE,
            DOCK_THREE,
            DOCK_FOUR,
            DOCK_FIVE,
            DOCK_SIX,
        )
    }
}
