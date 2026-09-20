package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goreecloud.launcher.core.launcher.GoreeCloudIndexHomeMode
import com.goreecloud.launcher.core.launcher.LauncherDockStyle
import com.goreecloud.launcher.core.launcher.LauncherDrawerBackdrop
import com.goreecloud.launcher.core.launcher.LauncherDrawerLayoutMode
import com.goreecloud.launcher.core.launcher.LauncherDrawerNavigation
import com.goreecloud.launcher.core.launcher.LauncherDrawerSearchPlacement
import com.goreecloud.launcher.core.launcher.LauncherExperiencePreferences
import com.goreecloud.launcher.core.launcher.LauncherHomeCardStyle
import com.goreecloud.launcher.core.launcher.LauncherHomeGlanceAlignment
import com.goreecloud.launcher.core.launcher.LauncherHomeSearchPlacement
import com.goreecloud.launcher.core.launcher.LauncherPreferences
import com.goreecloud.launcher.core.launcher.LauncherWallpaperShade
import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.theme.GlazeAtmosphere
import com.goreecloud.launcher.ui.theme.GlazeMetrics
import com.goreecloud.launcher.ui.theme.GlazeThemeMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LauncherSurfaceMode { HOME, DRAWER, SETTINGS }

@Composable
fun LauncherBetaRoot(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    homePageCount: Int,
    onManageHomePages: () -> Unit,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onOpenUniversalSearch: () -> Unit,
    onToggleFavorite: (LauncherActivityInfo) -> Unit,
    onToggleDock: (LauncherActivityInfo) -> Unit,
    onMoveFavorite: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveDock: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    themeMode: GlazeThemeMode,
    onSetThemeMode: (GlazeThemeMode) -> Unit,
    onSetHomeGrid: (Int, Int) -> Unit,
    onSetDrawerColumns: (Int) -> Unit,
    onSetDrawerLayoutMode: (LauncherDrawerLayoutMode) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetIconScale: (Float) -> Unit,
    onSetLayoutLocked: (Boolean) -> Unit,
    onSetIndexHomeMode: (GoreeCloudIndexHomeMode) -> Unit,
    onSetHomeCardStyle: (LauncherHomeCardStyle) -> Unit,
    onSetShowHomeQuickActions: (Boolean) -> Unit,
    onSetShowHomePageIndicator: (Boolean) -> Unit,
    onSetDrawerBackdrop: (LauncherDrawerBackdrop) -> Unit,
    onSetDrawerSearchPlacement: (LauncherDrawerSearchPlacement) -> Unit,
    onSetDrawerNavigation: (LauncherDrawerNavigation) -> Unit,
    onSetDrawerPageRows: (Int) -> Unit,
    onSetShowDrawerAppCount: (Boolean) -> Unit,
    onSetHomeGlanceAlignment: (LauncherHomeGlanceAlignment) -> Unit,
    onSetHomeSearchPlacement: (LauncherHomeSearchPlacement) -> Unit,
    onSetDockStyle: (LauncherDockStyle) -> Unit,
    onSetWallpaperShade: (LauncherWallpaperShade) -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onSurfaceModeChanged: (LauncherSurfaceMode) -> Unit,
) {
    var surfaceModeName by rememberSaveable { mutableStateOf(LauncherSurfaceMode.HOME.name) }
    val surfaceMode = runCatching { LauncherSurfaceMode.valueOf(surfaceModeName) }
        .getOrDefault(LauncherSurfaceMode.HOME)
    var selectedApp by remember { mutableStateOf<LauncherActivityInfo?>(null) }

    LaunchedEffect(surfaceMode) { onSurfaceModeChanged(surfaceMode) }

    AnimatedContent(
        targetState = surfaceMode,
        transitionSpec = {
            when {
                initialState == LauncherSurfaceMode.HOME &&
                    targetState == LauncherSurfaceMode.DRAWER ->
                    (
                        slideInVertically(
                            animationSpec = tween(durationMillis = 220),
                            initialOffsetY = { height -> height / 5 },
                        ) + fadeIn(animationSpec = tween(durationMillis = 160))
                    ) togetherWith (
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 180),
                            targetOffsetY = { height -> -height / 10 },
                        ) + fadeOut(animationSpec = tween(durationMillis = 120))
                    )
                initialState == LauncherSurfaceMode.DRAWER &&
                    targetState == LauncherSurfaceMode.HOME ->
                    (
                        slideInVertically(
                            animationSpec = tween(durationMillis = 180),
                            initialOffsetY = { height -> -height / 10 },
                        ) + fadeIn(animationSpec = tween(durationMillis = 140))
                    ) togetherWith (
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 220),
                            targetOffsetY = { height -> height / 5 },
                        ) + fadeOut(animationSpec = tween(durationMillis = 120))
                    )
                else ->
                    fadeIn(animationSpec = tween(durationMillis = 140)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 100))
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
                onManageHomePages = onManageHomePages,
                onLaunchApp = onLaunchApp,
                onOpenUniversalSearch = onOpenUniversalSearch,
                onManageApp = { selectedApp = it },
                onOpenDrawer = { surfaceModeName = LauncherSurfaceMode.DRAWER.name },
                onOpenSettings = { surfaceModeName = LauncherSurfaceMode.SETTINGS.name },
                onOpenWallpaperPicker = onOpenWallpaperPicker,
            )
            LauncherSurfaceMode.DRAWER -> AppDrawerSurface(
                apps = apps,
                preferences = preferences,
                drawerLayoutMode = drawerLayoutMode,
                experiencePreferences = experiencePreferences,
                onLaunchApp = onLaunchApp,
                onManageApp = { selectedApp = it },
                onHome = { surfaceModeName = LauncherSurfaceMode.HOME.name },
                onOpenSettings = { surfaceModeName = LauncherSurfaceMode.SETTINGS.name },
            )
            LauncherSurfaceMode.SETTINGS -> LauncherSettingsSurface(
                selectedThemeMode = themeMode,
                onSelectThemeMode = onSetThemeMode,
                rootContent = { onOpenThemeManager ->
                    LauncherSettingsRootSurface(
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
                        onSetIndexHomeMode = onSetIndexHomeMode,
                        onSetHomeCardStyle = onSetHomeCardStyle,
                        onSetShowHomeQuickActions = onSetShowHomeQuickActions,
                        onSetShowHomePageIndicator = onSetShowHomePageIndicator,
                        onSetDrawerBackdrop = onSetDrawerBackdrop,
                        onSetDrawerSearchPlacement = onSetDrawerSearchPlacement,
                        onSetDrawerNavigation = onSetDrawerNavigation,
                        onSetDrawerPageRows = onSetDrawerPageRows,
                        onSetShowDrawerAppCount = onSetShowDrawerAppCount,
                        onSetHomeGlanceAlignment = onSetHomeGlanceAlignment,
                        onSetHomeSearchPlacement = onSetHomeSearchPlacement,
                        onSetDockStyle = onSetDockStyle,
                        onSetWallpaperShade = onSetWallpaperShade,
                        onOpenThemeManager = onOpenThemeManager,
                        onBack = { surfaceModeName = LauncherSurfaceMode.HOME.name },
                    )
                },
            )
        }
    }

    selectedApp?.let { app ->
        AppPlacementDialog(
            app = app,
            workspace = workspace,
            layoutLocked = preferences.layoutLocked,
            onToggleFavorite = { onToggleFavorite(app) },
            onToggleDock = { onToggleDock(app) },
            onMoveFavorite = { onMoveFavorite(app, it) },
            onMoveDock = { onMoveDock(app, it) },
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
    onManageHomePages: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onOpenUniversalSearch: () -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val favoriteApps = remember(appsByKey, workspace.favoriteKeys, preferences.homeCapacity) {
        workspace.favoriteKeys.mapNotNull(appsByKey::get).take(preferences.homeCapacity)
    }
    val dockApps = remember(appsByKey, workspace.dockKeys) {
        workspace.dockKeys.mapNotNull(appsByKey::get).take(MAX_DOCK_ITEMS)
    }
    val swipeThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var showHomeEditor by rememberSaveable { mutableStateOf(false) }

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
    val showPermanentSearch = preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT
    val searchAtTop =
        experiencePreferences.homeSearchPlacement == LauncherHomeSearchPlacement.TOP

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showHomeEditor = true })
            }
            .pointerInput(onOpenUniversalSearch, onOpenDrawer, swipeThreshold) {
                var drag = 0f
                detectVerticalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragCancel = { drag = 0f },
                    onDragEnd = {
                        when {
                            drag >= swipeThreshold -> onOpenUniversalSearch()
                            drag <= -swipeThreshold -> onOpenDrawer()
                        }
                        drag = 0f
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        drag += amount
                    },
                )
            },
    ) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space2),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            if (experiencePreferences.homeCardStyle != LauncherHomeCardStyle.OFF) {
                HomeAtAGlance(
                    now = now,
                    compact = experiencePreferences.homeCardStyle == LauncherHomeCardStyle.COMPACT,
                    alignment = experiencePreferences.homeGlanceAlignment,
                )
            }

            if (showPermanentSearch && searchAtTop) {
                GlazeSearchCapsule(
                    value = "Search phone",
                    onClick = onOpenUniversalSearch,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (experiencePreferences.showHomeQuickActions) {
                HomeQuickActions(
                    onOpenApps = onOpenDrawer,
                    onOpenSearch = onOpenUniversalSearch,
                    onOpenSettings = onOpenSettings,
                )
            }

            Spacer(Modifier.weight(1f))

            if (favoriteApps.isEmpty() && dockApps.isEmpty()) {
                EmptyWorkspaceCard(
                    onOpenApps = onOpenDrawer,
                )
            } else if (favoriteApps.isNotEmpty()) {
                HomeFavoritesGrid(
                    apps = favoriteApps,
                    columns = preferences.homeColumns,
                    iconScale = preferences.iconScale,
                    showLabels = preferences.showLabels,
                    onLaunchApp = onLaunchApp,
                    onManageApp = onManageApp,
                    onSwipeUp = onOpenDrawer,
                    onSwipeDown = onOpenUniversalSearch,
                )
            }

            if (showPermanentSearch && !searchAtTop) {
                GlazeSearchCapsule(
                    value = "Search phone",
                    onClick = onOpenUniversalSearch,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (dockApps.isNotEmpty()) {
                GlazeDock(
                    apps = dockApps,
                    iconScale = preferences.iconScale,
                    style = experiencePreferences.dockStyle,
                    onLaunchApp = onLaunchApp,
                    onManageApp = onManageApp,
                    onSwipeUp = onOpenDrawer,
                    onSwipeDown = onOpenUniversalSearch,
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
    val showSearch = preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT

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
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
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
                HomeEditorPreviewSearch()
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
                HomeEditorPreviewSearch()
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
private fun HomeEditorPreviewSearch() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GlazeMetrics.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            Text("⌕", color = Color.White.copy(alpha = 0.84f))
            Text(
                "Search phone",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
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
    iconScale: Float,
    showLabels: Boolean,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
    ) {
        apps.chunked(columns).forEach { rowApps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
            ) {
                rowApps.forEach { app ->
                    LauncherAppTile(
                        app = app,
                        iconScale = iconScale,
                        showLabel = showLabels,
                        compact = true,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onManageApp(app) },
                        onSwipeUp = onSwipeUp,
                        onSwipeDown = onSwipeDown,
                        modifier = Modifier
                            .weight(1f)
                            .height(78.dp),
                    )
                }
                repeat(columns - rowApps.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
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
private fun AppDrawerSurface(
    apps: List<LauncherActivityInfo>,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onHome: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredApps = remember(apps, query) {
        apps.filter { app ->
            LauncherLocalAppSearch.matches(
                label = app.label.toString(),
                packageName = app.componentName.packageName,
                rawQuery = query,
            )
        }
    }
    val dismissThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    val glass = experiencePreferences.drawerBackdrop == LauncherDrawerBackdrop.GLASS
    val searchAtBottom =
        experiencePreferences.drawerSearchPlacement == LauncherDrawerSearchPlacement.BOTTOM
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
                        detectVerticalDragGestures(
                            onDragStart = { drag = 0f },
                            onDragCancel = { drag = 0f },
                            onDragEnd = {
                                if (drag >= dismissThreshold) onHome()
                                drag = 0f
                            },
                            onVerticalDrag = { _, amount -> drag += amount },
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
                            "Apps",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (experiencePreferences.showDrawerAppCount) {
                            Text(
                                filteredApps.size.toString() + " installed",
                                style = MaterialTheme.typography.bodySmall,
                                color = drawerSecondaryColor,
                            )
                        } else {
                            Text(
                                when (drawerLayoutMode) {
                                    LauncherDrawerLayoutMode.GRID -> "Grid · ${preferences.drawerColumns} columns"
                                    LauncherDrawerLayoutMode.COMPACT -> "Compact · ${preferences.drawerColumns} columns"
                                    LauncherDrawerLayoutMode.LIST -> "Alphabetical list"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = drawerSecondaryColor,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2)) {
                        GlazeRoundAction("⚙", onOpenSettings)
                        GlazeRoundAction("⌄", onHome)
                    }
                }

                if (!searchAtBottom) {
                    Spacer(Modifier.height(GlazeMetrics.space3))
                    GlazeAppSearchField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        darkSurface = glass,
                    )
                }

                Spacer(Modifier.height(GlazeMetrics.space2))
                DrawerAppsContent(
                    apps = filteredApps,
                    query = query,
                    preferences = preferences,
                    drawerLayoutMode = drawerLayoutMode,
                    experiencePreferences = experiencePreferences,
                    onLaunchApp = onLaunchApp,
                    onManageApp = onManageApp,
                    secondaryColor = drawerSecondaryColor,
                    modifier = Modifier.weight(1f),
                )

                if (searchAtBottom) {
                    Spacer(Modifier.height(GlazeMetrics.space2))
                    GlazeAppSearchField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        darkSurface = glass,
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
        drawerLayoutMode != LauncherDrawerLayoutMode.LIST

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
                            GlazeMetrics.space1
                        } else {
                            GlazeMetrics.space2
                        },
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        if (drawerLayoutMode == LauncherDrawerLayoutMode.COMPACT) {
                            GlazeMetrics.space1
                        } else {
                            GlazeMetrics.space2
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
                            modifier = Modifier.height(
                                if (drawerLayoutMode == LauncherDrawerLayoutMode.COMPACT) 72.dp
                                else 88.dp,
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
        LauncherDrawerLayoutMode.GRID -> LazyVerticalGrid(
            columns = GridCells.Fixed(preferences.drawerColumns),
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = GlazeMetrics.space2),
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            items(apps, key = { it.workspaceKey() }) { app ->
                LauncherAppTile(
                    app = app,
                    iconScale = preferences.iconScale,
                    showLabel = preferences.showLabels,
                    compact = false,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onManageApp(app) },
                    modifier = Modifier.height(88.dp),
                )
            }
        }
        LauncherDrawerLayoutMode.COMPACT -> LazyVerticalGrid(
            columns = GridCells.Fixed(preferences.drawerColumns),
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = GlazeMetrics.space2),
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            items(apps, key = { it.workspaceKey() }) { app ->
                LauncherAppTile(
                    app = app,
                    iconScale = preferences.iconScale,
                    showLabel = preferences.showLabels,
                    compact = true,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onManageApp(app) },
                    modifier = Modifier.height(72.dp),
                )
            }
        }
        LauncherDrawerLayoutMode.LIST -> LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = GlazeMetrics.space2),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            lazyItems(apps, key = { it.workspaceKey() }) { app ->
                LauncherAppListRow(
                    app = app,
                    iconScale = preferences.iconScale,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onManageApp(app) },
                )
            }
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
private fun LauncherSettingsRootSurface(
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
    onSetIndexHomeMode: (GoreeCloudIndexHomeMode) -> Unit,
    onSetHomeCardStyle: (LauncherHomeCardStyle) -> Unit,
    onSetShowHomeQuickActions: (Boolean) -> Unit,
    onSetShowHomePageIndicator: (Boolean) -> Unit,
    onSetDrawerBackdrop: (LauncherDrawerBackdrop) -> Unit,
    onSetDrawerSearchPlacement: (LauncherDrawerSearchPlacement) -> Unit,
    onSetDrawerNavigation: (LauncherDrawerNavigation) -> Unit,
    onSetDrawerPageRows: (Int) -> Unit,
    onSetShowDrawerAppCount: (Boolean) -> Unit,
    onSetHomeGlanceAlignment: (LauncherHomeGlanceAlignment) -> Unit,
    onSetDockStyle: (LauncherDockStyle) -> Unit,
    onSetWallpaperShade: (LauncherWallpaperShade) -> Unit,
    onOpenThemeManager: () -> Unit,
    onBack: () -> Unit,
) {
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
                    choices = listOf("Glass", "Clear"),
                    selected = if (experiencePreferences.dockStyle == LauncherDockStyle.GLASS) "Glass" else "Clear",
                    onChoice = {
                        onSetDockStyle(
                            if (it == "Clear") LauncherDockStyle.CLEAR
                            else LauncherDockStyle.GLASS,
                        )
                    },
                )
                SettingsReadOnlyRow("Capacity", "Up to 5 apps")
                SettingsReadOnlyRow("Edit", "Long-press an app")
            }

            SettingsSection("Search", "Home access and launcher search") {
                ChoiceRow(
                    choices = listOf("Swipe down", "Show bar"),
                    selected = if (preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT) "Show bar" else "Swipe down",
                    onChoice = {
                        onSetIndexHomeMode(
                            if (it == "Show bar") GoreeCloudIndexHomeMode.PERMANENT
                            else GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
                        )
                    },
                )
                SettingsReadOnlyRow("Home gesture", "Swipe down")
                SettingsReadOnlyRow("App drawer", "Search installed apps")
            }

            SettingsSection("App drawer", "Layout, density and background") {
                Text(
                    "Layout",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Grid", "Compact", "List"),
                    selected = drawerLayoutMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onChoice = {
                        onSetDrawerLayoutMode(
                            when (it) {
                                "Compact" -> LauncherDrawerLayoutMode.COMPACT
                                "List" -> LauncherDrawerLayoutMode.LIST
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
                Text(
                    "Search position",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Top", "Bottom"),
                    selected = if (
                        experiencePreferences.drawerSearchPlacement == LauncherDrawerSearchPlacement.TOP
                    ) "Top" else "Bottom",
                    onChoice = {
                        onSetDrawerSearchPlacement(
                            if (it == "Top") LauncherDrawerSearchPlacement.TOP
                            else LauncherDrawerSearchPlacement.BOTTOM,
                        )
                    },
                )
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

            SettingsSection("Gestures", "Current implemented shortcuts") {
                SettingsReadOnlyRow("Swipe up", "Open Apps")
                SettingsReadOnlyRow("Swipe down", "Open GoreeCloud Search")
                SettingsReadOnlyRow("Long-press Home", "Open Home editor")
                SettingsReadOnlyRow("Long-press app", "Home and dock actions")
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.height(46.dp).padding(horizontal = GlazeMetrics.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            Text("⌕", style = MaterialTheme.typography.titleMedium)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GlazeAppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    darkSurface: Boolean = false,
) {
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
                                "Search apps",
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
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    val glass = style == LauncherDockStyle.GLASS
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlazeMetrics.radius2ExtraLarge),
        color = if (glass) {
            GlazeAtmosphere.canvasBlack.copy(alpha = 0.24f)
        } else {
            Color.Transparent
        },
        border = if (glass) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = GlazeMetrics.space2),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            apps.forEach { app ->
                LauncherAppTile(
                    app = app,
                    iconScale = iconScale,
                    showLabel = false,
                    compact = true,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onManageApp(app) },
                    onSwipeUp = onSwipeUp,
                    onSwipeDown = onSwipeDown,
                    modifier = Modifier.size(60.dp),
                )
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
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
) {
    val icon = rememberLauncherAppIcon(app)
    val base = if (compact) 50f else 52f
    val iconSize = (base * iconScale.coerceIn(0.85f, 1.15f)).dp
    val swipeThreshold = with(LocalDensity.current) { 42.dp.toPx() }
    val gestureModifier = if (onSwipeUp != null || onSwipeDown != null) {
        Modifier.pointerInput(onSwipeUp, onSwipeDown, swipeThreshold) {
            var drag = 0f
            detectVerticalDragGestures(
                onDragStart = { drag = 0f },
                onDragCancel = { drag = 0f },
                onDragEnd = {
                    when {
                        drag <= -swipeThreshold -> onSwipeUp?.invoke()
                        drag >= swipeThreshold -> onSwipeDown?.invoke()
                    }
                    drag = 0f
                },
                onVerticalDrag = { change, amount ->
                    change.consume()
                    drag += amount
                },
            )
        }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
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
                style = MaterialTheme.typography.labelSmall,
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
) {
    val icon = rememberLauncherAppIcon(app)
    val iconSize = (44f * iconScale.coerceIn(0.85f, 1.15f)).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
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
    onClose: () -> Unit,
) {
    val key = app.workspaceKey()
    val favoriteIndex = workspace.favoriteKeys.indexOf(key)
    val dockIndex = workspace.dockKeys.indexOf(key)
    val isFavorite = favoriteIndex >= 0
    val isDocked = dockIndex >= 0
    val dockFull = !isDocked && workspace.dockKeys.size >= MAX_DOCK_ITEMS

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(app.label.toString()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Done") } },
    )
}
