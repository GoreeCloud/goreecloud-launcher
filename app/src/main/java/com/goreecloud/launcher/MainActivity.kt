package com.goreecloud.launcher

import android.app.role.RoleManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goreecloud.launcher.core.launcher.GoreeCloudIndexIntegration
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreRecoveryCoordinator
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupGate
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupSequence
import com.goreecloud.launcher.core.launcher.LauncherPreferencesRepository
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativePlacementState
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedHomeState
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedRoomMutationResult
import com.goreecloud.launcher.core.workspace.db.WorkspacePlacementSource
import com.goreecloud.launcher.core.workspace.db.WorkspaceProductionRuntimeCoordinator
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.HomePageSwitcher
import com.goreecloud.launcher.ui.LayoutLockHoldControl
import com.goreecloud.launcher.ui.LauncherBetaRoot
import com.goreecloud.launcher.ui.LauncherSurfaceMode
import com.goreecloud.launcher.ui.ReadOnlyPagedHomeSurface
import com.goreecloud.launcher.ui.theme.GlazeTheme
import com.goreecloud.launcher.ui.theme.GlazeThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var appsRepository: LauncherAppsRepository
    private lateinit var indexIntegration: GoreeCloudIndexIntegration
    private lateinit var launcherPreferencesRepository: LauncherPreferencesRepository
    private lateinit var themeRepository: GlazeThemeRepository
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var workspaceRuntimeCoordinator: WorkspaceProductionRuntimeCoordinator
    private val defaultHomeState = MutableStateFlow(false)
    private val portableRestoreRecoveryResult =
        MutableStateFlow<LauncherPortableRestoreRecoveryCoordinator.Result?>(null)

    private val homeRoleRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshHomeRoleState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appsRepository = LauncherAppsRepository(this)
        indexIntegration = GoreeCloudIndexIntegration(this)
        launcherPreferencesRepository = LauncherPreferencesRepository(this)
        themeRepository = GlazeThemeRepository(this)
        workspaceRepository = WorkspaceRepository(this)
        workspaceRuntimeCoordinator = WorkspaceProductionRuntimeCoordinator(
            authorityRepository = workspaceRepository,
            workspaceDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceDao()
            },
        )
        lifecycleScope.launch {
            val recovery = LauncherPortableRestoreStartupSequence.reconcileBeforeMutation(
                recoverPortableRestore = {
                    LauncherPortableRestoreRecoveryCoordinator(this@MainActivity).reconcile()
                },
                reconcileWorkspace = {
                    // onResume may have run while recovery was still pending and correctly skipped
                    // workspace reconciliation. Finish that deferred startup work before opening the
                    // normal mutation surface so recovery completion cannot strand Room activation.
                    workspaceRuntimeCoordinator.reconcileAndActivate()
                    Unit
                },
            )
            portableRestoreRecoveryResult.value = recovery
        }
        refreshHomeRoleState()

        setContent {
            val themeMode by themeRepository.themeMode.collectAsState(initial = themeRepository.defaultMode)
            val portableRestoreRecovery by portableRestoreRecoveryResult.collectAsStateWithLifecycle()

            if (!LauncherPortableRestoreStartupGate.allowsMutations(portableRestoreRecovery)) {
                GlazeTheme(themeMode) {
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(24.dp),
                    ) {
                        Text(LauncherPortableRestoreStartupGate.userMessage(portableRestoreRecovery))
                    }
                }
                return@setContent
            }

            val apps by appsRepository.apps.collectAsStateWithLifecycle(initialValue = emptyList())
            val launcherPreferences by launcherPreferencesRepository.preferences.collectAsStateWithLifecycle(
                initialValue = launcherPreferencesRepository.defaults,
            )
            val placement by workspaceRuntimeCoordinator.observePlacement().collectAsStateWithLifecycle(
                initialValue = WorkspaceAuthoritativePlacementState.WaitingForInitialization
            )
            val pagedHome by workspaceRuntimeCoordinator.observeHomePages().collectAsStateWithLifecycle(
                initialValue = WorkspacePagedHomeState.WaitingForRoom
            )
            val isDefaultHome by defaultHomeState.collectAsStateWithLifecycle()

            val workspace = when (val current = placement) {
                WorkspaceAuthoritativePlacementState.WaitingForInitialization -> WorkspaceState()
                is WorkspaceAuthoritativePlacementState.RecoveryRequired -> WorkspaceState(
                    initialized = true,
                    authority = WorkspaceAuthority.ROOM,
                )
                is WorkspaceAuthoritativePlacementState.Ready -> WorkspaceState(
                    initialized = true,
                    favoriteKeys = current.snapshot.favoriteKeys,
                    dockKeys = current.snapshot.dockKeys,
                    authority = when (current.snapshot.source) {
                        WorkspacePlacementSource.DATASTORE -> WorkspaceAuthority.DATASTORE
                        WorkspacePlacementSource.ROOM -> WorkspaceAuthority.ROOM
                    },
                )
            }
            val renderedPages = (pagedHome as? WorkspacePagedHomeState.Ready)?.pages.orEmpty()
            var selectedHomePageId by rememberSaveable {
                mutableStateOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID)
            }
            var primarySurfaceModeName by rememberSaveable {
                mutableStateOf(LauncherSurfaceMode.HOME.name)
            }
            val primarySurfaceMode = runCatching {
                LauncherSurfaceMode.valueOf(primarySurfaceModeName)
            }.getOrDefault(LauncherSurfaceMode.HOME)

            LaunchedEffect(renderedPages) {
                if (renderedPages.isNotEmpty() && renderedPages.none { it.pageId == selectedHomePageId }) {
                    selectedHomePageId = renderedPages
                        .firstOrNull { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
                        ?.pageId
                        ?: renderedPages.first().pageId
                }
            }

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
                workspace.initialized,
                workspace.authority,
                workspace.favoriteKeys,
                workspace.dockKeys,
            ) {
                if (workspace.initialized) {
                    workspaceRuntimeCoordinator.reconcileAndActivate()
                }
            }

            GlazeTheme(themeMode) {
                Box {
                    val selectedPage = renderedPages.firstOrNull { it.pageId == selectedHomePageId }
                    val onPrimaryPage = selectedPage == null ||
                        selectedPage.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
                    val showingHome = !onPrimaryPage || primarySurfaceMode == LauncherSurfaceMode.HOME

                    if (!onPrimaryPage && selectedPage != null) {
                        ReadOnlyPagedHomeSurface(
                            apps = apps,
                            page = selectedPage,
                            pages = renderedPages,
                            homeColumns = launcherPreferences.homeColumns,
                            showLabels = launcherPreferences.showLabels,
                            iconScale = launcherPreferences.iconScale,
                            layoutLocked = launcherPreferences.layoutLocked,
                            onLaunchApp = appsRepository::launch,
                            onMoveAppToPage = { app, targetPageId ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        val result = workspaceRuntimeCoordinator.moveHomeAppToPage(
                                            sourcePageId = selectedPage.pageId,
                                            appKey = app.workspaceKey(),
                                            targetPageId = targetPageId,
                                        )
                                        if (result is WorkspacePagedRoomMutationResult.UpdatedItem) {
                                            selectedHomePageId = result.pageId
                                        }
                                    }
                                }
                            },
                            onMoveAppWithinPage = { app, direction ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.moveHomeAppWithinPage(
                                            pageId = selectedPage.pageId,
                                            appKey = app.workspaceKey(),
                                            direction = direction,
                                        )
                                    }
                                }
                            },
                            onMoveAppOneCell = { app, direction ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.moveHomeAppOneCellWithinPage(
                                            pageId = selectedPage.pageId,
                                            appKey = app.workspaceKey(),
                                            direction = direction,
                                        )
                                    }
                                }
                            },
                        )
                    } else {
                        LauncherBetaRoot(
                            apps = apps,
                            workspace = workspace,
                            preferences = launcherPreferences,
                            isDefaultHome = isDefaultHome,
                            onRequestHomeRole = ::requestHomeRole,
                            onLaunchApp = appsRepository::launch,
                            onOpenUniversalSearch = ::openUniversalSearch,
                            onToggleFavorite = { app ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.toggleFavorite(app.workspaceKey())
                                    }
                                }
                            },
                            onToggleDock = { app ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.toggleDock(app.workspaceKey())
                                    }
                                }
                            },
                            onMoveFavorite = { app, direction ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.moveFavorite(app.workspaceKey(), direction)
                                    }
                                }
                            },
                            onMoveDock = { app, direction ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.moveDock(app.workspaceKey(), direction)
                                    }
                                }
                            },
                            themeMode = themeMode,
                            onCycleTheme = themeRepository::cycleMode,
                            onSetHomeGrid = launcherPreferencesRepository::setHomeGrid,
                            onSetDrawerColumns = launcherPreferencesRepository::setDrawerColumns,
                            onSetShowLabels = launcherPreferencesRepository::setShowLabels,
                            onSetIconScale = launcherPreferencesRepository::setIconScale,
                            onSetLayoutLocked = launcherPreferencesRepository::setLayoutLocked,
                            onSetIndexHomeMode = launcherPreferencesRepository::setIndexHomeMode,
                            onSurfaceModeChanged = { mode ->
                                primarySurfaceModeName = mode.name
                            },
                        )
                    }

                    val showPageSwitcher = renderedPages.isNotEmpty() && showingHome
                    if (showPageSwitcher) {
                        HomePageSwitcher(
                            pages = renderedPages,
                            selectedPageId = selectedHomePageId,
                            onSelectPage = { selectedHomePageId = it },
                            onMovePage = { pageId, targetRank ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.moveHomePage(pageId, targetRank)
                                    }
                                }
                            },
                            onCreatePage = {
                                if (!launcherPreferences.layoutLocked) {
                                    val pageId = "home:user:${UUID.randomUUID()}"
                                    lifecycleScope.launch {
                                        val result = workspaceRuntimeCoordinator.createHomePage(pageId)
                                        if (result is WorkspacePagedRoomMutationResult.CreatedPage) {
                                            selectedHomePageId = result.pageId
                                        }
                                    }
                                }
                            },
                            onDeletePage = { pageId ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        val result = workspaceRuntimeCoordinator.deleteEmptyHomePage(pageId)
                                        if (
                                            result is WorkspacePagedRoomMutationResult.DeletedPage &&
                                            selectedHomePageId == result.pageId
                                        ) {
                                            selectedHomePageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID
                                        }
                                    }
                                }
                            },
                            layoutLocked = launcherPreferences.layoutLocked,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }

                    if (launcherPreferences.layoutLocked && showingHome) {
                        LayoutLockHoldControl(
                            locked = true,
                            onUnlock = { launcherPreferencesRepository.setLayoutLocked(false) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 72.dp, end = 12.dp),
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHomeRoleState()
        if (
            ::workspaceRuntimeCoordinator.isInitialized &&
            LauncherPortableRestoreStartupGate.allowsMutations(portableRestoreRecoveryResult.value)
        ) {
            lifecycleScope.launch {
                workspaceRuntimeCoordinator.reconcileAndActivate()
            }
        }
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

    private fun openUniversalSearch() {
        if (!indexIntegration.openSearch()) {
            Toast.makeText(
                this,
                "GoreeCloud Index is not installed yet",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}
