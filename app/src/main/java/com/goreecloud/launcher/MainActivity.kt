package com.goreecloud.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.core.launcher.LauncherDrawerLayoutMode
import com.goreecloud.launcher.core.launcher.LauncherDockStyle
import com.goreecloud.launcher.core.launcher.LauncherExperiencePreferences
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreRecoveryCoordinator
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupGate
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupSequence
import com.goreecloud.launcher.core.launcher.LauncherPreferencesRepository
import com.goreecloud.launcher.core.launcher.LauncherWallpaperShade
import com.goreecloud.launcher.core.launcher.StarterWorkspaceCandidate
import com.goreecloud.launcher.core.launcher.StarterWorkspacePolicy
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativePlacementState
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativeWriteResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedHomeState
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedRoomMutationResult
import com.goreecloud.launcher.core.workspace.db.WorkspacePlacementSource
import com.goreecloud.launcher.core.workspace.db.WorkspacePrimaryHomeSpatialResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceProductionRuntimeCoordinator
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.HomePageDots
import com.goreecloud.launcher.ui.HomePageSwitcher
import com.goreecloud.launcher.ui.LayoutLockHoldControl
import com.goreecloud.launcher.ui.LauncherBetaRoot
import com.goreecloud.launcher.ui.LauncherSurfaceMode
import com.goreecloud.launcher.ui.LauncherTransitionDiagnostics
import com.goreecloud.launcher.ui.ReadOnlyPagedHomeSurface
import com.goreecloud.launcher.ui.theme.GlazeTheme
import com.goreecloud.launcher.ui.theme.GlazeThemeRepository
import com.goreecloud.launcher.ui.theme.rememberAndroidGlazeV16PresentationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var appsRepository: LauncherAppsRepository
    private lateinit var launcherPreferencesRepository: LauncherPreferencesRepository
    private lateinit var themeRepository: GlazeThemeRepository
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var workspaceRuntimeCoordinator: WorkspaceProductionRuntimeCoordinator
    private val defaultHomeState = MutableStateFlow(false)
    private val homeResetSequence = MutableStateFlow(0L)
    private val portableRestoreRecoveryResult =
        MutableStateFlow<LauncherPortableRestoreRecoveryCoordinator.Result?>(null)

    private val homeRoleRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshHomeRoleState()
        }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (LauncherHomeIntentPolicy.shouldResetToPrimaryHome(intent.action, intent.categories)) {
            homeResetSequence.value = homeResetSequence.value + 1L
        }
        refreshHomeRoleState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appsRepository = LauncherAppsRepository(this)
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
                val glazePresentationContext = rememberAndroidGlazeV16PresentationContext()
            GlazeTheme(themeMode, presentationContext = glazePresentationContext) {
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
            val drawerLayoutMode by launcherPreferencesRepository.drawerLayoutMode.collectAsStateWithLifecycle(
                initialValue = LauncherDrawerLayoutMode.GRID,
            )
            val experiencePreferences by launcherPreferencesRepository.experiencePreferences.collectAsStateWithLifecycle(
                initialValue = LauncherExperiencePreferences(),
            )
            val homeLabelOverrides by launcherPreferencesRepository.homeLabelOverrides.collectAsStateWithLifecycle(
                initialValue = emptyMap(),
            )
            val placement by workspaceRuntimeCoordinator.observePlacement().collectAsStateWithLifecycle(
                initialValue = WorkspaceAuthoritativePlacementState.WaitingForInitialization
            )
            val pagedHome by workspaceRuntimeCoordinator.observeHomePages().collectAsStateWithLifecycle(
                initialValue = WorkspacePagedHomeState.WaitingForRoom
            )
            val isDefaultHome by defaultHomeState.collectAsStateWithLifecycle()
            val homeResetSequenceValue by homeResetSequence.collectAsStateWithLifecycle()

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

            LaunchedEffect(homeResetSequenceValue) {
                selectedHomePageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID
                primarySurfaceModeName = LauncherSurfaceMode.HOME.name
            }

            LaunchedEffect(renderedPages) {
                if (renderedPages.isNotEmpty() && renderedPages.none { it.pageId == selectedHomePageId }) {
                    selectedHomePageId = renderedPages
                        .firstOrNull { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
                        ?.pageId
                        ?: renderedPages.first().pageId
                }
            }

            LaunchedEffect(
                apps,
                workspace.initialized,
                experiencePreferences.starterLayoutApplied,
            ) {
                if (apps.isEmpty() || experiencePreferences.starterLayoutApplied) {
                    return@LaunchedEffect
                }

                val starterSelection = StarterWorkspacePolicy.select(
                    apps
                        .filterNot { it.componentName.packageName == packageName }
                        .map { app ->
                            StarterWorkspaceCandidate(
                                key = app.workspaceKey(),
                                label = app.label.toString(),
                                packageName = app.componentName.packageName,
                            )
                        },
                )

                if (!workspace.initialized) {
                    workspaceRepository.ensureDefaults(
                        favoriteKeys = starterSelection.favoriteKeys,
                        dockKeys = starterSelection.dockKeys,
                    )
                    launcherPreferencesRepository.markStarterLayoutApplied()
                } else if (workspace.favoriteKeys.isEmpty() && workspace.dockKeys.isEmpty()) {
                    var seeded = true
                    for (key in starterSelection.favoriteKeys) {
                        if (workspaceRuntimeCoordinator.toggleFavorite(key) !is
                            WorkspaceAuthoritativeWriteResult.Written
                        ) {
                            seeded = false
                            break
                        }
                    }
                    if (seeded) {
                        for (key in starterSelection.dockKeys) {
                            if (workspaceRuntimeCoordinator.toggleDock(key) !is
                                WorkspaceAuthoritativeWriteResult.Written
                            ) {
                                seeded = false
                                break
                            }
                        }
                    }
                    if (seeded) {
                        launcherPreferencesRepository.markStarterLayoutApplied()
                    }
                } else {
                    // Existing user placement always wins over the one-time Development starter.
                    launcherPreferencesRepository.markStarterLayoutApplied()
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
                            homeLabelOverrides = homeLabelOverrides,
                            onLaunchApp = appsRepository::launch,
                            onSetHomeLabelOverride = { app, label ->
                                launcherPreferencesRepository.setHomeLabelOverride(app.workspaceKey(), label)
                            },
                            onRequestUninstall = ::requestUninstall,
                            onMoveAppToPage = { app, targetPageId ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        val result = workspaceRuntimeCoordinator.moveHomeAppToPage(
                                            sourcePageId = selectedPage.pageId,
                                            appKey = app.workspaceKey(),
                                            targetPageId = targetPageId,
                                            homeColumns = launcherPreferences.homeColumns,
                                            homeRows = launcherPreferences.homeRows,
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
                            drawerLayoutMode = drawerLayoutMode,
                            experiencePreferences = experiencePreferences,
                            homePageCount = renderedPages.size.coerceAtLeast(1),
                            homeResetSequence = homeResetSequenceValue,
                            homeLabelOverrides = homeLabelOverrides,
                            homePages = renderedPages,
                            primaryHomePage = renderedPages.firstOrNull {
                                it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
                            },
                            onManageHomePages = {
                                val secondaryPage = renderedPages.firstOrNull {
                                    it.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID
                                }
                                if (secondaryPage != null) {
                                    selectedHomePageId = secondaryPage.pageId
                                } else if (!launcherPreferences.layoutLocked) {
                                    val pageId = "home:user:${UUID.randomUUID()}"
                                    lifecycleScope.launch {
                                        val result = workspaceRuntimeCoordinator.createHomePage(pageId)
                                        if (result is WorkspacePagedRoomMutationResult.CreatedPage) {
                                            selectedHomePageId = result.pageId
                                        }
                                    }
                                }
                            },
                            isDefaultHome = isDefaultHome,
                            onRequestHomeRole = ::requestHomeRole,
                            onLaunchApp = appsRepository::launch,
                            onToggleFavorite = { app ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.toggleFavorite(
                                            key = app.workspaceKey(),
                                            homeColumns = launcherPreferences.homeColumns,
                                            homeRows = launcherPreferences.homeRows,
                                        )
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
                            onMoveFavoriteToCell = { app, cellX, cellY ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.movePrimaryHomeAppToCell(
                                            appKey = app.workspaceKey(),
                                            columns = launcherPreferences.homeColumns,
                                            rows = launcherPreferences.homeRows,
                                            cellX = cellX,
                                            cellY = cellY,
                                        )
                                    }
                                }
                            },
                            onMoveFavoriteToPage = { app, targetPageId ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        val result = workspaceRuntimeCoordinator.moveHomeAppToPage(
                                            sourcePageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
                                            appKey = app.workspaceKey(),
                                            targetPageId = targetPageId,
                                            homeColumns = launcherPreferences.homeColumns,
                                            homeRows = launcherPreferences.homeRows,
                                        )
                                        if (result is WorkspacePagedRoomMutationResult.UpdatedItem) {
                                            selectedHomePageId = result.pageId
                                        }
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
                            onSetThemeMode = themeRepository::setMode,
                            onSetHomeGrid = { columns, rows ->
                                if (workspace.authority != WorkspaceAuthority.ROOM) {
                                    launcherPreferencesRepository.setHomeGrid(columns, rows)
                                } else {
                                    lifecycleScope.launch {
                                        when (
                                            workspaceRuntimeCoordinator.ensurePrimaryHomeSpatialGrid(
                                                columns = columns,
                                                rows = rows,
                                            )
                                        ) {
                                            is WorkspacePrimaryHomeSpatialResult.Ready -> {
                                                launcherPreferencesRepository.setHomeGrid(columns, rows)
                                            }
                                            WorkspacePrimaryHomeSpatialResult.Reserved -> Unit
                                            WorkspacePrimaryHomeSpatialResult.Unavailable,
                                            WorkspacePrimaryHomeSpatialResult.InvalidWorkspace,
                                            WorkspacePrimaryHomeSpatialResult.StoredWorkspaceChanged,
                                            is WorkspacePrimaryHomeSpatialResult.Failed,
                                            is WorkspacePrimaryHomeSpatialResult.Moved -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Home grid could not be changed safely.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            },
                            onSetDrawerColumns = launcherPreferencesRepository::setDrawerColumns,
                            onSetDrawerLayoutMode = launcherPreferencesRepository::setDrawerLayoutMode,
                            onSetShowLabels = launcherPreferencesRepository::setShowLabels,
                            onSetIconScale = launcherPreferencesRepository::setIconScale,
                            onSetLayoutLocked = launcherPreferencesRepository::setLayoutLocked,
                            onSetUniversalSearchHomeMode = launcherPreferencesRepository::setUniversalSearchHomeMode,
                            onSetHomeCardStyle = launcherPreferencesRepository::setHomeCardStyle,
                            onSetShowHomeQuickActions = launcherPreferencesRepository::setShowHomeQuickActions,
                            onSetShowHomePageIndicator = launcherPreferencesRepository::setShowHomePageIndicator,
                            onSetDrawerBackdrop = launcherPreferencesRepository::setDrawerBackdrop,
                            onSetDrawerSearchPlacement = launcherPreferencesRepository::setDrawerSearchPlacement,
                            onSetDrawerNavigation = launcherPreferencesRepository::setDrawerNavigation,
                            onSetDrawerEntryMode = launcherPreferencesRepository::setDrawerEntryMode,
                            onSetDrawerSpacing = launcherPreferencesRepository::setDrawerSpacing,
                            onSetDrawerPageRows = launcherPreferencesRepository::setDrawerPageRows,
                            onSetShowDrawerAppCount = launcherPreferencesRepository::setShowDrawerAppCount,
                            onSetHomeGlanceAlignment = launcherPreferencesRepository::setHomeGlanceAlignment,
                            onSetHomeSearchPlacement = launcherPreferencesRepository::setHomeSearchPlacement,
                            onSetHomeSearchStyle = launcherPreferencesRepository::setHomeSearchStyle,
                            onSetHomeSpacing = launcherPreferencesRepository::setHomeSpacing,
                            onSetDockStyle = launcherPreferencesRepository::setDockStyle,
                            onSetWallpaperShade = launcherPreferencesRepository::setWallpaperShade,
                            onSetGestureAction = { gesture, action ->
                                launcherPreferencesRepository.setGestureAction(gesture, action)
                                Unit
                            },
                            onSetHomeLabelOverride = { app, label ->
                                launcherPreferencesRepository.setHomeLabelOverride(app.workspaceKey(), label)
                            },
                            onRequestUninstall = ::requestUninstall,
                            onOpenWallpaperPicker = ::openWallpaperPicker,
                            onSurfaceModeChanged = { mode ->
                                primarySurfaceModeName = mode.name
                                LauncherTransitionDiagnostics.recordSurfaceMode(mode)
                            },
                        )
                    }

                    if (
                        experiencePreferences.showHomePageIndicator &&
                        renderedPages.size > 1 &&
                        showingHome
                    ) {
                        HomePageDots(
                            pages = renderedPages,
                            selectedPageId = selectedHomePageId,
                            onSelectPage = { selectedHomePageId = it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = if (onPrimaryPage) 104.dp else 24.dp),
                        )
                    }

                    // Detailed page-management controls remain available on secondary pages.
                    val showPageSwitcher = renderedPages.size > 1 && showingHome && !onPrimaryPage
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

                    // The layout-lock affordance is intentionally absent from the primary Home
                    // surface. It remains available from Launcher settings without permanently
                    // occupying wallpaper space.
                    if (launcherPreferences.layoutLocked && showingHome && !onPrimaryPage) {
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

    private fun requestUninstall(app: LauncherActivityInfo) {
        if (app.user != Process.myUserHandle()) {
            Toast.makeText(
                this,
                "Uninstall this app from its Android profile.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val intent = Intent(
            Intent.ACTION_DELETE,
            Uri.fromParts("package", app.componentName.packageName, null),
        )
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(
                this,
                "Android uninstall is unavailable for this app.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun openWallpaperPicker() {
        runCatching {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), "Choose wallpaper"))
        }.onFailure {
            Toast.makeText(
                this,
                "No wallpaper picker is available",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

}
