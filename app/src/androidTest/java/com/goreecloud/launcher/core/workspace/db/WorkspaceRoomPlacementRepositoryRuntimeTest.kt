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

        val expectedReplacement = WorkspaceRelationalSnapshot(
            favoriteKeys = REPLACEMENT_FAVORITES,
            dockKeys = listOf(DOCK_ONE, DOCK_TWO, DOCK_THREE, DOCK_FOUR, DOCK_FIVE),
        )
        assertEquals(
            WorkspaceRoomWriteResult.Written(expectedReplacement),
            placementRepository.replace(
                favoriteKeys = REPLACEMENT_FAVORITES,
                dockKeys = REPLACEMENT_DOCK_WITH_DUPLICATES,
            ),
        )
        assertEquals(
            WorkspaceRoomReadResult.Loaded(expectedReplacement),
            placementRepository.read(),
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
