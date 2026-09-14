package com.goreecloud.launcher

import android.app.role.RoleManager
import android.content.Intent
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
import com.goreecloud.launcher.core.launcher.LauncherDrawerLayoutMode
import com.goreecloud.launcher.core.launcher.LauncherDrawerSearchPosition
import com.goreecloud.launcher.core.launcher.LauncherDrawerSortMode
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreRecoveryCoordinator
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupGate
import com.goreecloud.launcher.core.launcher.LauncherPortableRestoreStartupSequence
import com.goreecloud.launcher.core.launcher.LauncherPreferencesRepository
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativePlacementState
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomeBatchMoveCommit
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomeBatchMoveResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomeBatchMoveService
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomePageCompactionResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomePageCompactionService
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedHomeState
import com.goreecloud.launcher.core.workspace.db.WorkspacePagedRoomMutationResult
import com.goreecloud.launcher.core.workspace.db.WorkspacePlacementSource
import com.goreecloud.launcher.core.workspace.db.WorkspaceProductionRuntimeCoordinator
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.HomeEditEntryControl
import com.goreecloud.launcher.ui.HomeMultiSelectEditControl
import com.goreecloud.launcher.ui.HomeOverviewEditSurface
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
    private lateinit var homeBatchMoveService: WorkspaceHomeBatchMoveService
    private lateinit var homePageCompactionService: WorkspaceHomePageCompactionService
    private val defaultHomeState = MutableStateFlow(false)
    private val portableRestoreRecoveryResult =
        MutableStateFlow<LauncherPortableRestoreRecoveryCoordinator.Result?>(null)
    private val homeReturnRequestGeneration = MutableStateFlow(0L)
    private val homeBatchUndoCommit = MutableStateFlow<WorkspaceHomeBatchMoveCommit?>(null)

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
        homeBatchMoveService = WorkspaceHomeBatchMoveService(
            authorityRepository = workspaceRepository,
            workspaceDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceDao()
            },
            batchDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceHomeBatchMoveDao()
            },
        )
        homePageCompactionService = WorkspaceHomePageCompactionService(
            authorityRepository = workspaceRepository,
            workspaceDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceDao()
            },
            batchDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceHomeBatchMoveDao()
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
            val homeReturnGeneration by homeReturnRequestGeneration.collectAsStateWithLifecycle()
            val undoCommit by homeBatchUndoCommit.collectAsStateWithLifecycle()

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
            val drawerLayoutMode by launcherPreferencesRepository.drawerLayoutMode.collectAsStateWithLifecycle(
                initialValue = LauncherDrawerLayoutMode.GRID,
            )
            val drawerSortMode by launcherPreferencesRepository.drawerSortMode.collectAsStateWithLifecycle(
                initialValue = LauncherDrawerSortMode.NAME_ASC,
            )
            val drawerSearchPosition by launcherPreferencesRepository.drawerSearchPosition.collectAsStateWithLifecycle(
                initialValue = LauncherDrawerSearchPosition.TOP,
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
            var homeOverviewOpen by rememberSaveable { mutableStateOf(false) }
            val primarySurfaceMode = runCatching {
                LauncherSurfaceMode.valueOf(primarySurfaceModeName)
            }.getOrDefault(LauncherSurfaceMode.HOME)

            LaunchedEffect(homeReturnGeneration) {
                if (homeReturnGeneration > 0L) {
                    selectedHomePageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID
                    primarySurfaceModeName = LauncherSurfaceMode.HOME.name
                    homeOverviewOpen = false
                }
            }

            LaunchedEffect(renderedPages) {
                if (renderedPages.isEmpty()) {
                    homeOverviewOpen = false
                } else if (renderedPages.none { it.pageId == selectedHomePageId }) {
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

                    val moveHomePage: (String, Int) -> Unit = { pageId, targetRank ->
                        if (!launcherPreferences.layoutLocked) {
                            lifecycleScope.launch {
                                workspaceRuntimeCoordinator.moveHomePage(pageId, targetRank)
                            }
                        }
                    }
                    val createHomePage: () -> Unit = {
                        if (!launcherPreferences.layoutLocked) {
                            val pageId = "home:user:${UUID.randomUUID()}"
                            lifecycleScope.launch {
                                val result = workspaceRuntimeCoordinator.createHomePage(pageId)
                                if (result is WorkspacePagedRoomMutationResult.CreatedPage) {
                                    selectedHomePageId = result.pageId
                                }
                            }
                        }
                    }
                    val deleteHomePage: (String) -> Unit = { pageId ->
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
                    }

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
                            drawerLayoutMode = drawerLayoutMode,
                            drawerSortMode = drawerSortMode,
                            drawerSearchPosition = drawerSearchPosition,
                            isDefaultHome = isDefaultHome,
                            onRequestHomeRole = ::requestHomeRole,
                            onLaunchApp = appsRepository::launch,
                            isIndexSearchAvailable = indexIntegration.isAvailable(),
                            onOpenIndexSearch = ::openUniversalSearch,
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
                            onSetDrawerLayoutMode = launcherPreferencesRepository::setDrawerLayoutMode,
                            onSetDrawerSortMode = launcherPreferencesRepository::setDrawerSortMode,
                            onSetDrawerSearchPosition = launcherPreferencesRepository::setDrawerSearchPosition,
                            onSetShowLabels = launcherPreferencesRepository::setShowLabels,
                            onSetIconScale = launcherPreferencesRepository::setIconScale,
                            onSetLayoutLocked = launcherPreferencesRepository::setLayoutLocked,
                            onSetIndexHomeMode = launcherPreferencesRepository::setIndexHomeMode,
                            onSurfaceModeChanged = { mode ->
                                primarySurfaceModeName = mode.name
                            },
                            homeResetGeneration = homeReturnGeneration,
                        )
                    }

                    if (
                        showingHome &&
                        onPrimaryPage &&
                        renderedPages.isNotEmpty() &&
                        !homeOverviewOpen
                    ) {
                        HomeEditEntryControl(
                            onOpenOverview = { homeOverviewOpen = true },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(start = 12.dp, top = 6.dp),
                        )
                    }

                    // Keep the primary Home page visually quiet outside explicit edit mode.
                    // Additional pages retain the compact page switcher for navigation.
                    val showPageSwitcher =
                        renderedPages.size > 1 && showingHome && !onPrimaryPage && !homeOverviewOpen
                    if (showPageSwitcher) {
                        HomePageSwitcher(
                            pages = renderedPages,
                            selectedPageId = selectedHomePageId,
                            onSelectPage = { selectedHomePageId = it },
                            onMovePage = moveHomePage,
                            onCreatePage = createHomePage,
                            onDeletePage = deleteHomePage,
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
                    if (
                        launcherPreferences.layoutLocked &&
                        showingHome &&
                        !onPrimaryPage &&
                        !homeOverviewOpen
                    ) {
                        LayoutLockHoldControl(
                            locked = true,
                            onUnlock = { launcherPreferencesRepository.setLayoutLocked(false) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 72.dp, end = 12.dp),
                        )
                    }

                    if (homeOverviewOpen) {
                        HomeOverviewEditSurface(
                            pages = renderedPages,
                            selectedPageId = selectedHomePageId,
                            homeColumns = launcherPreferences.homeColumns,
                            homeRows = launcherPreferences.homeRows,
                            layoutLocked = launcherPreferences.layoutLocked,
                            onSelectPage = { selectedHomePageId = it },
                            onMovePage = moveHomePage,
                            onCreatePage = createHomePage,
                            onDeletePage = deleteHomePage,
                            onCompactPage = { pageId ->
                                if (!launcherPreferences.layoutLocked) {
                                    lifecycleScope.launch {
                                        val result = homePageCompactionService.compact(
                                            grid = WorkspaceGridPlacement.Grid(
                                                columns = launcherPreferences.homeColumns,
                                                rows = launcherPreferences.homeRows,
                                            ),
                                            pageId = pageId,
                                        )
                                        when (result) {
                                            is WorkspaceHomePageCompactionResult.Applied -> {
                                                homeBatchUndoCommit.value = null
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Compacted ${result.itemCount} app${if (result.itemCount == 1) "" else "s"} on this Home page.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            WorkspaceHomePageCompactionResult.AlreadyCompact -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "This Home page is already compact.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            WorkspaceHomePageCompactionResult.UnsupportedPageItems -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Compact apps is unavailable because this page contains unsupported or non-1×1 items.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            WorkspaceHomePageCompactionResult.CapacityExceeded -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Compact apps is unavailable because the current Home grid is too small for this page.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            WorkspaceHomePageCompactionResult.StoredWorkspaceChanged -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Home changed before compaction finished, so nothing was rearranged.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "This Home page could not be compacted safely.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            },
                            onDismiss = { homeOverviewOpen = false },
                        )

                        HomeMultiSelectEditControl(
                            page = selectedPage,
                            pages = renderedPages,
                            appLabelsByKey = apps.associate { it.workspaceKey() to it.label.toString() },
                            layoutLocked = launcherPreferences.layoutLocked,
                            undoAvailable = undoCommit != null,
                            onMoveSelectedApps = { sourcePageId, appKeys, targetPageId ->
                                if (!launcherPreferences.layoutLocked && appKeys.isNotEmpty()) {
                                    lifecycleScope.launch {
                                        when (
                                            val result = homeBatchMoveService.moveAppsToPage(
                                                sourcePageId = sourcePageId,
                                                appKeys = appKeys,
                                                targetPageId = targetPageId,
                                            )
                                        ) {
                                            is WorkspaceHomeBatchMoveResult.Applied -> {
                                                homeBatchUndoCommit.value = result.commit
                                                selectedHomePageId = result.commit.targetPageId
                                                val movedCount = result.commit.movedItemIds.size
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Moved $movedCount app${if (movedCount == 1) "" else "s"}.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            WorkspaceHomeBatchMoveResult.StoredWorkspaceChanged -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "No apps were moved because Home changed before the move finished.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "No apps were moved. The selection or destination could not be applied safely.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            },
                            onUndoLastMove = {
                                val pending = homeBatchUndoCommit.value
                                if (!launcherPreferences.layoutLocked && pending != null) {
                                    lifecycleScope.launch {
                                        when (val result = homeBatchMoveService.undo(pending)) {
                                            is WorkspaceHomeBatchMoveResult.Undone -> {
                                                homeBatchUndoCommit.value = null
                                                selectedHomePageId = result.commit.sourcePageId
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Last Home move undone.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            WorkspaceHomeBatchMoveResult.StoredWorkspaceChanged -> {
                                                homeBatchUndoCommit.value = null
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Undo is no longer available because Home changed after that move.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            else -> {
                                                homeBatchUndoCommit.value = null
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Undo is no longer available. Home could not be restored safely.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.isHomeReturnIntent()) {
            homeReturnRequestGeneration.value += 1L
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

    private fun openUniversalSearch(query: String? = null) {
        if (!indexIntegration.openSearch(query)) {
            Toast.makeText(
                this,
                "GoreeCloud Index is unavailable. Installed-app search remains local.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}

internal fun Intent.isHomeReturnIntent(): Boolean =
    action == Intent.ACTION_MAIN && hasCategory(Intent.CATEGORY_HOME)
