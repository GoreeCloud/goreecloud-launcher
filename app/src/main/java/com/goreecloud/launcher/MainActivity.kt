package com.goreecloud.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goreecloud.launcher.core.launcher.LauncherAppWidgetHostController
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.core.launcher.LauncherBuiltInWallpaperId
import com.goreecloud.launcher.core.launcher.LauncherBuiltInWallpapers
import com.goreecloud.launcher.core.launcher.LauncherDrawerLayoutMode
import com.goreecloud.launcher.core.launcher.LauncherDockStyle
import com.goreecloud.launcher.core.launcher.LauncherConnectedSearchProviderRegistry
import com.goreecloud.launcher.core.launcher.LauncherExperiencePreferences
import com.goreecloud.launcher.core.launcher.LauncherFileSearchPreferencesRepository
import com.goreecloud.launcher.core.launcher.LauncherFilesSearchProvider
import com.goreecloud.launcher.core.launcher.LauncherInstalledAppBaselineRepository
import com.goreecloud.launcher.core.launcher.LauncherLaunchShortcutSearchAction
import com.goreecloud.launcher.core.launcher.LauncherLocalSearchPermissions
import com.goreecloud.launcher.core.launcher.LauncherLocalUsageRepository
import com.goreecloud.launcher.core.launcher.LauncherOpenDocumentSearchAction
import com.goreecloud.launcher.core.launcher.LauncherOpenUriSearchAction
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreRecoveryCoordinator
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupGate
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupSequence
import com.goreecloud.launcher.core.launcher.LauncherPreferencesRepository
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderControlState
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderPreferenceDecodeResult
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderPreferenceSnapshot
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderUserControlPolicy
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderPreferencesRepository
import com.goreecloud.launcher.core.launcher.LauncherWallpaperShade
import com.goreecloud.launcher.core.launcher.StarterWorkspaceCandidate
import com.goreecloud.launcher.core.launcher.StarterWorkspacePolicy
import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.WorkspaceWidgetDescriptor
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativePlacementState
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativeWriteResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedHomeState
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedRoomMutationResult
import com.goreecloud.launcher.core.workspace.db.WorkspacePlacementSource
import com.goreecloud.launcher.core.workspace.db.WorkspacePrimaryHomeSpatialResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceProductionRuntimeCoordinator
import com.goreecloud.launcher.core.workspace.db.WorkspaceRenderedHomeWidget
import com.goreecloud.launcher.core.workspace.db.WorkspaceWidgetMutationResult
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var appsRepository: LauncherAppsRepository
    private lateinit var launcherPreferencesRepository: LauncherPreferencesRepository
    private lateinit var searchProviderPreferencesRepository: LauncherSearchProviderPreferencesRepository
    private lateinit var fileSearchPreferencesRepository: LauncherFileSearchPreferencesRepository
    private lateinit var installedAppBaselineRepository: LauncherInstalledAppBaselineRepository
    private lateinit var localUsageRepository: LauncherLocalUsageRepository
    private lateinit var appWidgetHostController: LauncherAppWidgetHostController
    private lateinit var themeRepository: GlazeThemeRepository
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var workspaceRuntimeCoordinator: WorkspaceProductionRuntimeCoordinator
    private val defaultHomeState = MutableStateFlow(false)
    private val homeResetSequence = MutableStateFlow(0L)
    private val searchProviderPreferencesState =
        MutableStateFlow<LauncherSearchProviderPreferenceDecodeResult?>(null)
    private val portableRestoreRecoveryResult =
        MutableStateFlow<LauncherPortableRestoreRecoveryCoordinator.Result?>(null)
    private var pendingAppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var pendingSearchProviderSnapshot: LauncherSearchProviderPreferenceSnapshot? = null
    private var pendingFileSearchProviderSnapshot: LauncherSearchProviderPreferenceSnapshot? = null

    private val fileSearchRootRequest =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            val pending = pendingFileSearchProviderSnapshot
            pendingFileSearchProviderSnapshot = null
            if (uri == null) return@registerForActivityResult

            val persisted = runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }.isSuccess
            if (!persisted) {
                Toast.makeText(
                    this,
                    "Android did not grant persistent access to that folder.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                fileSearchPreferencesRepository.addRoot(uri)
                if (pending != null) {
                    searchProviderPreferencesRepository.set(pending)
                }
            }
        }

    private val searchSourcePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val pending = pendingSearchProviderSnapshot
            pendingSearchProviderSnapshot = null
            if (granted && pending != null) {
                lifecycleScope.launch {
                    searchProviderPreferencesRepository.set(pending)
                }
            } else if (!granted) {
                Toast.makeText(
                    this,
                    "That Search source remains disabled until Android permission is granted.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    private val widgetConfigureRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val appWidgetId = widgetResultId(result.data)
            if (result.resultCode == Activity.RESULT_OK && appWidgetId > 0) {
                persistAndroidWidget(appWidgetId)
            } else {
                discardPendingAppWidget(appWidgetId)
            }
        }

    private val widgetPickerRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val appWidgetId = widgetResultId(result.data)
            if (result.resultCode != Activity.RESULT_OK || appWidgetId <= 0) {
                discardPendingAppWidget(appWidgetId)
                return@registerForActivityResult
            }

            val info = appWidgetHostController.providerInfo(appWidgetId)
            if (info == null) {
                discardPendingAppWidget(appWidgetId)
                Toast.makeText(this, "That widget is no longer available.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val configure = info.configure
            if (configure != null) {
                pendingAppWidgetId = appWidgetId
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                runCatching { widgetConfigureRequest.launch(intent) }.onFailure {
                    discardPendingAppWidget(appWidgetId)
                    Toast.makeText(
                        this,
                        "That widget could not be configured.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            } else {
                persistAndroidWidget(appWidgetId)
            }
        }

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
        searchProviderPreferencesRepository = LauncherSearchProviderPreferencesRepository(this)
        fileSearchPreferencesRepository = LauncherFileSearchPreferencesRepository(this)
        installedAppBaselineRepository = LauncherInstalledAppBaselineRepository(this)
        localUsageRepository = LauncherLocalUsageRepository(this)
        appWidgetHostController = LauncherAppWidgetHostController(this)
        themeRepository = GlazeThemeRepository(this)
        workspaceRepository = WorkspaceRepository(this)
        workspaceRuntimeCoordinator = WorkspaceProductionRuntimeCoordinator(
            authorityRepository = workspaceRepository,
            workspaceDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceDao()
            },
        )
        lifecycleScope.launch {
            searchProviderPreferencesRepository.preferences.collect { decoded ->
                searchProviderPreferencesState.value = decoded
            }
        }
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
            val localLaunchCounts by localUsageRepository.launchCounts.collectAsStateWithLifecycle(
                initialValue = emptyMap(),
            )
            val searchProviderPreferences by searchProviderPreferencesState.collectAsStateWithLifecycle()
            val fileSearchRoots by fileSearchPreferencesRepository.roots.collectAsStateWithLifecycle(
                initialValue = emptyList(),
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

            val launchApp: (LauncherActivityInfo) -> Unit = { app ->
                appsRepository.launch(app)
                if (experiencePreferences.useLocalUsageForSuggestions) {
                    localUsageRepository.recordLaunch(app.workspaceKey())
                }
            }

            LaunchedEffect(
                apps,
                experiencePreferences.addNewAppsToHome,
                launcherPreferences.layoutLocked,
                launcherPreferences.homeColumns,
                launcherPreferences.homeRows,
                workspace.authority,
                workspace.favoriteKeys,
            ) {
                val primaryAppsByKey = apps
                    .asSequence()
                    .filter {
                        it.user == Process.myUserHandle() &&
                            it.componentName.packageName != packageName
                    }
                    .associateBy { it.workspaceKey() }
                val baseline = installedAppBaselineRepository.reconcile(
                    primaryAppsByKey.keys,
                )

                if (
                    !baseline.initializedBefore ||
                    !experiencePreferences.addNewAppsToHome ||
                    launcherPreferences.layoutLocked ||
                    workspace.authority != WorkspaceAuthority.ROOM
                ) {
                    return@LaunchedEffect
                }

                for (appKey in baseline.newAppKeys) {
                    if (appKey in workspace.favoriteKeys) continue
                    if (primaryAppsByKey[appKey] == null) continue
                    workspaceRuntimeCoordinator.toggleFavorite(
                        key = appKey,
                        homeColumns = launcherPreferences.homeColumns,
                        homeRows = launcherPreferences.homeRows,
                    )
                }
            }

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
                workspace.authority,
                workspace.favoriteKeys,
                workspace.dockKeys,
                launcherPreferences.homeColumns,
                launcherPreferences.homeRows,
                experiencePreferences.starterLayoutApplied,
                experiencePreferences.useLocalUsageForSuggestions,
                localLaunchCounts,
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
                                localLaunchCount = if (
                                    experiencePreferences.useLocalUsageForSuggestions
                                ) {
                                    localLaunchCounts[app.workspaceKey()] ?: 0L
                                } else {
                                    0L
                                },
                            )
                        },
                )

                if (!workspace.initialized) {
                    workspaceRepository.ensureDefaults(
                        favoriteKeys = starterSelection.favoriteKeys,
                        dockKeys = starterSelection.dockKeys,
                    )
                } else if (workspace.favoriteKeys.isEmpty() && workspace.dockKeys.isEmpty()) {
                    var seeded = true
                    for (key in starterSelection.favoriteKeys) {
                        if (
                            workspaceRuntimeCoordinator.toggleFavorite(
                                key = key,
                                homeColumns = launcherPreferences.homeColumns,
                                homeRows = launcherPreferences.homeRows,
                            ) !is WorkspaceAuthoritativeWriteResult.Written
                        ) {
                            seeded = false
                            break
                        }
                    }
                    if (seeded) {
                        for (key in starterSelection.dockKeys) {
                            if (
                                workspaceRuntimeCoordinator.toggleDock(key) !is
                                    WorkspaceAuthoritativeWriteResult.Written
                            ) {
                                seeded = false
                                break
                            }
                        }
                    }
                    if (!seeded) return@LaunchedEffect
                } else if (
                    workspace.favoriteKeys != starterSelection.favoriteKeys ||
                    workspace.dockKeys != starterSelection.dockKeys
                ) {
                    launcherPreferencesRepository.markStarterLayoutApplied()
                    return@LaunchedEffect
                }

                workspaceRuntimeCoordinator.reconcileAndActivate()
                val ready = workspaceRuntimeCoordinator.ensurePrimaryHomeSpatialGrid(
                    columns = launcherPreferences.homeColumns,
                    rows = launcherPreferences.homeRows,
                )
                if (ready !is WorkspacePrimaryHomeSpatialResult.Ready) {
                    return@LaunchedEffect
                }

                val cells = StarterWorkspacePolicy.homeCells(
                    itemCount = starterSelection.favoriteKeys.size,
                    columns = launcherPreferences.homeColumns,
                    rows = launcherPreferences.homeRows,
                )
                var positioned = true
                for ((key, cell) in starterSelection.favoriteKeys.zip(cells)) {
                    if (
                        workspaceRuntimeCoordinator.movePrimaryHomeAppToCell(
                            appKey = key,
                            columns = launcherPreferences.homeColumns,
                            rows = launcherPreferences.homeRows,
                            cellX = cell.first,
                            cellY = cell.second,
                        ) !is WorkspacePrimaryHomeSpatialResult.Moved
                    ) {
                        positioned = false
                        break
                    }
                }
                if (positioned) {
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
                            onLaunchApp = launchApp,
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
                            searchProviderPreferences = searchProviderPreferences,
                            homePageCount = renderedPages.size.coerceAtLeast(1),
                            homeResetSequence = homeResetSequenceValue,
                            homeLabelOverrides = homeLabelOverrides,
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
                            onLaunchApp = launchApp,
                            onOpenAppInfo = appsRepository::openDetails,
                            onAddBuiltInWidget = ::addBuiltInWidget,
                            onPickAndroidWidget = ::beginAndroidWidgetPick,
                            onCreateAndroidWidgetView = appWidgetHostController::createHostView,
                            onRemoveWidget = ::removeWidget,
                            onResizeWidget = ::resizeWidget,
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
                            onMoveFavoriteToDock = { app, targetDockKey ->
                                if (!launcherPreferences.layoutLocked) {
                                    val key = app.workspaceKey()
                                    if (
                                        key !in workspace.dockKeys &&
                                        workspace.dockKeys.size >= MAX_DOCK_ITEMS
                                    ) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Dock is full.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        lifecycleScope.launch {
                                            workspaceRuntimeCoordinator.moveHomeToDock(
                                                key = key,
                                                targetDockKey = targetDockKey,
                                            )
                                        }
                                    }
                                }
                            },
                            onMoveDockToHomeCell = { app, cellX, cellY ->
                                if (!launcherPreferences.layoutLocked) {
                                    val key = app.workspaceKey()
                                    if (
                                        key !in workspace.favoriteKeys &&
                                        workspace.favoriteKeys.size >= launcherPreferences.homeCapacity
                                    ) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Home screen is full.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        lifecycleScope.launch {
                                            workspaceRuntimeCoordinator.moveDockToPrimaryHomeCell(
                                                key = key,
                                                columns = launcherPreferences.homeColumns,
                                                rows = launcherPreferences.homeRows,
                                                cellX = cellX,
                                                cellY = cellY,
                                            )
                                        }
                                    }
                                }
                            },
                            onCopyDrawerToHomeCell = { app, cellX, cellY ->
                                if (!launcherPreferences.layoutLocked) {
                                    val key = app.workspaceKey()
                                    if (
                                        key !in workspace.favoriteKeys &&
                                        workspace.favoriteKeys.size >= launcherPreferences.homeCapacity
                                    ) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Home screen is full.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        lifecycleScope.launch {
                                            workspaceRuntimeCoordinator.copyDrawerToPrimaryHomeCell(
                                                key = key,
                                                columns = launcherPreferences.homeColumns,
                                                rows = launcherPreferences.homeRows,
                                                cellX = cellX,
                                                cellY = cellY,
                                            )
                                        }
                                    }
                                }
                            },
                            onCopyDrawerToDock = { app, targetDockKey ->
                                if (!launcherPreferences.layoutLocked) {
                                    val key = app.workspaceKey()
                                    if (
                                        key !in workspace.dockKeys &&
                                        workspace.dockKeys.size >= MAX_DOCK_ITEMS
                                    ) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Dock is full.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        lifecycleScope.launch {
                                            workspaceRuntimeCoordinator.copyDrawerToDock(
                                                key = key,
                                                targetDockKey = targetDockKey,
                                            )
                                        }
                                    }
                                }
                            },
                            onReorderDockByDrop = { app, targetDockKey ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        workspaceRuntimeCoordinator.reorderDockByDrop(
                                            key = app.workspaceKey(),
                                            targetDockKey = targetDockKey,
                                        )
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
                            onSetSearchProviderPreferences = { snapshot ->
                                lifecycleScope.launch {
                                    searchProviderPreferencesRepository.set(snapshot)
                                }
                            },
                            onSetSearchProviderEnabled = ::setSearchProviderEnabled,
                            onLaunchSearchShortcut = { action ->
                                runCatching {
                                    appsRepository.launchShortcut(
                                        packageName = action.packageName,
                                        shortcutId = action.shortcutId,
                                        user = action.user,
                                    )
                                }.onFailure {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "That shortcut is no longer available.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                            onOpenSearchUri = ::openSearchUri,
                            onResetSearchProviderPreferences = {
                                lifecycleScope.launch {
                                    searchProviderPreferencesRepository.clear()
                                }
                            },
                            onSetHomeCardStyle = launcherPreferencesRepository::setHomeCardStyle,
                            onSetShowHomeQuickActions = launcherPreferencesRepository::setShowHomeQuickActions,
                            onSetShowHomePageIndicator = launcherPreferencesRepository::setShowHomePageIndicator,
                            onSetUseLocalUsageForSuggestions =
                                launcherPreferencesRepository::setUseLocalUsageForSuggestions,
                            onSetAddNewAppsToHome =
                                launcherPreferencesRepository::setAddNewAppsToHome,
                            onClearLocalUsage = {
                                localUsageRepository.clear()
                                Unit
                            },
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

    override fun onStart() {
        super.onStart()
        if (::appWidgetHostController.isInitialized) {
            runCatching { appWidgetHostController.startListening() }
        }
    }

    override fun onStop() {
        if (::appWidgetHostController.isInitialized) {
            runCatching { appWidgetHostController.stopListening() }
        }
        super.onStop()
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

    private fun beginAndroidWidgetPick() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
            Toast.makeText(this, "Android widgets are not supported on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        val appWidgetId = runCatching { appWidgetHostController.allocateAppWidgetId() }
            .getOrElse {
                Toast.makeText(this, "A widget ID could not be allocated.", Toast.LENGTH_SHORT).show()
                return
            }
        pendingAppWidgetId = appWidgetId
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        runCatching { widgetPickerRequest.launch(intent) }.onFailure {
            discardPendingAppWidget(appWidgetId)
            Toast.makeText(this, "No Android widget picker is available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun widgetResultId(data: Intent?): Int {
        val returned = data?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        return if (returned > 0) returned else pendingAppWidgetId
    }

    private fun discardPendingAppWidget(appWidgetId: Int) {
        val id = if (appWidgetId > 0) appWidgetId else pendingAppWidgetId
        pendingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        if (::appWidgetHostController.isInitialized) {
            appWidgetHostController.deleteAppWidgetId(id)
        }
    }

    private fun persistAndroidWidget(appWidgetId: Int) {
        pendingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        val info = appWidgetHostController.providerInfo(appWidgetId)
        if (info == null) {
            appWidgetHostController.deleteAppWidgetId(appWidgetId)
            return
        }

        lifecycleScope.launch {
            val preferences = launcherPreferencesRepository.preferences.first()
            val result = workspaceRuntimeCoordinator.addAndroidWidget(
                itemId = "widget:android:${UUID.randomUUID()}",
                appWidgetId = appWidgetId,
                providerComponent = info.provider.flattenToString(),
                columns = preferences.homeColumns,
                rows = preferences.homeRows,
            )
            if (result !is WorkspaceWidgetMutationResult.Added) {
                appWidgetHostController.deleteAppWidgetId(appWidgetId)
                Toast.makeText(
                    this@MainActivity,
                    if (result == WorkspaceWidgetMutationResult.NoSpace) {
                        "There is not enough room on Home for that widget."
                    } else {
                        "That widget could not be added."
                    },
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun addBuiltInWidget(typeId: String) {
        lifecycleScope.launch {
            val preferences = launcherPreferencesRepository.preferences.first()
            val result = workspaceRuntimeCoordinator.addBuiltInWidget(
                itemId = "widget:builtin:${UUID.randomUUID()}",
                typeId = typeId,
                columns = preferences.homeColumns,
                rows = preferences.homeRows,
            )
            if (result !is WorkspaceWidgetMutationResult.Added) {
                Toast.makeText(
                    this@MainActivity,
                    if (result == WorkspaceWidgetMutationResult.NoSpace) {
                        "There is not enough room on Home for that widget."
                    } else {
                        "That widget could not be added."
                    },
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun removeWidget(widget: WorkspaceRenderedHomeWidget) {
        lifecycleScope.launch {
            val result = workspaceRuntimeCoordinator.removeWidget(widget.itemId)
            if (result is WorkspaceWidgetMutationResult.Removed) {
                val descriptor = widget.descriptor
                if (descriptor is WorkspaceWidgetDescriptor.Android) {
                    appWidgetHostController.deleteAppWidgetId(descriptor.appWidgetId)
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "That widget could not be removed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun resizeWidget(
        widget: WorkspaceRenderedHomeWidget,
        spanX: Int,
        spanY: Int,
    ) {
        lifecycleScope.launch {
            val preferences = launcherPreferencesRepository.preferences.first()
            val result = workspaceRuntimeCoordinator.resizeWidget(
                itemId = widget.itemId,
                columns = preferences.homeColumns,
                rows = preferences.homeRows,
                spanX = spanX,
                spanY = spanY,
            )
            if (result !is WorkspaceWidgetMutationResult.Resized) {
                Toast.makeText(
                    this@MainActivity,
                    if (result == WorkspaceWidgetMutationResult.NoSpace) {
                        "That widget size does not fit the current Home layout."
                    } else {
                        "That widget could not be resized."
                    },
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun setSearchProviderEnabled(
        state: LauncherSearchProviderControlState,
        providerId: String,
        enabled: Boolean,
    ) {
        val snapshot = LauncherSearchProviderUserControlPolicy.withProviderEnabled(
            state = state,
            providerId = providerId,
            enabled = enabled,
        )
        val permission = LauncherLocalSearchPermissions.permissionFor(providerId)
        if (
            enabled &&
            permission != null &&
            ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            pendingSearchProviderSnapshot = snapshot
            searchSourcePermissionRequest.launch(permission)
            return
        }

        lifecycleScope.launch {
            searchProviderPreferencesRepository.set(snapshot)
        }
    }

    private fun openSearchUri(action: LauncherOpenUriSearchAction) {
        val intent = Intent(action.intentAction, Uri.parse(action.uri))
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(
                this,
                "No compatible app is available for this result.",
                Toast.LENGTH_SHORT,
            ).show()
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

    private fun applyBuiltInWallpaper(id: LauncherBuiltInWallpaperId) {
        lifecycleScope.launch(Dispatchers.Default) {
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels.coerceAtLeast(1080)
            val height = metrics.heightPixels.coerceAtLeast(1920)
            val bitmap = LauncherBuiltInWallpapers.render(id, width, height)
            val result = runCatching {
                WallpaperManager.getInstance(this@MainActivity).setBitmap(
                    bitmap,
                    null,
                    true,
                    WallpaperManager.FLAG_SYSTEM,
                )
            }
            bitmap.recycle()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    if (result.isSuccess) "Wallpaper applied" else "Wallpaper could not be applied",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun openWallpaperPicker() {
        val wallpapers = LauncherBuiltInWallpapers.all
        val labels = wallpapers
            .map { wallpaper -> wallpaper.name + "\n" + wallpaper.description }
            .plus("More wallpapers from Android")
            .toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("GoreeCloud Wallpapers")
            .setItems(labels) { _, index ->
                if (index < wallpapers.size) {
                    applyBuiltInWallpaper(wallpapers[index].id)
                } else {
                    openSystemWallpaperPicker()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openSystemWallpaperPicker() {
        runCatching {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SET_WALLPAPER),
                    "Choose wallpaper",
                ),
            )
        }.onFailure {
            Toast.makeText(
                this,
                "No wallpaper picker is available",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

}
