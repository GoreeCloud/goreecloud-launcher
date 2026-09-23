package com.goreecloud.launcher.ui

import android.content.ClipData
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.os.Process
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.goreecloud.launcher.core.launcher.LauncherUniversalSearchHomeMode
import com.goreecloud.launcher.core.launcher.LaunchApplicationSearchAction
import com.goreecloud.launcher.core.launcher.LauncherDockStyle
import com.goreecloud.launcher.core.launcher.LauncherDrawerBackdrop
import com.goreecloud.launcher.core.launcher.LauncherDrawerEntryMode
import com.goreecloud.launcher.core.launcher.LauncherDrawerLayoutMode
import com.goreecloud.launcher.core.launcher.LauncherDrawerNavigation
import com.goreecloud.launcher.core.launcher.LauncherDrawerProfileKind
import com.goreecloud.launcher.core.launcher.LauncherDrawerSearchPlacement
import com.goreecloud.launcher.core.launcher.LauncherDrawerSpacing
import com.goreecloud.launcher.core.launcher.LauncherExperiencePreferences
import com.goreecloud.launcher.core.launcher.LauncherHomeCardStyle
import com.goreecloud.launcher.core.launcher.LauncherHomeLabelPolicy
import com.goreecloud.launcher.core.launcher.LauncherHomeGlanceAlignment
import com.goreecloud.launcher.core.launcher.LauncherHomeSearchPlacement
import com.goreecloud.launcher.core.launcher.LauncherHomeSearchStyle
import com.goreecloud.launcher.core.launcher.LauncherHomeSpacing
import com.goreecloud.launcher.core.launcher.LauncherGestureAction
import com.goreecloud.launcher.core.launcher.LauncherGestureActionType
import com.goreecloud.launcher.core.launcher.LauncherHomeGesture
import com.goreecloud.launcher.core.launcher.LauncherBuiltInSearchProviderRegistry
import com.goreecloud.launcher.core.launcher.LauncherNavigateSearchAction
import com.goreecloud.launcher.core.launcher.LauncherSearchCategory
import com.goreecloud.launcher.core.launcher.LauncherSearchDestination
import com.goreecloud.launcher.core.launcher.LauncherSearchResult
import com.goreecloud.launcher.core.launcher.LauncherPreferences
import com.goreecloud.launcher.core.launcher.LauncherUniversalSearch
import com.goreecloud.launcher.core.launcher.LauncherWallpaperShade
import com.goreecloud.launcher.core.launcher.launcherDrawerProfilePages
import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.db.WorkspaceRenderedHomePage
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.theme.GlazeAtmosphere
import com.goreecloud.launcher.ui.theme.GlazeMetrics
import com.goreecloud.launcher.ui.theme.GlazeThemeMode
import com.goreecloud.launcher.ui.theme.GlazeV16MaterialRole
import com.goreecloud.launcher.ui.theme.GlazeV16PresentationPolicy
import com.goreecloud.launcher.ui.theme.LocalGlazeV16PresentationContext
import com.goreecloud.launcher.ui.theme.ThemeManagerSurface
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LauncherSurfaceMode { HOME, SEARCH, DRAWER, SETTINGS, THEME_MANAGER }

private enum class LauncherAppDragOrigin { HOME, DOCK, DRAWER }

private data class LauncherAppDragData(
    val appKey: String,
    val origin: LauncherAppDragOrigin,
)

private fun LauncherAppDragData.toTransferData(): DragAndDropTransferData =
    DragAndDropTransferData(
        clipData = ClipData.newPlainText("GoreeCloud Launcher app", appKey),
        localState = this,
    )

private fun DragAndDropEvent.launcherAppDragData(): LauncherAppDragData? =
    toAndroidDragEvent().localState as? LauncherAppDragData

private fun DragAndDropEvent.rootDropPoint(): Offset =
    toAndroidDragEvent().let { event -> Offset(event.x, event.y) }

private fun nearestHomeCell(
    point: Offset,
    bounds: Map<Pair<Int, Int>, Rect>,
): Pair<Int, Int>? =
    bounds.entries.minByOrNull { entry ->
        val dx = entry.value.center.x - point.x
        val dy = entry.value.center.y - point.y
        dx * dx + dy * dy
    }?.key

private fun dockInsertionTarget(
    dropX: Float,
    sourceKey: String,
    bounds: Map<String, Rect>,
): String? =
    bounds.entries
        .asSequence()
        .filter { (key, rect) ->
            key != sourceKey && rect.center.x.isFinite()
        }
        .sortedBy { it.value.center.x }
        .firstOrNull { it.value.center.x > dropX }
        ?.key

@Composable
fun LauncherBetaRoot(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    homePageCount: Int,
    homeResetSequence: Long,
    homeLabelOverrides: Map<String, String>,
    primaryHomePage: WorkspaceRenderedHomePage?,
    onManageHomePages: () -> Unit,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onOpenAppInfo: (LauncherActivityInfo) -> Unit,
    onToggleFavorite: (LauncherActivityInfo) -> Unit,
    onToggleDock: (LauncherActivityInfo) -> Unit,
    onMoveFavorite: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveFavoriteToCell: (LauncherActivityInfo, Int, Int) -> Unit,
    onMoveFavoriteToDock: (LauncherActivityInfo, String?) -> Unit,
    onMoveDockToHomeCell: (LauncherActivityInfo, Int, Int) -> Unit,
    onCopyDrawerToHomeCell: (LauncherActivityInfo, Int, Int) -> Unit,
    onCopyDrawerToDock: (LauncherActivityInfo, String?) -> Unit,
    onReorderDockByDrop: (LauncherActivityInfo, String?) -> Unit,
    onMoveDock: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onSetHomeLabelOverride: (LauncherActivityInfo, String?) -> Unit,
    onRequestUninstall: (LauncherActivityInfo) -> Unit,
    themeMode: GlazeThemeMode,
    onSetThemeMode: (GlazeThemeMode) -> Unit,
    onSetHomeGrid: (Int, Int) -> Unit,
    onSetDrawerColumns: (Int) -> Unit,
    onSetDrawerLayoutMode: (LauncherDrawerLayoutMode) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetIconScale: (Float) -> Unit,
    onSetLayoutLocked: (Boolean) -> Unit,
    onSetUniversalSearchHomeMode: (LauncherUniversalSearchHomeMode) -> Unit,
    onSetHomeCardStyle: (LauncherHomeCardStyle) -> Unit,
    onSetShowHomeQuickActions: (Boolean) -> Unit,
    onSetShowHomePageIndicator: (Boolean) -> Unit,
    onSetDrawerBackdrop: (LauncherDrawerBackdrop) -> Unit,
    onSetDrawerSearchPlacement: (LauncherDrawerSearchPlacement) -> Unit,
    onSetDrawerNavigation: (LauncherDrawerNavigation) -> Unit,
    onSetDrawerEntryMode: (LauncherDrawerEntryMode) -> Unit,
    onSetDrawerSpacing: (LauncherDrawerSpacing) -> Unit,
    onSetDrawerPageRows: (Int) -> Unit,
    onSetShowDrawerAppCount: (Boolean) -> Unit,
    onSetHomeGlanceAlignment: (LauncherHomeGlanceAlignment) -> Unit,
    onSetHomeSearchPlacement: (LauncherHomeSearchPlacement) -> Unit,
    onSetHomeSearchStyle: (LauncherHomeSearchStyle) -> Unit,
    onSetHomeSpacing: (LauncherHomeSpacing) -> Unit,
    onSetDockStyle: (LauncherDockStyle) -> Unit,
    onSetWallpaperShade: (LauncherWallpaperShade) -> Unit,
    onSetGestureAction: (LauncherHomeGesture, LauncherGestureAction) -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onSurfaceModeChanged: (LauncherSurfaceMode) -> Unit,
) {
    var surfaceModeName by rememberSaveable { mutableStateOf(LauncherSurfaceMode.HOME.name) }
    val surfaceMode = runCatching { LauncherSurfaceMode.valueOf(surfaceModeName) }
        .getOrDefault(LauncherSurfaceMode.HOME)
    var selectedApp by remember { mutableStateOf<LauncherActivityInfo?>(null) }
    var drawerSearchRequested by rememberSaveable { mutableStateOf(false) }
    var homeEditorRequestSequence by remember { mutableStateOf(0L) }
    val homeCellBounds = remember { mutableStateMapOf<Pair<Int, Int>, Rect>() }
    val dockItemBounds = remember { mutableStateMapOf<String, Rect>() }
    var dockBounds by remember { mutableStateOf<Rect?>(null) }
    var activeDrag by remember { mutableStateOf<LauncherAppDragData?>(null) }
    var dragPoint by remember { mutableStateOf<Offset?>(null) }
    var homeEditMode by rememberSaveable { mutableStateOf(false) }

    val currentApps by rememberUpdatedState(apps)
    val currentMoveFavoriteToCell by rememberUpdatedState(onMoveFavoriteToCell)
    val currentMoveFavoriteToDock by rememberUpdatedState(onMoveFavoriteToDock)
    val currentMoveDockToHomeCell by rememberUpdatedState(onMoveDockToHomeCell)
    val currentCopyDrawerToHomeCell by rememberUpdatedState(onCopyDrawerToHomeCell)
    val currentCopyDrawerToDock by rememberUpdatedState(onCopyDrawerToDock)
    val currentReorderDockByDrop by rememberUpdatedState(onReorderDockByDrop)

    val routeAppDrop: (LauncherAppDragData, Offset) -> Boolean = { drag, point ->
        val app = currentApps.firstOrNull { it.workspaceKey() == drag.appKey }
        if (app == null) {
            false
        } else {
            val dock = dockBounds
            if (dock != null && dock.contains(point)) {
                val targetDockKey = dockInsertionTarget(
                    dropX = point.x,
                    sourceKey = drag.appKey,
                    bounds = dockItemBounds,
                )
                when (drag.origin) {
                    LauncherAppDragOrigin.HOME ->
                        currentMoveFavoriteToDock(app, targetDockKey)
                    LauncherAppDragOrigin.DOCK ->
                        currentReorderDockByDrop(app, targetDockKey)
                    LauncherAppDragOrigin.DRAWER ->
                        currentCopyDrawerToDock(app, targetDockKey)
                }
                true
            } else {
                val targetCell = nearestHomeCell(point, homeCellBounds)
                if (targetCell == null) {
                    false
                } else {
                    when (drag.origin) {
                        LauncherAppDragOrigin.HOME ->
                            currentMoveFavoriteToCell(app, targetCell.first, targetCell.second)
                        LauncherAppDragOrigin.DOCK ->
                            currentMoveDockToHomeCell(app, targetCell.first, targetCell.second)
                        LauncherAppDragOrigin.DRAWER ->
                            currentCopyDrawerToHomeCell(app, targetCell.first, targetCell.second)
                    }
                    true
                }
            }
        }
    }

    val launcherDragTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                val drag = event.launcherAppDragData() ?: return
                if (drag.origin != LauncherAppDragOrigin.DRAWER) return
                selectedApp = null
                homeEditMode = true
                activeDrag = drag
                dragPoint = null
                drawerSearchRequested = false
                surfaceModeName = LauncherSurfaceMode.HOME.name
            }

            override fun onMoved(event: DragAndDropEvent) {
                if (activeDrag?.origin == LauncherAppDragOrigin.DRAWER) {
                    dragPoint = event.rootDropPoint()
                }
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val drag = activeDrag ?: event.launcherAppDragData() ?: return false
                if (drag.origin != LauncherAppDragOrigin.DRAWER) return false
                val point = event.rootDropPoint()
                dragPoint = point
                return routeAppDrop(drag, point)
            }

            override fun onEnded(event: DragAndDropEvent) {
                if (activeDrag?.origin == LauncherAppDragOrigin.DRAWER) {
                    activeDrag = null
                    dragPoint = null
                }
            }
        }
    }

    val beginLocalDrag: (LauncherAppDragData, Offset) -> Unit = { drag, point ->
        selectedApp = null
        homeEditMode = true
        activeDrag = drag
        dragPoint = point
    }
    val updateLocalDrag: (Offset) -> Unit = { point ->
        dragPoint = point
    }
    val endLocalDrag: (LauncherAppDragData, Offset) -> Unit = { drag, point ->
        dragPoint = point
        routeAppDrop(drag, point)
        activeDrag = null
        dragPoint = null
    }
    val cancelLocalDrag: () -> Unit = {
        activeDrag = null
        dragPoint = null
    }

    LaunchedEffect(homeResetSequence) {
        drawerSearchRequested = false
        selectedApp = null
        homeEditMode = false
        activeDrag = null
        dragPoint = null
        surfaceModeName = LauncherSurfaceMode.HOME.name
    }

    LaunchedEffect(surfaceMode) { onSurfaceModeChanged(surfaceMode) }

    val presentationContext = LocalGlazeV16PresentationContext.current
    val surfaceTransitionMotionMode = remember(presentationContext) {
        GlazeV16PresentationPolicy.resolve(
            requestedMaterial = GlazeV16MaterialRole.RAISED,
            context = presentationContext,
        ).motionMode
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event.launcherAppDragData()?.origin == LauncherAppDragOrigin.DRAWER
                },
                target = launcherDragTarget,
            ),
    ) {
        AnimatedContent(
            targetState = surfaceMode,
            transitionSpec = {
            val profile = LauncherSurfaceTransitionPolicy.resolve(
                initial = initialState,
                target = targetState,
                motionMode = surfaceTransitionMotionMode,
            )
            when {
                !profile.spatial ->
                    fadeIn(animationSpec = tween(durationMillis = profile.enterDurationMillis)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = profile.exitDurationMillis))

                initialState == LauncherSurfaceMode.HOME &&
                    targetState == LauncherSurfaceMode.DRAWER ->
                    (
                        slideInVertically(
                            animationSpec = tween(durationMillis = profile.enterDurationMillis),
                            initialOffsetY = { height -> height / profile.enterOffsetDivisor },
                        ) + fadeIn(animationSpec = tween(durationMillis = profile.enterDurationMillis))
                    ) togetherWith (
                        slideOutVertically(
                            animationSpec = tween(durationMillis = profile.exitDurationMillis),
                            targetOffsetY = { height -> -height / profile.exitOffsetDivisor },
                        ) + fadeOut(animationSpec = tween(durationMillis = profile.exitDurationMillis))
                    )

                initialState == LauncherSurfaceMode.DRAWER &&
                    targetState == LauncherSurfaceMode.HOME ->
                    (
                        slideInVertically(
                            animationSpec = tween(durationMillis = profile.enterDurationMillis),
                            initialOffsetY = { height -> -height / profile.enterOffsetDivisor },
                        ) + fadeIn(animationSpec = tween(durationMillis = profile.enterDurationMillis))
                    ) togetherWith (
                        slideOutVertically(
                            animationSpec = tween(durationMillis = profile.exitDurationMillis),
                            targetOffsetY = { height -> height / profile.exitOffsetDivisor },
                        ) + fadeOut(animationSpec = tween(durationMillis = profile.exitDurationMillis))
                    )

                else ->
                    fadeIn(animationSpec = tween(durationMillis = profile.enterDurationMillis)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = profile.exitDurationMillis))
            }
        },
    ) { targetSurfaceMode ->
        when (targetSurfaceMode) {
            LauncherSurfaceMode.HOME -> HomeSurface(
                apps = apps,
                workspace = workspace,
                preferences = preferences,
                experiencePreferences = experiencePreferences,
                homePageCount = homePageCount,
                homeEditorRequestSequence = homeEditorRequestSequence,
                homeLabelOverrides = homeLabelOverrides,
                primaryHomePage = primaryHomePage,
                editMode = homeEditMode,
                activeDrag = activeDrag,
                dragPoint = dragPoint,
                homeCellBounds = homeCellBounds,
                dockItemBounds = dockItemBounds,
                onDockBoundsChanged = { dockBounds = it },
                onBeginLocalDrag = beginLocalDrag,
                onUpdateLocalDrag = updateLocalDrag,
                onEndLocalDrag = endLocalDrag,
                onCancelLocalDrag = cancelLocalDrag,
                onExitEditMode = {
                    homeEditMode = false
                    selectedApp = null
                    activeDrag = null
                    dragPoint = null
                },
                onManageHomePages = onManageHomePages,
                onMoveFavoriteToCell = onMoveFavoriteToCell,
                onLaunchApp = onLaunchApp,
                onOpenLauncherSearch = {
                    drawerSearchRequested = false
                    surfaceModeName = LauncherSurfaceMode.SEARCH.name
                },
                onManageApp = {
                    homeEditMode = true
                    selectedApp = it
                },
                onOpenDrawer = {
                    drawerSearchRequested =
                        experiencePreferences.drawerEntryMode == LauncherDrawerEntryMode.SEARCH_FIRST
                    surfaceModeName = LauncherSurfaceMode.DRAWER.name
                },
                onOpenSettings = { surfaceModeName = LauncherSurfaceMode.SETTINGS.name },
                onOpenThemeManager = { surfaceModeName = LauncherSurfaceMode.THEME_MANAGER.name },
                onOpenWallpaperPicker = onOpenWallpaperPicker,
            )
            LauncherSurfaceMode.SEARCH -> LauncherUniversalSearchSurface(
                apps = apps,
                onLaunchApp = onLaunchApp,
                onNavigate = { destination ->
                    when (destination) {
                        LauncherSearchDestination.HOME -> {
                            drawerSearchRequested = false
                            surfaceModeName = LauncherSurfaceMode.HOME.name
                        }
                        LauncherSearchDestination.APPS -> {
                            drawerSearchRequested = false
                            surfaceModeName = LauncherSurfaceMode.DRAWER.name
                        }
                        LauncherSearchDestination.SETTINGS -> {
                            surfaceModeName = LauncherSurfaceMode.SETTINGS.name
                        }
                        LauncherSearchDestination.HOME_EDITOR -> {
                            homeEditorRequestSequence += 1L
                            surfaceModeName = LauncherSurfaceMode.HOME.name
                        }
                        LauncherSearchDestination.WALLPAPER -> {
                            surfaceModeName = LauncherSurfaceMode.HOME.name
                            onOpenWallpaperPicker()
                        }
                        LauncherSearchDestination.THEME_MANAGER -> {
                            surfaceModeName = LauncherSurfaceMode.THEME_MANAGER.name
                        }
                    }
                },
                onBack = {
                    surfaceModeName = LauncherSurfaceMode.HOME.name
                },
            )
            LauncherSurfaceMode.DRAWER -> AppDrawerSurface(
                apps = apps,
                preferences = preferences,
                drawerLayoutMode = drawerLayoutMode,
                experiencePreferences = experiencePreferences,
                focusSearch = drawerSearchRequested,
                onLaunchApp = onLaunchApp,
                onManageApp = { selectedApp = it },
                onHome = {
                    drawerSearchRequested = false
                    surfaceModeName = LauncherSurfaceMode.HOME.name
                },
                onOpenSettings = { surfaceModeName = LauncherSurfaceMode.SETTINGS.name },
            )
            LauncherSurfaceMode.SETTINGS -> LauncherSettingsSurface(
                selectedThemeMode = themeMode,
                onSelectThemeMode = onSetThemeMode,
                rootContent = { onOpenThemeManager ->
                    LauncherSettingsRootSurface(
                        apps = apps,
                        preferences = preferences,
                        drawerLayoutMode = drawerLayoutMode,
                        experiencePreferences = experiencePreferences,
                        themeMode = themeMode,
                        isDefaultHome = isDefaultHome,
                        onRequestHomeRole = onRequestHomeRole,
                        onSetHomeGrid = onSetHomeGrid,
                        onSetDrawerColumns = onSetDrawerColumns,
                        onSetDrawerLayoutMode = onSetDrawerLayoutMode,
                        onSetShowLabels = onSetShowLabels,
                        onSetIconScale = onSetIconScale,
                        onSetLayoutLocked = onSetLayoutLocked,
                        onSetUniversalSearchHomeMode = onSetUniversalSearchHomeMode,
                        onSetHomeCardStyle = onSetHomeCardStyle,
                        onSetShowHomeQuickActions = onSetShowHomeQuickActions,
                        onSetShowHomePageIndicator = onSetShowHomePageIndicator,
                        onSetDrawerBackdrop = onSetDrawerBackdrop,
                        onSetDrawerSearchPlacement = onSetDrawerSearchPlacement,
                        onSetDrawerNavigation = onSetDrawerNavigation,
                        onSetDrawerEntryMode = onSetDrawerEntryMode,
                        onSetDrawerSpacing = onSetDrawerSpacing,
                        onSetDrawerPageRows = onSetDrawerPageRows,
                        onSetShowDrawerAppCount = onSetShowDrawerAppCount,
                        onSetHomeGlanceAlignment = onSetHomeGlanceAlignment,
                        onSetHomeSearchPlacement = onSetHomeSearchPlacement,
                        onSetHomeSearchStyle = onSetHomeSearchStyle,
                        onSetHomeSpacing = onSetHomeSpacing,
                        onSetDockStyle = onSetDockStyle,
                        onSetWallpaperShade = onSetWallpaperShade,
                        onSetGestureAction = onSetGestureAction,
                        onOpenThemeManager = onOpenThemeManager,
                        onBack = { surfaceModeName = LauncherSurfaceMode.HOME.name },
                    )
                },
            )
            LauncherSurfaceMode.THEME_MANAGER -> ThemeManagerSurface(
                selectedMode = themeMode,
                onSelectMode = onSetThemeMode,
                onBack = { surfaceModeName = LauncherSurfaceMode.HOME.name },
            )
        }
    }

    }

    if (activeDrag == null) selectedApp?.let { app ->
        AppPlacementDialog(
            app = app,
            workspace = workspace,
            layoutLocked = preferences.layoutLocked,
            onToggleFavorite = { onToggleFavorite(app) },
            onToggleDock = { onToggleDock(app) },
            onMoveFavorite = { onMoveFavorite(app, it) },
            onMoveDock = { onMoveDock(app, it) },
            homeLabelOverride = homeLabelOverrides[app.workspaceKey()],
            onSetHomeLabelOverride = { onSetHomeLabelOverride(app, it) },
            onOpenAppInfo = { onOpenAppInfo(app) },
            onRequestUninstall = { onRequestUninstall(app) },
            onClose = { selectedApp = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSurface(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    experiencePreferences: LauncherExperiencePreferences,
    homePageCount: Int,
    homeEditorRequestSequence: Long,
    homeLabelOverrides: Map<String, String>,
    primaryHomePage: WorkspaceRenderedHomePage?,
    editMode: Boolean,
    activeDrag: LauncherAppDragData?,
    dragPoint: Offset?,
    homeCellBounds: MutableMap<Pair<Int, Int>, Rect>,
    dockItemBounds: MutableMap<String, Rect>,
    onDockBoundsChanged: (Rect) -> Unit,
    onBeginLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onUpdateLocalDrag: (Offset) -> Unit,
    onEndLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onCancelLocalDrag: () -> Unit,
    onExitEditMode: () -> Unit,
    onManageHomePages: () -> Unit,
    onMoveFavoriteToCell: (LauncherActivityInfo, Int, Int) -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onOpenLauncherSearch: () -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenThemeManager: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val favoriteApps = remember(appsByKey, workspace.favoriteKeys, preferences.homeCapacity) {
        workspace.favoriteKeys.mapNotNull(appsByKey::get).take(preferences.homeCapacity)
    }
    val dockApps = remember(appsByKey, workspace.dockKeys) {
        workspace.dockKeys.mapNotNull(appsByKey::get).take(MAX_DOCK_ITEMS)
    }

    LaunchedEffect(preferences.homeColumns, preferences.homeRows) {
        homeCellBounds.keys
            .filter { (cellX, cellY) ->
                cellX !in 0 until preferences.homeColumns ||
                    cellY !in 0 until preferences.homeRows
            }
            .forEach(homeCellBounds::remove)
    }
    LaunchedEffect(dockApps.map { it.workspaceKey() }) {
        val visibleDockKeys = dockApps.map { it.workspaceKey() }.toSet()
        dockItemBounds.keys
            .filterNot(visibleDockKeys::contains)
            .forEach(dockItemBounds::remove)
    }

    val swipeThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var showHomeEditor by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(homeEditorRequestSequence) {
        if (homeEditorRequestSequence > 0L) {
            showHomeEditor = true
        }
    }

    val executeGestureAction: (LauncherGestureAction) -> Unit = { action ->
        when (action.type) {
            LauncherGestureActionType.NONE -> Unit
            LauncherGestureActionType.APPS -> onOpenDrawer()
            LauncherGestureActionType.UNIVERSAL_SEARCH -> onOpenLauncherSearch()
            LauncherGestureActionType.LAUNCHER_SETTINGS -> onOpenSettings()
            LauncherGestureActionType.HOME_EDITOR -> showHomeEditor = true
            LauncherGestureActionType.WALLPAPER -> onOpenWallpaperPicker()
            LauncherGestureActionType.THEME_MANAGER -> onOpenThemeManager()
            LauncherGestureActionType.OPEN_APP -> {
                action.appKey
                    ?.let(appsByKey::get)
                    ?.let(onLaunchApp)
            }
        }
    }
    val currentGesturePreferences by rememberUpdatedState(experiencePreferences)
    val currentExecuteGestureAction by rememberUpdatedState(executeGestureAction)

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }

    val wallpaperShadeAlpha = when (experiencePreferences.wallpaperShade) {
        LauncherWallpaperShade.OFF -> 0f
        LauncherWallpaperShade.SOFT -> 0.18f
        LauncherWallpaperShade.STRONG -> 0.34f
    }
    val showPermanentSearch = preferences.universalSearchHomeMode == LauncherUniversalSearchHomeMode.PERMANENT
    val searchAtTop =
        experiencePreferences.homeSearchPlacement == LauncherHomeSearchPlacement.TOP
    val openSearch = onOpenLauncherSearch

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(
                "launcher-home-swipe-up-" +
                    experiencePreferences.swipeUpAction.storageValue,
            )
            .pointerInput(swipeThreshold) {
                var drag = 0f
                var triggered = false
                detectVerticalDragGestures(
                    onDragStart = {
                        drag = 0f
                        triggered = false
                    },
                    onDragCancel = {
                        drag = 0f
                        triggered = false
                    },
                    onDragEnd = {
                        drag = 0f
                        triggered = false
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        if (!triggered) {
                            drag += amount
                            when {
                                drag >= swipeThreshold -> {
                                    triggered = true
                                    currentExecuteGestureAction(
                                        currentGesturePreferences.swipeDownAction,
                                    )
                                }
                                drag <= -swipeThreshold -> {
                                    triggered = true
                                    currentExecuteGestureAction(
                                        currentGesturePreferences.swipeUpAction,
                                    )
                                }
                            }
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(swipeThreshold) {
                    detectTapGestures(
                        onDoubleTap = {
                            currentExecuteGestureAction(
                                currentGesturePreferences.doubleTapAction,
                            )
                        },
                        onLongPress = {
                            currentExecuteGestureAction(
                                currentGesturePreferences.tapAndHoldAction,
                            )
                        },
                    )
                }
                .pointerInput(swipeThreshold) {
                    var drag = 0f
                    var triggered = false
                    detectHorizontalDragGestures(
                        onDragStart = {
                            drag = 0f
                            triggered = false
                        },
                        onDragCancel = {
                            drag = 0f
                            triggered = false
                        },
                        onDragEnd = {
                            drag = 0f
                            triggered = false
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            if (!triggered) {
                                drag += amount
                                when {
                                    drag >= swipeThreshold -> {
                                        triggered = true
                                        currentExecuteGestureAction(
                                            currentGesturePreferences.swipeRightAction,
                                        )
                                    }
                                    drag <= -swipeThreshold -> {
                                        triggered = true
                                        currentExecuteGestureAction(
                                            currentGesturePreferences.swipeLeftAction,
                                        )
                                    }
                                }
                            }
                        },
                    )
                },
        )

        if (wallpaperShadeAlpha > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GlazeAtmosphere.canvasBlack.copy(alpha = wallpaperShadeAlpha * 0.35f),
                                Color.Transparent,
                                GlazeAtmosphere.canvasBlack.copy(alpha = wallpaperShadeAlpha),
                            ),
                        ),
                    ),
            )
        }

        val homeVerticalSpacing = when (experiencePreferences.homeSpacing) {
            LauncherHomeSpacing.COMPACT -> GlazeMetrics.space1
            LauncherHomeSpacing.BALANCED -> GlazeMetrics.space2
            LauncherHomeSpacing.AIRY -> GlazeMetrics.space3
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space2),
            verticalArrangement = Arrangement.spacedBy(homeVerticalSpacing),
        ) {
            if (editMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launcher-home-edit-mode"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            if (activeDrag != null) "Move app" else "Edit Home",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (activeDrag != null) {
                                "Drop on a Home cell or in the Dock"
                            } else {
                                "Drag apps, or long-press one for more options"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.76f),
                        )
                    }
                    FilledTonalButton(
                        onClick = onExitEditMode,
                        enabled = activeDrag == null,
                    ) {
                        Text("Done")
                    }
                }
            }

            if (experiencePreferences.homeCardStyle != LauncherHomeCardStyle.OFF) {
                HomeAtAGlance(
                    now = now,
                    compact = experiencePreferences.homeCardStyle == LauncherHomeCardStyle.COMPACT,
                    alignment = experiencePreferences.homeGlanceAlignment,
                )
            }

            if (showPermanentSearch && searchAtTop) {
                GlazeSearchCapsule(
                    value = "Search GoreeCloud",
                    style = experiencePreferences.homeSearchStyle,
                    onClick = openSearch,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (experiencePreferences.showHomeQuickActions) {
                HomeQuickActions(
                    onOpenApps = onOpenDrawer,
                    onOpenSearch = openSearch,
                    onOpenSettings = onOpenSettings,
                )
            }

            Spacer(Modifier.weight(1f))

            if (favoriteApps.isEmpty() && dockApps.isEmpty() && activeDrag == null) {
                EmptyWorkspaceCard(
                    onOpenApps = onOpenDrawer,
                )
            }
            if (favoriteApps.isNotEmpty() || activeDrag != null) {
                HomeFavoritesGrid(
                    apps = favoriteApps,
                    columns = preferences.homeColumns,
                    rows = preferences.homeRows,
                    primaryHomePage = primaryHomePage,
                    iconScale = preferences.iconScale,
                    showLabels = preferences.showLabels,
                    spacing = experiencePreferences.homeSpacing,
                    layoutLocked = preferences.layoutLocked,
                    homeLabelOverrides = homeLabelOverrides,
                    cellBounds = homeCellBounds,
                    editMode = editMode,
                    activeDrag = activeDrag,
                    dragPoint = dragPoint,
                    onBeginLocalDrag = onBeginLocalDrag,
                    onUpdateLocalDrag = onUpdateLocalDrag,
                    onEndLocalDrag = onEndLocalDrag,
                    onCancelLocalDrag = onCancelLocalDrag,
                    onLaunchApp = onLaunchApp,
                    onManageApp = onManageApp,
                    onSwipeUp = {
                        executeGestureAction(experiencePreferences.swipeUpAction)
                    },
                    onSwipeDown = {
                        executeGestureAction(experiencePreferences.swipeDownAction)
                    },
                )
            }

            if (showPermanentSearch && !searchAtTop) {
                GlazeSearchCapsule(
                    value = "Search GoreeCloud",
                    style = experiencePreferences.homeSearchStyle,
                    onClick = openSearch,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (dockApps.isNotEmpty() || activeDrag != null) {
                GlazeDock(
                    apps = dockApps,
                    iconScale = preferences.iconScale,
                    style = experiencePreferences.dockStyle,
                    layoutLocked = preferences.layoutLocked,
                    editMode = editMode,
                    activeDrag = activeDrag,
                    dragPoint = dragPoint,
                    onDockBoundsChanged = onDockBoundsChanged,
                    dockItemBounds = dockItemBounds,
                    onBeginLocalDrag = onBeginLocalDrag,
                    onUpdateLocalDrag = onUpdateLocalDrag,
                    onEndLocalDrag = onEndLocalDrag,
                    onCancelLocalDrag = onCancelLocalDrag,
                    onLaunchApp = onLaunchApp,
                    onManageApp = onManageApp,
                    onSwipeUp = {
                        executeGestureAction(experiencePreferences.swipeUpAction)
                    },
                    onSwipeDown = {
                        executeGestureAction(experiencePreferences.swipeDownAction)
                    },
                )
            }

            Spacer(Modifier.height(2.dp))
        }

        if (showHomeEditor) {
            ModalBottomSheet(
                onDismissRequest = { showHomeEditor = false },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 0.dp,
            ) {
                HomeEditorSheet(
                    now = now,
                    favoriteApps = favoriteApps,
                    dockApps = dockApps,
                    preferences = preferences,
                    experiencePreferences = experiencePreferences,
                    homePageCount = homePageCount,
                    onWallpaper = {
                        showHomeEditor = false
                        onOpenWallpaperPicker()
                    },
                    onPages = {
                        showHomeEditor = false
                        onManageHomePages()
                    },
                    onApps = {
                        showHomeEditor = false
                        onOpenDrawer()
                    },
                    onSettings = {
                        showHomeEditor = false
                        onOpenSettings()
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeEditorSheet(
    now: LocalDateTime,
    favoriteApps: List<LauncherActivityInfo>,
    dockApps: List<LauncherActivityInfo>,
    preferences: LauncherPreferences,
    experiencePreferences: LauncherExperiencePreferences,
    homePageCount: Int,
    onWallpaper: () -> Unit,
    onPages: () -> Unit,
    onApps: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space3),
        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Edit Home",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    homePageCount.toString() + if (homePageCount == 1) " Home page" else " Home pages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Long-press Home anytime",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HomeEditorPreview(
            now = now,
            favoriteApps = favoriteApps,
            dockApps = dockApps,
            preferences = preferences,
            experiencePreferences = experiencePreferences,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            HomeEditorAction("Wallpaper", "◫", onWallpaper, Modifier.weight(1f))
            HomeEditorAction("Pages", "▣", onPages, Modifier.weight(1f))
            HomeEditorAction("Apps", "▦", onApps, Modifier.weight(1f))
            HomeEditorAction("Settings", "⚙", onSettings, Modifier.weight(1f))
        }
        Spacer(Modifier.height(GlazeMetrics.space1))
    }
}

@Composable
private fun HomeEditorPreview(
    now: LocalDateTime,
    favoriteApps: List<LauncherActivityInfo>,
    dockApps: List<LauncherActivityInfo>,
    preferences: LauncherPreferences,
    experiencePreferences: LauncherExperiencePreferences,
) {
    val locale = Locale.getDefault()
    val time = remember(now.minute, locale) {
        now.format(DateTimeFormatter.ofPattern("h:mm", locale))
    }
    val date = remember(now.dayOfYear, locale) {
        now.format(DateTimeFormatter.ofPattern("EEE, MMM d", locale))
    }
    val searchAtTop =
        experiencePreferences.homeSearchPlacement == LauncherHomeSearchPlacement.TOP
    val showSearch = preferences.universalSearchHomeMode == LauncherUniversalSearchHomeMode.PERMANENT

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp),
        shape = RoundedCornerShape(34.dp),
        color = GlazeAtmosphere.canvasBlack.copy(alpha = 0.40f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(GlazeMetrics.space4),
            verticalArrangement = Arrangement.spacedBy(
                when (experiencePreferences.homeSpacing) {
                    LauncherHomeSpacing.COMPACT -> GlazeMetrics.space1
                    LauncherHomeSpacing.BALANCED -> GlazeMetrics.space2
                    LauncherHomeSpacing.AIRY -> GlazeMetrics.space3
                },
            ),
        ) {
            if (experiencePreferences.homeCardStyle != LauncherHomeCardStyle.OFF) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (
                        experiencePreferences.homeGlanceAlignment == LauncherHomeGlanceAlignment.CENTER
                    ) Alignment.CenterHorizontally else Alignment.Start,
                ) {
                    Text(
                        time,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                    )
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
            }

            if (showSearch && searchAtTop) {
                HomeEditorPreviewSearch(experiencePreferences.homeSearchStyle)
            }

            Spacer(Modifier.weight(1f))

            if (favoriteApps.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    favoriteApps.take(preferences.homeColumns.coerceAtMost(5)).forEach { app ->
                        HomeEditorPreviewIcon(app)
                    }
                }
            }

            if (showSearch && !searchAtTop) {
                HomeEditorPreviewSearch(experiencePreferences.homeSearchStyle)
            }

            if (dockApps.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = if (experiencePreferences.dockStyle == LauncherDockStyle.EDGE) {
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
                    } else {
                        RoundedCornerShape(GlazeMetrics.radiusExtraLarge)
                    },
                    color = when (experiencePreferences.dockStyle) {
                        LauncherDockStyle.CLEAR -> Color.Transparent
                        LauncherDockStyle.GLASS -> GlazeAtmosphere.canvasBlack.copy(alpha = 0.24f)
                        LauncherDockStyle.EDGE -> GlazeAtmosphere.canvasBlack.copy(alpha = 0.40f)
                    },
                    border = if (experiencePreferences.dockStyle == LauncherDockStyle.CLEAR) {
                        null
                    } else {
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dockApps.take(MAX_DOCK_ITEMS).forEach { app ->
                            HomeEditorPreviewIcon(app)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEditorPreviewSearch(style: LauncherHomeSearchStyle) {
    val fill = when (style) {
        LauncherHomeSearchStyle.GLASS -> Color.White.copy(alpha = 0.14f)
        LauncherHomeSearchStyle.CLEAR -> Color.White.copy(alpha = 0.05f)
        LauncherHomeSearchStyle.SOLID -> Color.White.copy(alpha = 0.90f)
    }
    val foreground = if (style == LauncherHomeSearchStyle.SOLID) {
        GlazeAtmosphere.canvasBlack.copy(alpha = 0.88f)
    } else {
        Color.White.copy(alpha = 0.84f)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = fill,
        border = BorderStroke(
            1.dp,
            if (style == LauncherHomeSearchStyle.CLEAR) {
                Color.White.copy(alpha = 0.16f)
            } else {
                Color.White.copy(alpha = 0.08f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GlazeMetrics.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            Text("⌕", color = foreground)
            Text(
                "Search phone",
                style = MaterialTheme.typography.labelSmall,
                color = foreground.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun HomeEditorPreviewIcon(app: LauncherActivityInfo) {
    val icon = rememberLauncherAppIcon(app)
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(34.dp),
        )
    } else {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.14f),
        ) {}
    }
}

@Composable
private fun HomeEditorAction(
    label: String,
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(glyph, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HomeAtAGlance(
    now: LocalDateTime,
    compact: Boolean,
    alignment: LauncherHomeGlanceAlignment,
) {
    val locale = Locale.getDefault()
    val time = remember(now.minute, locale) {
        now.format(DateTimeFormatter.ofPattern("h:mm", locale))
    }
    val date = remember(now.dayOfYear, locale) {
        now.format(DateTimeFormatter.ofPattern("EEE, MMM d", locale))
    }
    val horizontalAlignment = if (alignment == LauncherHomeGlanceAlignment.CENTER) {
        Alignment.CenterHorizontally
    } else {
        Alignment.Start
    }
    val textAlign = if (alignment == LauncherHomeGlanceAlignment.CENTER) {
        TextAlign.Center
    } else {
        TextAlign.Start
    }
    val glanceShadow = Shadow(
        color = Color.Black.copy(alpha = 0.48f),
        offset = Offset(0f, 2f),
        blurRadius = 7f,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (compact) GlazeMetrics.space2 else GlazeMetrics.space3,
                vertical = if (compact) GlazeMetrics.space1 else GlazeMetrics.space2,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            time,
            modifier = if (alignment == LauncherHomeGlanceAlignment.CENTER) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
            },
            style = (
                if (compact) MaterialTheme.typography.headlineMedium
                else MaterialTheme.typography.displayMedium
            ).copy(shadow = glanceShadow),
            color = Color.White,
            fontWeight = FontWeight.Light,
            textAlign = textAlign,
        )
        Text(
            date,
            modifier = if (alignment == LauncherHomeGlanceAlignment.CENTER) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
            },
            style = MaterialTheme.typography.bodyMedium.copy(shadow = glanceShadow),
            color = Color.White.copy(alpha = 0.92f),
            textAlign = textAlign,
        )
    }
}

@Composable
private fun HomeFavoritesGrid(
    apps: List<LauncherActivityInfo>,
    columns: Int,
    rows: Int,
    primaryHomePage: WorkspaceRenderedHomePage?,
    iconScale: Float,
    showLabels: Boolean,
    spacing: LauncherHomeSpacing,
    layoutLocked: Boolean,
    homeLabelOverrides: Map<String, String>,
    cellBounds: MutableMap<Pair<Int, Int>, Rect>,
    editMode: Boolean,
    activeDrag: LauncherAppDragData?,
    dragPoint: Offset?,
    onBeginLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onUpdateLocalDrag: (Offset) -> Unit,
    onEndLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onCancelLocalDrag: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    val gridSpacing = when (spacing) {
        LauncherHomeSpacing.COMPACT -> 2.dp
        LauncherHomeSpacing.BALANCED -> GlazeMetrics.space1
        LauncherHomeSpacing.AIRY -> GlazeMetrics.space2
    }
    val tileHeight = when (spacing) {
        LauncherHomeSpacing.COMPACT -> 72.dp
        LauncherHomeSpacing.BALANCED -> 78.dp
        LauncherHomeSpacing.AIRY -> 86.dp
    }
    val storedPlacements = remember(primaryHomePage) {
        primaryHomePage?.appPlacements?.associateBy { it.appKey }.orEmpty()
    }
    val useSpatialPlacement = remember(apps, storedPlacements, columns, rows) {
        apps.isNotEmpty() && apps.all { app ->
            val placement = storedPlacements[app.workspaceKey()]
            placement?.cellX != null &&
                placement.cellY != null &&
                placement.cellX in 0 until columns &&
                placement.cellY in 0 until rows
        }
    }
    val appByCell = remember(apps, storedPlacements, useSpatialPlacement, columns, rows) {
        buildMap<Pair<Int, Int>, LauncherActivityInfo> {
            if (useSpatialPlacement) {
                apps.forEach { app ->
                    val placement = storedPlacements[app.workspaceKey()] ?: return@forEach
                    val cellX = placement.cellX ?: return@forEach
                    val cellY = placement.cellY ?: return@forEach
                    put(cellX to cellY, app)
                }
            } else {
                apps.take(columns * rows).forEachIndexed { index, app ->
                    put((index % columns) to (index / columns), app)
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
    ) {
        repeat(rows) { cellY ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            ) {
                repeat(columns) { cellX ->
                    val coordinate = cellX to cellY
                    val app = appByCell[coordinate]
                    val cellHovered = activeDrag != null &&
                        dragPoint?.let { point -> cellBounds[coordinate]?.contains(point) } == true
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(tileHeight)
                            .testTag("launcher-home-cell-$cellX-$cellY")
                            .onGloballyPositioned {
                                cellBounds[coordinate] = it.boundsInRoot()
                            }
                            .background(
                                when {
                                    cellHovered -> Color.White.copy(alpha = 0.16f)
                                    editMode -> Color.White.copy(alpha = 0.045f)
                                    else -> Color.Transparent
                                },
                                RoundedCornerShape(GlazeMetrics.radiusLarge),
                            )
                            .then(
                                if (editMode) {
                                    Modifier.border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(GlazeMetrics.radiusLarge),
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (app != null) {
                            val appKey = app.workspaceKey()
                            HomeFavoriteTile(
                                app = app,
                                displayLabel = homeLabelOverrides[appKey] ?: app.label.toString(),
                                iconScale = iconScale,
                                showLabel = showLabels,
                                layoutLocked = layoutLocked,
                                editMode = editMode,
                                dragData = if (layoutLocked) null else {
                                    LauncherAppDragData(
                                        appKey = appKey,
                                        origin = LauncherAppDragOrigin.HOME,
                                    )
                                },
                                onBeginLocalDrag = onBeginLocalDrag,
                                onUpdateLocalDrag = onUpdateLocalDrag,
                                onEndLocalDrag = onEndLocalDrag,
                                onCancelLocalDrag = onCancelLocalDrag,
                                onLaunchApp = onLaunchApp,
                                onManageApp = onManageApp,
                                onSwipeUp = onSwipeUp,
                                onSwipeDown = onSwipeDown,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFavoriteTile(
    app: LauncherActivityInfo,
    displayLabel: String,
    iconScale: Float,
    showLabel: Boolean,
    layoutLocked: Boolean,
    editMode: Boolean,
    dragData: LauncherAppDragData?,
    onBeginLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onUpdateLocalDrag: (Offset) -> Unit,
    onEndLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onCancelLocalDrag: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = rememberLauncherAppIcon(app)
    val iconSize = (50f * iconScale.coerceIn(0.85f, 1.15f)).dp
    val moveThreshold = with(LocalDensity.current) { 14.dp.toPx() }
    val swipeThreshold = with(LocalDensity.current) { 42.dp.toPx() }
    var dragging by remember(app.componentName, app.user) { mutableStateOf(false) }
    var dragOffset by remember(app.componentName, app.user) { mutableStateOf(Offset.Zero) }
    var tileBounds by remember(app.componentName, app.user) { mutableStateOf<Rect?>(null) }
    var dragStartCenter by remember(app.componentName, app.user) { mutableStateOf<Offset?>(null) }

    val gestureModifier = if (layoutLocked || dragData == null) {
        Modifier.combinedClickable(
            onClick = { onLaunchApp(app) },
            onLongClick = { onManageApp(app) },
        )
    } else {
        Modifier
            .pointerInput(app.componentName, app.user, moveThreshold) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragging = true
                        dragOffset = Offset.Zero
                        val start = tileBounds?.center
                        dragStartCenter = start
                        if (start != null) onBeginLocalDrag(dragData, start)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragOffset += amount
                        val start = dragStartCenter
                        if (start != null) onUpdateLocalDrag(start + dragOffset)
                    },
                    onDragEnd = {
                        val moved =
                            kotlin.math.abs(dragOffset.x) >= moveThreshold ||
                                kotlin.math.abs(dragOffset.y) >= moveThreshold
                        val start = dragStartCenter ?: tileBounds?.center
                        if (moved && start != null) {
                            onEndLocalDrag(dragData, start + dragOffset)
                        } else {
                            onCancelLocalDrag()
                            onManageApp(app)
                        }
                        dragging = false
                        dragOffset = Offset.Zero
                        dragStartCenter = null
                    },
                    onDragCancel = {
                        onCancelLocalDrag()
                        dragging = false
                        dragOffset = Offset.Zero
                        dragStartCenter = null
                    },
                )
            }
            .pointerInput(onSwipeUp, onSwipeDown, swipeThreshold) {
                var drag = 0f
                var triggered = false
                detectVerticalDragGestures(
                    onDragStart = {
                        drag = 0f
                        triggered = false
                    },
                    onDragCancel = {
                        drag = 0f
                        triggered = false
                    },
                    onDragEnd = {
                        drag = 0f
                        triggered = false
                    },
                    onVerticalDrag = { change, amount ->
                        if (!dragging) {
                            change.consume()
                            if (!triggered) {
                                drag += amount
                                when {
                                    drag <= -swipeThreshold -> {
                                        triggered = true
                                        onSwipeUp()
                                    }
                                    drag >= swipeThreshold -> {
                                        triggered = true
                                        onSwipeDown()
                                    }
                                }
                            }
                        }
                    },
                )
            }
            .clickable(enabled = !dragging) { onLaunchApp(app) }
    }

    Column(
        modifier = modifier
            .onGloballyPositioned { tileBounds = it.boundsInRoot() }
            .graphicsLayer {
                scaleX = if (editMode && !dragging) 0.96f else 1f
                scaleY = if (editMode && !dragging) 0.96f else 1f
                alpha = if (dragging) 0.88f else 1f
                translationX = dragOffset.x
                translationY = dragOffset.y
            }
            .then(gestureModifier)
            .background(
                if (editMode && !dragging) Color.White.copy(alpha = 0.06f)
                else Color.Transparent,
                RoundedCornerShape(GlazeMetrics.radiusLarge),
            )
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = displayLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(iconSize),
            )
        } else {
            Surface(
                modifier = Modifier.size(iconSize),
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(displayLabel.take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showLabel) {
            Spacer(Modifier.height(3.dp))
            Text(
                displayLabel,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.60f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 5f,
                    ),
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HomeQuickActions(
    onOpenApps: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
    ) {
        GlazeActionChip("Apps", onOpenApps, Modifier.weight(1f))
        GlazeActionChip("Search", onOpenSearch, Modifier.weight(1f))
        GlazeActionChip("Customize", onOpenSettings, Modifier.weight(1f))
    }
}

@Composable
private fun GlazeActionChip(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = GlazeAtmosphere.canvasBlack.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = GlazeMetrics.space2, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EmptyWorkspaceCard(
    onOpenApps: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        color = GlazeAtmosphere.canvasBlack.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GlazeMetrics.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Add apps to Home",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Long-press an app to place it on Home or in the dock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.74f),
                )
            }
            GlazeTextAction("Apps", onOpenApps)
        }
    }
}

@Composable
private fun LauncherUniversalSearchSurface(
    apps: List<LauncherActivityInfo>,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onNavigate: (LauncherSearchDestination) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val providers = remember(apps) {
        LauncherBuiltInSearchProviderRegistry.providers(apps)
    }
    val executionPolicy = remember {
        com.goreecloud.launcher.core.launcher.LauncherSearchExecutionPolicy.cancellationOnly()
    }
    var results by remember(providers, query) {
        mutableStateOf<List<LauncherSearchResult>>(emptyList())
    }
    var searchCompleted by remember(providers, query) { mutableStateOf(false) }

    LaunchedEffect(providers, query) {
        results = LauncherUniversalSearch.searchAsync(
            rawQuery = query,
            providers = providers,
            policy = executionPolicy,
        )
        searchCompleted = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        GlazeAtmosphere.softAqua.copy(alpha = 0.07f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space3),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Universal Search",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Launcher-owned local search and actions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GlazeTextAction("Done", onBack)
            }

            GlazeAppSearchField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                requestFocus = true,
                placeholder = "Search apps, settings and actions",
                inputTestTag = "launcher-universal-search-field",
            )

            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (searchCompleted) {
                            "No Launcher results match “" + query.trim() + "”"
                        } else {
                            "Searching…"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                    contentPadding = PaddingValues(bottom = GlazeMetrics.space3),
                ) {
                    lazyItems(
                        items = results,
                        key = { result -> result.providerId + ":" + result.resultId },
                    ) { result ->
                        LauncherUniversalSearchResultRow(
                            result = result,
                            onClick = {
                                when (val action = result.action) {
                                    is LaunchApplicationSearchAction -> onLaunchApp(action.app)
                                    is LauncherNavigateSearchAction -> onNavigate(action.destination)
                                    null -> Unit
                                    else -> Unit
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherUniversalSearchResultRow(
    result: LauncherSearchResult,
    onClick: () -> Unit,
) {
    val categoryLabel = when (result.category) {
        LauncherSearchCategory.APPLICATION -> "App"
        LauncherSearchCategory.SETTING -> "Setting"
        LauncherSearchCategory.ACTION -> "Action"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.64f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GlazeMetrics.space3, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        when (result.category) {
                            LauncherSearchCategory.APPLICATION -> "◫"
                            LauncherSearchCategory.SETTING -> "⚙"
                            LauncherSearchCategory.ACTION -> "→"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    result.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                result.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun AppDrawerSurface(
    apps: List<LauncherActivityInfo>,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    focusSearch: Boolean,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onHome: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val primaryUser = remember { Process.myUserHandle() }
    val profilePages = remember(apps, primaryUser) {
        launcherDrawerProfilePages(
            items = apps,
            primaryUser = primaryUser,
            userOf = { app -> app.user },
        )
    }
    var selectedProfileName by rememberSaveable {
        mutableStateOf(LauncherDrawerProfileKind.USER.name)
    }
    val selectedProfileKind = runCatching {
        LauncherDrawerProfileKind.valueOf(selectedProfileName)
    }.getOrDefault(LauncherDrawerProfileKind.USER)
    val selectedPage = profilePages.firstOrNull { page -> page.kind == selectedProfileKind }
        ?: profilePages.first()

    LaunchedEffect(profilePages.map { page -> page.kind }, selectedProfileKind) {
        if (profilePages.none { page -> page.kind == selectedProfileKind }) {
            selectedProfileName = LauncherDrawerProfileKind.USER.name
        }
    }

    val dismissThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    val glass = experiencePreferences.drawerBackdrop == LauncherDrawerBackdrop.GLASS
    val drawerSurfaceColor = if (glass) {
        GlazeAtmosphere.canvasBlack.copy(alpha = 0.76f)
    } else {
        MaterialTheme.colorScheme.background
    }
    val drawerSecondaryColor = if (glass) {
        Color.White.copy(alpha = 0.68f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val layoutDescription = when (drawerLayoutMode) {
        LauncherDrawerLayoutMode.GRID -> "Grid · ${preferences.drawerColumns} columns"
        LauncherDrawerLayoutMode.COMPACT -> "Compact · ${preferences.drawerColumns} columns"
        LauncherDrawerLayoutMode.LIST -> "Alphabetical list"
        LauncherDrawerLayoutMode.CATEGORY -> "Grouped by app category"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GlazeAtmosphere.canvasBlack.copy(alpha = if (glass) 0.08f else 0.02f),
                        GlazeAtmosphere.canvasBlack.copy(alpha = if (glass) 0.42f else 0.18f),
                    ),
                ),
            ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = GlazeMetrics.space2),
            shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
            color = drawerSurfaceColor,
            contentColor = if (glass) Color.White else MaterialTheme.colorScheme.onBackground,
            border = BorderStroke(
                1.dp,
                if (glass) Color.White.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space2)
                    .pointerInput(onHome, dismissThreshold) {
                        var drag = 0f
                        var triggered = false
                        detectVerticalDragGestures(
                            onDragStart = {
                                drag = 0f
                                triggered = false
                            },
                            onDragCancel = {
                                drag = 0f
                                triggered = false
                            },
                            onDragEnd = {
                                drag = 0f
                                triggered = false
                            },
                            onVerticalDrag = { _, amount ->
                                if (!triggered) {
                                    drag += amount
                                    if (drag >= dismissThreshold) {
                                        triggered = true
                                        onHome()
                                    }
                                }
                            },
                        )
                    },
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.width(36.dp).height(4.dp),
                        shape = CircleShape,
                        color = if (glass) Color.White.copy(alpha = 0.28f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                    ) {}
                }
                Spacer(Modifier.height(GlazeMetrics.space3))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            selectedPage.kind.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (experiencePreferences.showDrawerAppCount) {
                                selectedPage.items.size.toString() + " installed · " + layoutDescription
                            } else {
                                layoutDescription
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = drawerSecondaryColor,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2)) {
                        GlazeRoundAction("⚙", onOpenSettings)
                        GlazeRoundAction("⌄", onHome)
                    }
                }

                if (profilePages.size > 1) {
                    Spacer(Modifier.height(GlazeMetrics.space3))
                    DrawerProfileTabs(
                        pages = profilePages.map { page -> page.kind to page.items.size },
                        selected = selectedPage.kind,
                        onSelect = { kind -> selectedProfileName = kind.name },
                        secondaryColor = drawerSecondaryColor,
                    )
                }

                Spacer(Modifier.height(GlazeMetrics.space2))
                if (selectedPage.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No apps are available in " + selectedPage.kind.displayName + ".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = drawerSecondaryColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    DrawerAppsContent(
                        apps = selectedPage.items,
                        query = "",
                        preferences = preferences,
                        drawerLayoutMode = drawerLayoutMode,
                        experiencePreferences = experiencePreferences,
                        onLaunchApp = onLaunchApp,
                        onManageApp = onManageApp,
                        onDismiss = onHome,
                        secondaryColor = drawerSecondaryColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerProfileTabs(
    pages: List<Pair<LauncherDrawerProfileKind, Int>>,
    selected: LauncherDrawerProfileKind,
    onSelect: (LauncherDrawerProfileKind) -> Unit,
    secondaryColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("launcher-drawer-profile-tabs"),
        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
    ) {
        pages.forEach { (kind, count) ->
            val isSelected = kind == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .testTag(
                        if (kind == LauncherDrawerProfileKind.USER) {
                            "launcher-drawer-profile-user"
                        } else {
                            "launcher-drawer-profile-work"
                        },
                    ),
                onClick = { onSelect(kind) },
                shape = RoundedCornerShape(GlazeMetrics.radiusPill),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    Color.Transparent
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.46f)
                    } else {
                        secondaryColor.copy(alpha = 0.20f)
                    },
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        kind.displayName + " · " + count,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            secondaryColor
                        },
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerAppsContent(
    apps: List<LauncherActivityInfo>,
    query: String,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onDismiss: () -> Unit,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty() && query.isNotBlank()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No installed apps match “" + query.trim() + "”",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val pagedGrid = experiencePreferences.drawerNavigation == LauncherDrawerNavigation.PAGES &&
        drawerLayoutMode != LauncherDrawerLayoutMode.LIST &&
        drawerLayoutMode != LauncherDrawerLayoutMode.CATEGORY
    val standardSpacing = when (experiencePreferences.drawerSpacing) {
        LauncherDrawerSpacing.TIGHT -> GlazeMetrics.space1
        LauncherDrawerSpacing.STANDARD -> GlazeMetrics.space2
        LauncherDrawerSpacing.RELAXED -> GlazeMetrics.space3
    }
    val compactSpacing = when (experiencePreferences.drawerSpacing) {
        LauncherDrawerSpacing.TIGHT -> 2.dp
        LauncherDrawerSpacing.STANDARD -> GlazeMetrics.space1
        LauncherDrawerSpacing.RELAXED -> GlazeMetrics.space2
    }
    val gridTileHeight = when (experiencePreferences.drawerSpacing) {
        LauncherDrawerSpacing.TIGHT -> 80.dp
        LauncherDrawerSpacing.STANDARD -> 88.dp
        LauncherDrawerSpacing.RELAXED -> 96.dp
    }
    val compactTileHeight = when (experiencePreferences.drawerSpacing) {
        LauncherDrawerSpacing.TIGHT -> 66.dp
        LauncherDrawerSpacing.STANDARD -> 72.dp
        LauncherDrawerSpacing.RELAXED -> 80.dp
    }

    if (pagedGrid) {
        val pageSize = (
            preferences.drawerColumns * experiencePreferences.drawerPageRows.coerceIn(4, 6)
        ).coerceAtLeast(1)
        val pageCount = ((apps.size + pageSize - 1) / pageSize).coerceAtLeast(1)
        val pagerState = rememberPagerState(pageCount = { pageCount })
        val pagerScope = rememberCoroutineScope()

        LaunchedEffect(query, pageCount) {
            if (pagerState.currentPage >= pageCount || query.isNotBlank()) {
                pagerState.scrollToPage(0)
            }
        }

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = GlazeMetrics.space3,
            ) { page ->
                val pageApps = apps.drop(page * pageSize).take(pageSize)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(preferences.drawerColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = GlazeMetrics.space2),
                    horizontalArrangement = Arrangement.spacedBy(
                        if (drawerLayoutMode == LauncherDrawerLayoutMode.COMPACT) {
                            compactSpacing
                        } else {
                            standardSpacing
                        },
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        if (drawerLayoutMode == LauncherDrawerLayoutMode.COMPACT) {
                            compactSpacing
                        } else {
                            standardSpacing
                        },
                    ),
                    userScrollEnabled = false,
                ) {
                    items(pageApps, key = { it.workspaceKey() }) { app ->
                        LauncherAppTile(
                            app = app,
                            iconScale = preferences.iconScale,
                            showLabel = preferences.showLabels,
                            compact = drawerLayoutMode == LauncherDrawerLayoutMode.COMPACT,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onManageApp(app) },
                            dragData = if (preferences.layoutLocked) null else {
                                LauncherAppDragData(
                                    appKey = app.workspaceKey(),
                                    origin = LauncherAppDragOrigin.DRAWER,
                                )
                            },
                            modifier = Modifier.height(
                                if (drawerLayoutMode == LauncherDrawerLayoutMode.COMPACT) compactTileHeight
                                else gridTileHeight,
                            ),
                        )
                    }
                }
            }

            if (pageCount > 1) {
                DrawerPageDots(
                    pageCount = pageCount,
                    currentPage = pagerState.currentPage,
                    onSelectPage = { target ->
                        if (target != pagerState.currentPage) {
                            pagerScope.launch {
                                pagerState.animateScrollToPage(target)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
        return
    }

    when (drawerLayoutMode) {
        LauncherDrawerLayoutMode.GRID -> {
            val gridState = rememberLazyGridState()
            val dismissConnection = rememberDrawerDismissNestedScrollConnection(
                canScrollBackward = { gridState.canScrollBackward },
                onDismiss = onDismiss,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(preferences.drawerColumns),
                state = gridState,
                modifier = modifier
                    .fillMaxWidth()
                    .nestedScroll(dismissConnection),
                contentPadding = PaddingValues(vertical = standardSpacing),
                horizontalArrangement = Arrangement.spacedBy(standardSpacing),
                verticalArrangement = Arrangement.spacedBy(standardSpacing),
            ) {
                items(apps, key = { it.workspaceKey() }) { app ->
                    LauncherAppTile(
                        app = app,
                        iconScale = preferences.iconScale,
                        showLabel = preferences.showLabels,
                        compact = false,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onManageApp(app) },
                        dragData = if (preferences.layoutLocked) null else {
                            LauncherAppDragData(
                                appKey = app.workspaceKey(),
                                origin = LauncherAppDragOrigin.DRAWER,
                            )
                        },
                        modifier = Modifier.height(gridTileHeight),
                    )
                }
            }
        }
        LauncherDrawerLayoutMode.COMPACT -> {
            val gridState = rememberLazyGridState()
            val dismissConnection = rememberDrawerDismissNestedScrollConnection(
                canScrollBackward = { gridState.canScrollBackward },
                onDismiss = onDismiss,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(preferences.drawerColumns),
                state = gridState,
                modifier = modifier
                    .fillMaxWidth()
                    .nestedScroll(dismissConnection),
                contentPadding = PaddingValues(vertical = compactSpacing),
                horizontalArrangement = Arrangement.spacedBy(compactSpacing),
                verticalArrangement = Arrangement.spacedBy(compactSpacing),
            ) {
                items(apps, key = { it.workspaceKey() }) { app ->
                    LauncherAppTile(
                        app = app,
                        iconScale = preferences.iconScale,
                        showLabel = preferences.showLabels,
                        compact = true,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onManageApp(app) },
                        dragData = if (preferences.layoutLocked) null else {
                            LauncherAppDragData(
                                appKey = app.workspaceKey(),
                                origin = LauncherAppDragOrigin.DRAWER,
                            )
                        },
                        modifier = Modifier.height(compactTileHeight),
                    )
                }
            }
        }
        LauncherDrawerLayoutMode.LIST -> {
            val listState = rememberLazyListState()
            val dismissConnection = rememberDrawerDismissNestedScrollConnection(
                canScrollBackward = { listState.canScrollBackward },
                onDismiss = onDismiss,
            )
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxWidth()
                    .nestedScroll(dismissConnection),
                contentPadding = PaddingValues(vertical = standardSpacing),
                verticalArrangement = Arrangement.spacedBy(compactSpacing),
            ) {
                lazyItems(apps, key = { it.workspaceKey() }) { app ->
                    LauncherAppListRow(
                        app = app,
                        iconScale = preferences.iconScale,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onManageApp(app) },
                        dragData = if (preferences.layoutLocked) null else {
                            LauncherAppDragData(
                                appKey = app.workspaceKey(),
                                origin = LauncherAppDragOrigin.DRAWER,
                            )
                        },
                    )
                }
            }
        }
        LauncherDrawerLayoutMode.CATEGORY -> {
            val categoryGroups = remember(apps) {
                apps.groupBy(::drawerCategoryLabel)
                    .toList()
                    .sortedWith(
                        compareBy<Pair<String, List<LauncherActivityInfo>>>(
                            { drawerCategoryRank(it.first) },
                            { it.first },
                        ),
                    )
            }
            val listState = rememberLazyListState()
            val dismissConnection = rememberDrawerDismissNestedScrollConnection(
                canScrollBackward = { listState.canScrollBackward },
                onDismiss = onDismiss,
            )
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxWidth()
                    .nestedScroll(dismissConnection),
                contentPadding = PaddingValues(vertical = standardSpacing),
                verticalArrangement = Arrangement.spacedBy(compactSpacing),
            ) {
                categoryGroups.forEach { (category, categoryApps) ->
                    item(key = "category:$category") {
                        DrawerCategoryHeader(
                            label = category,
                            count = categoryApps.size,
                            secondaryColor = secondaryColor,
                        )
                    }
                    val rows = categoryApps.chunked(preferences.drawerColumns.coerceAtLeast(1))
                    lazyItems(
                        rows,
                        key = { row ->
                            "category:$category:" + row.first().workspaceKey()
                        },
                    ) { rowApps ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(standardSpacing),
                        ) {
                            rowApps.forEach { app ->
                                LauncherAppTile(
                                    app = app,
                                    iconScale = preferences.iconScale,
                                    showLabel = preferences.showLabels,
                                    compact = false,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onManageApp(app) },
                                    dragData = if (preferences.layoutLocked) null else {
                                        LauncherAppDragData(
                                            appKey = app.workspaceKey(),
                                            origin = LauncherAppDragOrigin.DRAWER,
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(gridTileHeight),
                                )
                            }
                            repeat(preferences.drawerColumns - rowApps.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDrawerDismissNestedScrollConnection(
    canScrollBackward: () -> Boolean,
    onDismiss: () -> Unit,
): NestedScrollConnection {
    val dismissThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    val currentCanScrollBackward by rememberUpdatedState(canScrollBackward)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    return remember(dismissThreshold) {
        object : NestedScrollConnection {
            private var downwardDrag = 0f
            private var triggered = false

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                if (available.y <= 0f || currentCanScrollBackward()) {
                    downwardDrag = 0f
                    triggered = false
                    return Offset.Zero
                }

                if (!triggered) {
                    downwardDrag += available.y
                    if (downwardDrag >= dismissThreshold) {
                        triggered = true
                        currentOnDismiss()
                    }
                }

                return if (triggered) Offset(0f, available.y) else Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                downwardDrag = 0f
                triggered = false
                return Velocity.Zero
            }
        }
    }
}

private fun drawerCategoryLabel(app: LauncherActivityInfo): String =
    when (app.applicationInfo.category) {
        ApplicationInfo.CATEGORY_GAME -> "Games"
        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_IMAGE,
        -> "Media"
        ApplicationInfo.CATEGORY_SOCIAL -> "Social"
        ApplicationInfo.CATEGORY_NEWS -> "News"
        ApplicationInfo.CATEGORY_MAPS -> "Travel & maps"
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
        else -> "Other"
    }

private fun drawerCategoryRank(label: String): Int =
    when (label) {
        "Productivity" -> 0
        "Social" -> 1
        "Media" -> 2
        "Games" -> 3
        "Travel & maps" -> 4
        "News" -> 5
        else -> 6
    }

@Composable
private fun DrawerCategoryHeader(
    label: String,
    count: Int,
    secondaryColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = GlazeMetrics.space2, bottom = GlazeMetrics.space1),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Surface(
            shape = RoundedCornerShape(GlazeMetrics.radiusPill),
            color = secondaryColor.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.18f)),
        ) {
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = secondaryColor,
            )
        }
    }
}

@Composable
private fun DrawerPageDots(
    pageCount: Int,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            Surface(
                onClick = { onSelectPage(page) },
                modifier = Modifier.size(if (page == currentPage) 8.dp else 6.dp),
                shape = CircleShape,
                color = if (page == currentPage) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                },
            ) {}
        }
    }
}


@Composable
@Suppress("UNUSED_PARAMETER")
private fun LauncherSettingsRootSurface(
    apps: List<LauncherActivityInfo>,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    themeMode: GlazeThemeMode,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onSetHomeGrid: (Int, Int) -> Unit,
    onSetDrawerColumns: (Int) -> Unit,
    onSetDrawerLayoutMode: (LauncherDrawerLayoutMode) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetIconScale: (Float) -> Unit,
    onSetLayoutLocked: (Boolean) -> Unit,
    onSetUniversalSearchHomeMode: (LauncherUniversalSearchHomeMode) -> Unit,
    onSetHomeCardStyle: (LauncherHomeCardStyle) -> Unit,
    onSetShowHomeQuickActions: (Boolean) -> Unit,
    onSetShowHomePageIndicator: (Boolean) -> Unit,
    onSetDrawerBackdrop: (LauncherDrawerBackdrop) -> Unit,
    onSetDrawerSearchPlacement: (LauncherDrawerSearchPlacement) -> Unit,
    onSetDrawerNavigation: (LauncherDrawerNavigation) -> Unit,
    onSetDrawerEntryMode: (LauncherDrawerEntryMode) -> Unit,
    onSetDrawerSpacing: (LauncherDrawerSpacing) -> Unit,
    onSetDrawerPageRows: (Int) -> Unit,
    onSetShowDrawerAppCount: (Boolean) -> Unit,
    onSetHomeGlanceAlignment: (LauncherHomeGlanceAlignment) -> Unit,
    onSetHomeSearchPlacement: (LauncherHomeSearchPlacement) -> Unit,
    onSetHomeSearchStyle: (LauncherHomeSearchStyle) -> Unit,
    onSetHomeSpacing: (LauncherHomeSpacing) -> Unit,
    onSetDockStyle: (LauncherDockStyle) -> Unit,
    onSetWallpaperShade: (LauncherWallpaperShade) -> Unit,
    onSetGestureAction: (LauncherHomeGesture, LauncherGestureAction) -> Unit,
    onOpenThemeManager: () -> Unit,
    onBack: () -> Unit,
) {
    var gestureToConfigure by remember { mutableStateOf<LauncherHomeGesture?>(null) }
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        GlazeAtmosphere.softAqua.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space3),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Launcher settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Home, apps, dock, search and Glaze",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GlazeTextAction("Done", onBack)
            }

            SettingsSection("Home screen", "Layout and glance content") {
                Text(
                    "Clock & date",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Clock", "Compact", "Off"),
                    selected = when (experiencePreferences.homeCardStyle) {
                        LauncherHomeCardStyle.CLOCK -> "Clock"
                        LauncherHomeCardStyle.COMPACT -> "Compact"
                        LauncherHomeCardStyle.OFF -> "Off"
                    },
                    onChoice = {
                        onSetHomeCardStyle(
                            when (it) {
                                "Compact" -> LauncherHomeCardStyle.COMPACT
                                "Off" -> LauncherHomeCardStyle.OFF
                                else -> LauncherHomeCardStyle.CLOCK
                            },
                        )
                    },
                )
                Text(
                    "Home grid",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("4×5", "5×6", "6×7"),
                    selected = preferences.homeColumns.toString() + "×" + preferences.homeRows.toString(),
                    onChoice = {
                        val parts = it.split("×")
                        onSetHomeGrid(parts[0].toInt(), parts[1].toInt())
                    },
                )
                Text(
                    "Home spacing",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Compact", "Balanced", "Airy"),
                    selected = when (experiencePreferences.homeSpacing) {
                        LauncherHomeSpacing.COMPACT -> "Compact"
                        LauncherHomeSpacing.BALANCED -> "Balanced"
                        LauncherHomeSpacing.AIRY -> "Airy"
                    },
                    onChoice = {
                        onSetHomeSpacing(
                            when (it) {
                                "Compact" -> LauncherHomeSpacing.COMPACT
                                "Airy" -> LauncherHomeSpacing.AIRY
                                else -> LauncherHomeSpacing.BALANCED
                            },
                        )
                    },
                )
                Text(
                    "Clock alignment",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Left", "Center"),
                    selected = if (
                        experiencePreferences.homeGlanceAlignment == LauncherHomeGlanceAlignment.CENTER
                    ) "Center" else "Left",
                    onChoice = {
                        onSetHomeGlanceAlignment(
                            if (it == "Center") LauncherHomeGlanceAlignment.CENTER
                            else LauncherHomeGlanceAlignment.LEFT,
                        )
                    },
                )
                SettingSwitch(
                    "Quick actions",
                    experiencePreferences.showHomeQuickActions,
                    onSetShowHomeQuickActions,
                )
                SettingSwitch(
                    "Page indicator",
                    experiencePreferences.showHomePageIndicator,
                    onSetShowHomePageIndicator,
                )
                SettingSwitch("Lock layout", preferences.layoutLocked, onSetLayoutLocked)
            }

            SettingsSection("Dock", "Bottom-row apps and material") {
                Text(
                    "Dock material",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Glass", "Clear", "Edge"),
                    selected = when (experiencePreferences.dockStyle) {
                        LauncherDockStyle.GLASS -> "Glass"
                        LauncherDockStyle.CLEAR -> "Clear"
                        LauncherDockStyle.EDGE -> "Edge"
                    },
                    onChoice = {
                        onSetDockStyle(
                            when (it) {
                                "Clear" -> LauncherDockStyle.CLEAR
                                "Edge" -> LauncherDockStyle.EDGE
                                else -> LauncherDockStyle.GLASS
                            },
                        )
                    },
                )
                SettingsReadOnlyRow("Capacity", "Up to 5 apps")
                SettingsReadOnlyRow("Edit", "Long-press an app")
            }

            SettingsSection("Search", "Home access and Launcher Universal Search") {
                ChoiceRow(
                    choices = listOf("Gesture only", "Show bar"),
                    selected = if (preferences.universalSearchHomeMode == LauncherUniversalSearchHomeMode.PERMANENT) "Show bar" else "Gesture only",
                    onChoice = {
                        onSetUniversalSearchHomeMode(
                            if (it == "Show bar") LauncherUniversalSearchHomeMode.PERMANENT
                            else LauncherUniversalSearchHomeMode.SWIPE_DOWN_ONLY,
                        )
                    },
                )
                if (preferences.universalSearchHomeMode == LauncherUniversalSearchHomeMode.PERMANENT) {
                    Text(
                        "Home bar position",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ChoiceRow(
                        choices = listOf("Top", "Bottom"),
                        selected = if (
                            experiencePreferences.homeSearchPlacement == LauncherHomeSearchPlacement.TOP
                        ) "Top" else "Bottom",
                        onChoice = {
                            onSetHomeSearchPlacement(
                                if (it == "Top") LauncherHomeSearchPlacement.TOP
                                else LauncherHomeSearchPlacement.BOTTOM,
                            )
                        },
                    )
                    Text(
                        "Home bar style",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ChoiceRow(
                        choices = listOf("Glass", "Clear", "Solid"),
                        selected = when (experiencePreferences.homeSearchStyle) {
                            LauncherHomeSearchStyle.GLASS -> "Glass"
                            LauncherHomeSearchStyle.CLEAR -> "Clear"
                            LauncherHomeSearchStyle.SOLID -> "Solid"
                        },
                        onChoice = {
                            onSetHomeSearchStyle(
                                when (it) {
                                    "Clear" -> LauncherHomeSearchStyle.CLEAR
                                    "Solid" -> LauncherHomeSearchStyle.SOLID
                                    else -> LauncherHomeSearchStyle.GLASS
                                },
                            )
                        },
                    )
                }
                SettingsReadOnlyRow("Home gestures", "Configured in Gestures")
                SettingsReadOnlyRow("Core provider", "Installed apps · Launcher")
            }

            SettingsSection("App drawer", "Profiles, layout, density and background") {
                SettingsReadOnlyRow("Profiles", "User Apps · Work Apps when available")
                Text(
                    "Layout",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Grid", "Compact", "List", "Category"),
                    selected = drawerLayoutMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onChoice = {
                        onSetDrawerLayoutMode(
                            when (it) {
                                "Compact" -> LauncherDrawerLayoutMode.COMPACT
                                "List" -> LauncherDrawerLayoutMode.LIST
                                "Category" -> LauncherDrawerLayoutMode.CATEGORY
                                else -> LauncherDrawerLayoutMode.GRID
                            },
                        )
                    },
                )
                Text(
                    "Columns",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("4", "5", "6"),
                    selected = preferences.drawerColumns.toString(),
                    onChoice = { onSetDrawerColumns(it.toInt()) },
                )
                Text(
                    "Spacing",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Tight", "Standard", "Relaxed"),
                    selected = when (experiencePreferences.drawerSpacing) {
                        LauncherDrawerSpacing.TIGHT -> "Tight"
                        LauncherDrawerSpacing.STANDARD -> "Standard"
                        LauncherDrawerSpacing.RELAXED -> "Relaxed"
                    },
                    onChoice = {
                        onSetDrawerSpacing(
                            when (it) {
                                "Tight" -> LauncherDrawerSpacing.TIGHT
                                "Relaxed" -> LauncherDrawerSpacing.RELAXED
                                else -> LauncherDrawerSpacing.STANDARD
                            },
                        )
                    },
                )
                Text(
                    "Background",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Glass", "Solid"),
                    selected = if (experiencePreferences.drawerBackdrop == LauncherDrawerBackdrop.GLASS) "Glass" else "Solid",
                    onChoice = {
                        onSetDrawerBackdrop(
                            if (it == "Solid") LauncherDrawerBackdrop.SOLID
                            else LauncherDrawerBackdrop.GLASS,
                        )
                    },
                )
                Text(
                    "Navigation",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Pages", "Scroll"),
                    selected = if (
                        experiencePreferences.drawerNavigation == LauncherDrawerNavigation.PAGES
                    ) "Pages" else "Scroll",
                    onChoice = {
                        onSetDrawerNavigation(
                            if (it == "Scroll") LauncherDrawerNavigation.SCROLL
                            else LauncherDrawerNavigation.PAGES,
                        )
                    },
                )
                if (
                    experiencePreferences.drawerNavigation == LauncherDrawerNavigation.PAGES &&
                    drawerLayoutMode != LauncherDrawerLayoutMode.LIST
                ) {
                    Text(
                        "Rows per page",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ChoiceRow(
                        choices = listOf("4", "5", "6"),
                        selected = experiencePreferences.drawerPageRows.toString(),
                        onChoice = { onSetDrawerPageRows(it.toInt()) },
                    )
                }
                SettingSwitch("Show app labels", preferences.showLabels, onSetShowLabels)
                SettingSwitch(
                    "Show app count",
                    experiencePreferences.showDrawerAppCount,
                    onSetShowDrawerAppCount,
                )
            }

            SettingsSection("Icons", "Size across Home and Apps") {
                ChoiceRow(
                    choices = listOf("Small", "Medium", "Large"),
                    selected = when {
                        preferences.iconScale < 0.95f -> "Small"
                        preferences.iconScale > 1.05f -> "Large"
                        else -> "Medium"
                    },
                    onChoice = {
                        onSetIconScale(
                            when (it) {
                                "Small" -> 0.85f
                                "Large" -> 1.15f
                                else -> 1f
                            },
                        )
                    },
                )
            }

            SettingsSection("Appearance", "Glaze theme and wallpaper treatment") {
                GlazeSettingsAction(
                    title = "Theme Manager",
                    summary = "System, Light, Dark and Deep Dark",
                    value = themeMode.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                    onClick = onOpenThemeManager,
                )
                Text(
                    "Wallpaper shade",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Off", "Soft", "Strong"),
                    selected = when (experiencePreferences.wallpaperShade) {
                        LauncherWallpaperShade.OFF -> "Off"
                        LauncherWallpaperShade.SOFT -> "Soft"
                        LauncherWallpaperShade.STRONG -> "Strong"
                    },
                    onChoice = {
                        onSetWallpaperShade(
                            when (it) {
                                "Off" -> LauncherWallpaperShade.OFF
                                "Strong" -> LauncherWallpaperShade.STRONG
                                else -> LauncherWallpaperShade.SOFT
                            },
                        )
                    },
                )
            }

            SettingsSection(
                "Gestures",
                "Assign Home gestures to Launcher actions or installed apps",
            ) {
                LauncherHomeGesture.entries.forEach { gesture ->
                    val action = when (gesture) {
                        LauncherHomeGesture.SWIPE_UP -> experiencePreferences.swipeUpAction
                        LauncherHomeGesture.SWIPE_DOWN -> experiencePreferences.swipeDownAction
                        LauncherHomeGesture.SWIPE_LEFT -> experiencePreferences.swipeLeftAction
                        LauncherHomeGesture.SWIPE_RIGHT -> experiencePreferences.swipeRightAction
                        LauncherHomeGesture.DOUBLE_TAP -> experiencePreferences.doubleTapAction
                        LauncherHomeGesture.TAP_AND_HOLD -> experiencePreferences.tapAndHoldAction
                    }
                    GestureAssignmentRow(
                        gesture = gesture,
                        actionLabel = gestureActionLabel(action, appsByKey),
                        onClick = { gestureToConfigure = gesture },
                    )
                }
                Text(
                    "Swipe left/right, Double-tap, and Tap and hold apply to empty Home space so app drag/reorder and app long-press controls remain authoritative.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsSection("System", "Default HOME and Development status") {
                if (!isDefaultHome) {
                    GlazeSettingsAction(
                        title = "Default Home app",
                        summary = "Use GoreeCloud Launcher for the Home gesture",
                        value = "Set Home",
                        onClick = onRequestHomeRole,
                    )
                } else {
                    SettingsReadOnlyRow("Default Home app", "GoreeCloud Launcher")
                }
                SettingsReadOnlyRow("Build channel", "Development")
            }
        }
    }

    gestureToConfigure?.let { gesture ->
        val currentAction = when (gesture) {
            LauncherHomeGesture.SWIPE_UP -> experiencePreferences.swipeUpAction
            LauncherHomeGesture.SWIPE_DOWN -> experiencePreferences.swipeDownAction
            LauncherHomeGesture.SWIPE_LEFT -> experiencePreferences.swipeLeftAction
            LauncherHomeGesture.SWIPE_RIGHT -> experiencePreferences.swipeRightAction
            LauncherHomeGesture.DOUBLE_TAP -> experiencePreferences.doubleTapAction
            LauncherHomeGesture.TAP_AND_HOLD -> experiencePreferences.tapAndHoldAction
        }
        GestureActionPickerDialog(
            gesture = gesture,
            currentAction = currentAction,
            apps = apps,
            onSelect = { action ->
                onSetGestureAction(gesture, action)
                gestureToConfigure = null
            },
            onDismiss = { gestureToConfigure = null },
        )
    }
}

private fun gestureActionLabel(
    action: LauncherGestureAction,
    appsByKey: Map<String, LauncherActivityInfo>,
): String = when (action.type) {
    LauncherGestureActionType.OPEN_APP ->
        action.appKey
            ?.let(appsByKey::get)
            ?.label
            ?.toString()
            ?.let { "Open $it" }
            ?: "Unavailable app"
    else -> action.type.displayName
}

@Composable
private fun GestureAssignmentRow(
    gesture: LauncherHomeGesture,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GlazeMetrics.space3, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                gesture.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                actionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun GestureActionPickerDialog(
    gesture: LauncherHomeGesture,
    currentAction: LauncherGestureAction,
    apps: List<LauncherActivityInfo>,
    onSelect: (LauncherGestureAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val builtInActions = remember {
        listOf(
            LauncherGestureActionType.NONE,
            LauncherGestureActionType.APPS,
            LauncherGestureActionType.UNIVERSAL_SEARCH,
            LauncherGestureActionType.LAUNCHER_SETTINGS,
            LauncherGestureActionType.HOME_EDITOR,
            LauncherGestureActionType.WALLPAPER,
            LauncherGestureActionType.THEME_MANAGER,
        )
    }
    val sortedApps = remember(apps) {
        apps.sortedWith(
            compareBy<LauncherActivityInfo> { it.label.toString().lowercase(Locale.getDefault()) }
                .thenBy { it.componentName.packageName },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(gesture.displayName) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    Text(
                        "Launcher actions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                lazyItems(builtInActions, key = { it.storageValue }) { type ->
                    val selected = currentAction.type == type
                    TextButton(
                        onClick = {
                            onSelect(LauncherGestureAction.builtIn(type))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(type.displayName)
                            if (selected) {
                                Text(
                                    "Selected",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(GlazeMetrics.space2))
                    Text(
                        "Open app",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                lazyItems(
                    items = sortedApps,
                    key = { app -> app.workspaceKey() },
                ) { app ->
                    val appKey = app.workspaceKey()
                    val selected =
                        currentAction.type == LauncherGestureActionType.OPEN_APP &&
                            currentAction.appKey == appKey
                    TextButton(
                        onClick = {
                            onSelect(LauncherGestureAction.openApp(appKey))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(app.label.toString())
                                Text(
                                    app.componentName.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (selected) {
                                Text(
                                    "Selected",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SettingsReadOnlyRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    summary: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(GlazeMetrics.space4),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun ChoiceRow(
    choices: List<String>,
    selected: String,
    onChoice: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
    ) {
        choices.forEach { label ->
            val isSelected = label == selected
            Surface(
                onClick = { onChoice(label) },
                shape = RoundedCornerShape(GlazeMetrics.radiusPill),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                ),
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun GlazeSettingsAction(
    title: String,
    summary: String,
    value: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(GlazeMetrics.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun GlazeTextAction(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun GlazeRoundAction(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun GlazeSearchCapsule(
    value: String,
    style: LauncherHomeSearchStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val solid = style == LauncherHomeSearchStyle.SOLID
    val foreground = if (solid) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }
    val background = when (style) {
        LauncherHomeSearchStyle.GLASS -> GlazeAtmosphere.canvasBlack.copy(alpha = 0.34f)
        LauncherHomeSearchStyle.CLEAR -> GlazeAtmosphere.canvasBlack.copy(alpha = 0.12f)
        LauncherHomeSearchStyle.SOLID -> MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }
    val outline = when (style) {
        LauncherHomeSearchStyle.GLASS -> Color.White.copy(alpha = 0.14f)
        LauncherHomeSearchStyle.CLEAR -> Color.White.copy(alpha = 0.24f)
        LauncherHomeSearchStyle.SOLID -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    }
    val leadingFill = when (style) {
        LauncherHomeSearchStyle.GLASS -> Color.White.copy(alpha = 0.13f)
        LauncherHomeSearchStyle.CLEAR -> Color.White.copy(alpha = 0.06f)
        LauncherHomeSearchStyle.SOLID -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }
    val height = if (style == LauncherHomeSearchStyle.CLEAR) 50.dp else 54.dp

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = background,
        border = BorderStroke(1.dp, outline),
        shadowElevation = if (style == LauncherHomeSearchStyle.CLEAR) 0.dp else 3.dp,
    ) {
        Row(
            modifier = Modifier.height(height).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(if (style == LauncherHomeSearchStyle.CLEAR) 32.dp else 36.dp),
                shape = CircleShape,
                color = leadingFill,
                border = if (style == LauncherHomeSearchStyle.CLEAR) null else {
                    BorderStroke(1.dp, outline.copy(alpha = 0.72f))
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "⌕",
                        style = MaterialTheme.typography.titleMedium,
                        color = foreground.copy(alpha = 0.94f),
                    )
                }
            }
            Text(
                value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = foreground.copy(alpha = if (solid) 0.86f else 0.90f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (style != LauncherHomeSearchStyle.CLEAR) {
                Text(
                    "Search",
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = foreground.copy(alpha = 0.56f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun GlazeAppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    darkSurface: Boolean = false,
    requestFocus: Boolean = false,
    placeholder: String = "Search apps",
    inputTestTag: String? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        color = if (darkSurface) Color.White.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        border = BorderStroke(
            1.dp,
            if (darkSurface) Color.White.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        ),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (darkSurface) Color.White else MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .focusRequester(focusRequester)
                .then(
                    inputTestTag?.let { tag -> Modifier.testTag(tag) } ?: Modifier,
                )
                .padding(horizontal = GlazeMetrics.space4),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    Text("⌕", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text(
                                placeholder,
                                color = if (darkSurface) Color.White.copy(alpha = 0.62f)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )
    }
}

@Composable
private fun GlazeDock(
    apps: List<LauncherActivityInfo>,
    iconScale: Float,
    style: LauncherDockStyle,
    layoutLocked: Boolean,
    editMode: Boolean,
    activeDrag: LauncherAppDragData?,
    dragPoint: Offset?,
    onDockBoundsChanged: (Rect) -> Unit,
    dockItemBounds: MutableMap<String, Rect>,
    onBeginLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onUpdateLocalDrag: (Offset) -> Unit,
    onEndLocalDrag: (LauncherAppDragData, Offset) -> Unit,
    onCancelLocalDrag: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    val shape = if (style == LauncherDockStyle.EDGE) {
        RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 30.dp,
            bottomStart = 12.dp,
            bottomEnd = 12.dp,
        )
    } else {
        RoundedCornerShape(GlazeMetrics.radius2ExtraLarge)
    }
    val color = when (style) {
        LauncherDockStyle.CLEAR -> Color.Transparent
        LauncherDockStyle.GLASS -> GlazeAtmosphere.canvasBlack.copy(alpha = 0.24f)
        LauncherDockStyle.EDGE -> GlazeAtmosphere.canvasBlack.copy(alpha = 0.40f)
    }
    var measuredBounds by remember { mutableStateOf<Rect?>(null) }
    val dockHovered = activeDrag != null &&
        dragPoint?.let { point -> measuredBounds?.contains(point) } == true
    val border = when {
        dockHovered -> BorderStroke(2.dp, Color.White.copy(alpha = 0.58f))
        style == LauncherDockStyle.CLEAR -> null
        else -> BorderStroke(
            1.dp,
            Color.White.copy(alpha = if (style == LauncherDockStyle.EDGE) 0.12f else 0.10f),
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                val bounds = it.boundsInRoot()
                measuredBounds = bounds
                onDockBoundsChanged(bounds)
            },
        shape = shape,
        color = color,
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (style == LauncherDockStyle.EDGE) 78.dp else 72.dp)
                .padding(horizontal = GlazeMetrics.space2),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (apps.isEmpty() && activeDrag != null) {
                Text(
                    "Drop in Dock",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.76f),
                )
            } else {
                apps.forEach { app ->
                    val appKey = app.workspaceKey()
                    Box(
                        modifier = Modifier.onGloballyPositioned {
                            dockItemBounds[appKey] = it.boundsInRoot()
                        },
                    ) {
                        HomeFavoriteTile(
                            app = app,
                            displayLabel = app.label.toString(),
                            iconScale = iconScale,
                            showLabel = false,
                            layoutLocked = layoutLocked,
                            editMode = editMode,
                            dragData = if (layoutLocked) null else {
                                LauncherAppDragData(
                                    appKey = appKey,
                                    origin = LauncherAppDragOrigin.DOCK,
                                )
                            },
                            onBeginLocalDrag = onBeginLocalDrag,
                            onUpdateLocalDrag = onUpdateLocalDrag,
                            onEndLocalDrag = onEndLocalDrag,
                            onCancelLocalDrag = onCancelLocalDrag,
                            onLaunchApp = onLaunchApp,
                            onManageApp = onManageApp,
                            onSwipeUp = onSwipeUp,
                            onSwipeDown = onSwipeDown,
                            modifier = Modifier.size(60.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherAppTile(
    app: LauncherActivityInfo,
    iconScale: Float,
    showLabel: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier,
    dragData: LauncherAppDragData? = null,
    onBoundsChanged: ((Rect) -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    labelOnWallpaper: Boolean = false,
) {
    val icon = rememberLauncherAppIcon(app)
    val base = if (compact) 50f else 52f
    val iconSize = (base * iconScale.coerceIn(0.85f, 1.15f)).dp
    val swipeThreshold = with(LocalDensity.current) { 42.dp.toPx() }
    val dragModifier = if (dragData != null) {
        Modifier.dragAndDropSource(transferData = { _ -> dragData.toTransferData() })
    } else {
        Modifier
    }
    val gestureModifier = if (onSwipeUp != null || onSwipeDown != null) {
        Modifier.pointerInput(onSwipeUp, onSwipeDown, swipeThreshold) {
            var drag = 0f
            var triggered = false
            detectVerticalDragGestures(
                onDragStart = {
                    drag = 0f
                    triggered = false
                },
                onDragCancel = {
                    drag = 0f
                    triggered = false
                },
                onDragEnd = {
                    drag = 0f
                    triggered = false
                },
                onVerticalDrag = { change, amount ->
                    change.consume()
                    if (!triggered) {
                        drag += amount
                        when {
                            drag <= -swipeThreshold -> {
                                triggered = true
                                onSwipeUp?.invoke()
                            }
                            drag >= swipeThreshold -> {
                                triggered = true
                                onSwipeDown?.invoke()
                            }
                        }
                    }
                },
            )
        }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                onBoundsChanged?.invoke(coordinates.boundsInRoot())
            }
            .then(dragModifier)
            .then(gestureModifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.label.toString(),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(iconSize),
            )
        } else {
            Surface(
                modifier = Modifier.size(iconSize),
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(app.label.toString().take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showLabel) {
            Spacer(Modifier.height(if (compact) 3.dp else 4.dp))
            Text(
                app.label.toString(),
                modifier = Modifier.fillMaxWidth(),
                style = if (labelOnWallpaper) {
                    MaterialTheme.typography.labelSmall.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.60f),
                            offset = Offset(0f, 1.5f),
                            blurRadius = 5f,
                        ),
                    )
                } else {
                    MaterialTheme.typography.labelSmall
                },
                color = if (labelOnWallpaper) Color.White else Color.Unspecified,
                textAlign = TextAlign.Center,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherAppListRow(
    app: LauncherActivityInfo,
    iconScale: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    dragData: LauncherAppDragData? = null,
) {
    val icon = rememberLauncherAppIcon(app)
    val iconSize = (44f * iconScale.coerceIn(0.85f, 1.15f)).dp
    val dragModifier = if (dragData != null) {
        Modifier.dragAndDropSource(transferData = { _ -> dragData.toTransferData() })
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(dragModifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.label.toString(),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(iconSize),
            )
        } else {
            Surface(
                modifier = Modifier.size(iconSize),
                shape = RoundedCornerShape(13.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(app.label.toString().take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                app.label.toString(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                app.componentName.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppPlacementDialog(
    app: LauncherActivityInfo,
    workspace: WorkspaceState,
    layoutLocked: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleDock: () -> Unit,
    onMoveFavorite: (WorkspaceMoveDirection) -> Unit,
    onMoveDock: (WorkspaceMoveDirection) -> Unit,
    homeLabelOverride: String?,
    onSetHomeLabelOverride: (String?) -> Unit,
    onOpenAppInfo: () -> Unit,
    onRequestUninstall: () -> Unit,
    onClose: () -> Unit,
) {
    val key = app.workspaceKey()
    val favoriteIndex = workspace.favoriteKeys.indexOf(key)
    val dockIndex = workspace.dockKeys.indexOf(key)
    val isFavorite = favoriteIndex >= 0
    val isDocked = dockIndex >= 0
    val dockFull = !isDocked && workspace.dockKeys.size >= MAX_DOCK_ITEMS
    val originalLabel = app.label.toString()
    var labelDraft by remember(key, homeLabelOverride) {
        mutableStateOf(homeLabelOverride ?: originalLabel)
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(homeLabelOverride ?: originalLabel) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (layoutLocked) {
                    Text("Home layout is locked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    enabled = !layoutLocked,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (isFavorite) "Remove from Home" else "Add to Home") }
                OutlinedButton(
                    onClick = onToggleDock,
                    enabled = !layoutLocked && !dockFull,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (isDocked) "Remove from Dock" else "Add to Dock") }
                if (isFavorite && !layoutLocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onMoveFavorite(WorkspaceMoveDirection.EARLIER) }) { Text("Earlier") }
                        TextButton(onClick = { onMoveFavorite(WorkspaceMoveDirection.LATER) }) { Text("Later") }
                    }
                }
                if (isDocked && !layoutLocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onMoveDock(WorkspaceMoveDirection.EARLIER) }) { Text("Dock left") }
                        TextButton(onClick = { onMoveDock(WorkspaceMoveDirection.LATER) }) { Text("Dock right") }
                    }
                }

                if (isFavorite) {
                    Text("Rename on Home", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = labelDraft,
                        onValueChange = { labelDraft = it.take(LauncherHomeLabelPolicy.MAX_LABEL_LENGTH) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Rename this label on Home only.") },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                val normalized = LauncherHomeLabelPolicy.normalize(labelDraft)
                                val original = LauncherHomeLabelPolicy.normalize(originalLabel)
                                onSetHomeLabelOverride(normalized?.takeUnless { it == original })
                            },
                        ) { Text("Save label") }
                        TextButton(
                            onClick = {
                                labelDraft = originalLabel
                                onSetHomeLabelOverride(null)
                            },
                        ) { Text("Reset") }
                    }
                }

                HorizontalDivider()
                OutlinedButton(
                    onClick = {
                        onOpenAppInfo()
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("App info") }
                OutlinedButton(
                    onClick = {
                        onRequestUninstall()
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Uninstall app") }
                Text(
                    "Android will show its system uninstall confirmation before anything is removed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Done") } },
    )
}
