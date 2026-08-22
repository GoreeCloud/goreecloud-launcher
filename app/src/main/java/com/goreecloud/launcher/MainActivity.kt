package com.goreecloud.launcher

import android.app.role.RoleManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceDualReadResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceMirrorResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceRelationalMirror
import com.goreecloud.launcher.core.workspace.db.WorkspaceRelationalReader
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.LauncherRoot
import com.goreecloud.launcher.ui.theme.GlazeTheme
import com.goreecloud.launcher.ui.theme.GlazeThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appsRepository: LauncherAppsRepository
    private lateinit var themeRepository: GlazeThemeRepository
    private lateinit var workspaceRepository: WorkspaceRepository
    private var workspaceRelationalMirror: WorkspaceRelationalMirror? = null
    private var workspaceRelationalReader: WorkspaceRelationalReader? = null
    private val defaultHomeState = MutableStateFlow(false)

    private val homeRoleRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshHomeRoleState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appsRepository = LauncherAppsRepository(this)
        themeRepository = GlazeThemeRepository(this)
        workspaceRepository = WorkspaceRepository(this)

        runCatching { LauncherDatabaseProvider.get(this).workspaceDao() }
            .getOrNull()
            ?.let { workspaceDao ->
                workspaceRelationalMirror = WorkspaceRelationalMirror(workspaceDao)
                workspaceRelationalReader = WorkspaceRelationalReader(workspaceDao)
            }

        refreshHomeRoleState()

        setContent {
            val apps by appsRepository.apps.collectAsStateWithLifecycle(initialValue = emptyList())
            val themeMode by themeRepository.themeMode.collectAsState(initial = themeRepository.defaultMode)
            val workspace by workspaceRepository.state.collectAsStateWithLifecycle(initialValue = WorkspaceState())
            val isDefaultHome by defaultHomeState.collectAsStateWithLifecycle()

            LaunchedEffect(apps, workspace.initialized) {
                if (!workspace.initialized && apps.isNotEmpty()) {
                    val defaults = apps.filterNot { it.componentName.packageName == packageName }
                    workspaceRepository.ensureDefaults(
                        favoriteKeys = defaults.take(12).map { it.workspaceKey() },
                        dockKeys = defaults.take(4).map { it.workspaceKey() },
                    )
                }
            }

            LaunchedEffect(
                workspace.authority,
                workspace.initialized,
                workspace.favoriteKeys,
                workspace.dockKeys,
            ) {
                if (workspace.authority != WorkspaceAuthority.DATASTORE) return@LaunchedEffect
                val mirror = workspaceRelationalMirror ?: return@LaunchedEffect

                when (mirror.sync(workspace)) {
                    WorkspaceMirrorResult.Verified -> workspaceRepository.markRoomVerified(workspace)
                    WorkspaceMirrorResult.Mismatch,
                    is WorkspaceMirrorResult.Failed -> workspaceRepository.markDataStoreAuthoritative()
                    WorkspaceMirrorResult.Skipped -> Unit
                }
            }

            LaunchedEffect(
                workspace.authority,
                workspace.initialized,
                workspace.favoriteKeys,
                workspace.dockKeys,
                workspace.verifiedRoomFingerprint,
            ) {
                if (workspace.authority != WorkspaceAuthority.ROOM_VERIFIED) return@LaunchedEffect
                val reader = workspaceRelationalReader
                if (reader == null) {
                    workspaceRepository.markDataStoreAuthoritative()
                    return@LaunchedEffect
                }

                when (reader.reconcile(workspace)) {
                    WorkspaceDualReadResult.Match -> Unit
                    WorkspaceDualReadResult.Mismatch,
                    is WorkspaceDualReadResult.Failed -> workspaceRepository.markDataStoreAuthoritative()
                    WorkspaceDualReadResult.Skipped -> Unit
                }
            }

            GlazeTheme(themeMode) {
                LauncherRoot(
                    apps = apps,
                    workspace = workspace,
                    isDefaultHome = isDefaultHome,
                    onRequestHomeRole = ::requestHomeRole,
                    onLaunchApp = appsRepository::launch,
                    onToggleFavorite = { app ->
                        lifecycleScope.launch { workspaceRepository.toggleFavorite(app.workspaceKey()) }
                    },
                    onToggleDock = { app ->
                        lifecycleScope.launch { workspaceRepository.toggleDock(app.workspaceKey()) }
                    },
                    onMoveFavorite = { app, direction ->
                        lifecycleScope.launch {
                            workspaceRepository.moveFavorite(app.workspaceKey(), direction)
                        }
                    },
                    onMoveDock = { app, direction ->
                        lifecycleScope.launch {
                            workspaceRepository.moveDock(app.workspaceKey(), direction)
                        }
                    },
                    onMoveFavoriteToTarget = { app, targetKey ->
                        lifecycleScope.launch {
                            workspaceRepository.moveFavoriteToTarget(app.workspaceKey(), targetKey)
                        }
                    },
                    onMoveDockToTarget = { app, targetKey ->
                        lifecycleScope.launch {
                            workspaceRepository.moveDockToTarget(app.workspaceKey(), targetKey)
                        }
                    },
                    themeMode = themeMode,
                    onCycleTheme = themeRepository::cycleMode,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHomeRoleState()
    }

    private fun refreshHomeRoleState() {
        val manager = getSystemService(RoleManager::class.java)
        defaultHomeState.value =
            manager.isRoleAvailable(RoleManager.ROLE_HOME) && manager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun requestHomeRole() {
        val manager = getSystemService(RoleManager::class.java)
        if (manager.isRoleAvailable(RoleManager.ROLE_HOME) && !manager.isRoleHeld(RoleManager.ROLE_HOME)) {
            homeRoleRequest.launch(manager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        }
    }
}
