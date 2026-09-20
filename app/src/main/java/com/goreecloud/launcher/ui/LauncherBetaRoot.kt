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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.goreecloud.launcher.core.launcher.LauncherDrawerSearchPosition
import com.goreecloud.launcher.core.launcher.LauncherExperiencePreferences
import com.goreecloud.launcher.core.launcher.LauncherHomeCardStyle
import com.goreecloud.launcher.core.launcher.LauncherHomeLayoutStyle
import com.goreecloud.launcher.core.launcher.LauncherPreferences
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

enum class LauncherSurfaceMode { HOME, DRAWER, SETTINGS }

@Composable
fun LauncherBetaRoot(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    drawerLayoutMode: LauncherDrawerLayoutMode,
    experiencePreferences: LauncherExperiencePreferences,
    homePageCount: Int,
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
    onSetHomeLayoutStyle: (LauncherHomeLayoutStyle) -> Unit,
    onSetShowHomeQuickActions: (Boolean) -> Unit,
    onSetShowHomePageIndicator: (Boolean) -> Unit,
    onSetDockStyle: (LauncherDockStyle) -> Unit,
    onSetDrawerBackdrop: (LauncherDrawerBackdrop) -> Unit,
    onSetDrawerSearchPosition: (LauncherDrawerSearchPosition) -> Unit,
    onSetShowDrawerAppCount: (Boolean) -> Unit,
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
                isDefaultHome = isDefaultHome,
                onRequestHomeRole = onRequestHomeRole,
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
                        onSetHomeLayoutStyle = onSetHomeLayoutStyle,
                        onSetShowHomeQuickActions = onSetShowHomeQuickActions,
                        onSetShowHomePageIndicator = onSetShowHomePageIndicator,
                        onSetDockStyle = onSetDockStyle,
                        onSetDrawerBackdrop = onSetDrawerBackdrop,
                        onSetDrawerSearchPosition = onSetDrawerSearchPosition,
                        onSetShowDrawerAppCount = onSetShowDrawerAppCount,
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

@Composable
private fun HomeSurface(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    experiencePreferences: LauncherExperiencePreferences,
    homePageCount: Int,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
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
    val configuredDockApps = remember(appsByKey, workspace.dockKeys) {
        workspace.dockKeys.mapNotNull(appsByKey::get).take(MAX_DOCK_ITEMS)
    }
    val suggestedDockApps = remember(apps, configuredDockApps) {
        if (configuredDockApps.isNotEmpty()) emptyList()
        else selectSuggestedLauncherApps(apps = apps, excluded = emptySet(), count = MAX_DOCK_ITEMS)
    }
    val dockApps = if (configuredDockApps.isNotEmpty()) configuredDockApps else suggestedDockApps
    val dockKeys = remember(dockApps) { dockApps.map { it.workspaceKey() }.toSet() }
    val suggestedHomeApps = remember(apps, favoriteApps, dockKeys, preferences.homeColumns) {
        if (favoriteApps.isNotEmpty()) emptyList()
        else selectSuggestedLauncherApps(
            apps = apps,
            excluded = dockKeys,
            count = preferences.homeColumns.coerceAtMost(5),
        )
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

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { showHomeEditor = true })
                }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            GlazeAtmosphere.canvasBlack.copy(alpha = 0.03f),
                            GlazeAtmosphere.canvasBlack.copy(alpha = 0.24f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space2),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            if (!isDefaultHome) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    GlazeTextAction("Set as Home", onRequestHomeRole)
                }
            }

            if (experiencePreferences.homeCardStyle != LauncherHomeCardStyle.OFF) {
                HomeClockBlock(
                    now = now,
                    cardStyle = experiencePreferences.homeCardStyle,
                    layoutStyle = experiencePreferences.homeLayoutStyle,
                )
            }

            if (preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT) {
                GlazeSearchCapsule(
                    value = "Search GoreeCloud",
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val homeApps = if (favoriteApps.isNotEmpty()) favoriteApps else suggestedHomeApps
                if (homeApps.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(
                                when (experiencePreferences.homeLayoutStyle) {
                                    LauncherHomeLayoutStyle.SPACIOUS -> Alignment.Center
                                    LauncherHomeLayoutStyle.MINIMAL -> Alignment.BottomCenter
                                    LauncherHomeLayoutStyle.CLASSIC -> Alignment.BottomCenter
                                },
                            )
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                    ) {
                        if (favoriteApps.isEmpty()) {
                            Text(
                                "Suggested",
                                modifier = Modifier.padding(horizontal = GlazeMetrics.space1),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                        }
                        homeApps.chunked(preferences.homeColumns).forEach { rowApps ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                            ) {
                                rowApps.forEach { app ->
                                    LauncherAppTile(
                                        app = app,
                                        iconScale = preferences.iconScale,
                                        showLabel = preferences.showLabels,
                                        compact = true,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onManageApp(app) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(84.dp),
                                    )
                                }
                                repeat(preferences.homeColumns - rowApps.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (experiencePreferences.showHomePageIndicator) {
                HomePageIndicator(pageCount = homePageCount.coerceAtLeast(1), selectedPage = 0)
            }

            if (dockApps.isNotEmpty()) {
                GlazeDock(
                    apps = dockApps,
                    iconScale = preferences.iconScale,
                    style = experiencePreferences.dockStyle,
                    onLaunchApp = onLaunchApp,
                    onManageApp = onManageApp,
                )
            }
        }

        if (showHomeEditor) {
            ModalBottomSheet(
                onDismissRequest = { showHomeEditor = false },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 0.dp,
            ) {
                HomeEditorSheet(
                    isDefaultHome = isDefaultHome,
                    onWallpaper = {
                        showHomeEditor = false
                        onOpenWallpaperPicker()
                    },
                    onApps = {
                        showHomeEditor = false
                        onOpenDrawer()
                    },
                    onSearch = {
                        showHomeEditor = false
                        onOpenUniversalSearch()
                    },
                    onSettings = {
                        showHomeEditor = false
                        onOpenSettings()
                    },
                    onSetHome = {
                        showHomeEditor = false
                        onRequestHomeRole()
                    },
                )
            }
        }
    }
}

private fun selectSuggestedLauncherApps(
    apps: List<LauncherActivityInfo>,
    excluded: Set<String>,
    count: Int,
): List<LauncherActivityInfo> {
    val preferredTerms = listOf(
        "phone", "dialer", "message", "browser", "firefox", "camera",
        "gallery", "contacts", "calendar", "music", "files",
    )
    return apps
        .asSequence()
        .filterNot { it.workspaceKey() in excluded }
        .filterNot { it.componentName.packageName == "com.goreecloud.launcher" }
        .filterNot { it.componentName.packageName == "com.goreecloud.launcher.dev" }
        .sortedWith(
            compareBy<LauncherActivityInfo> { app ->
                val haystack = (
                    app.label.toString() + " " + app.componentName.packageName
                ).lowercase(Locale.getDefault())
                preferredTerms.indexOfFirst { term -> term in haystack }
                    .let { if (it == -1) 100 else it }
            }.thenBy { it.label.toString().lowercase(Locale.getDefault()) },
        )
        .take(count)
        .toList()
}

@Composable
private fun HomeClockBlock(
    now: LocalDateTime,
    cardStyle: LauncherHomeCardStyle,
    layoutStyle: LauncherHomeLayoutStyle,
) {
    val locale = Locale.getDefault()
    val time = remember(now.minute, locale) {
        now.format(DateTimeFormatter.ofPattern("h:mm", locale))
    }
    val date = remember(now.dayOfYear, locale) {
        now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", locale))
    }
    val alignment = when (layoutStyle) {
        LauncherHomeLayoutStyle.CLASSIC -> Alignment.Start
        LauncherHomeLayoutStyle.SPACIOUS -> Alignment.CenterHorizontally
        LauncherHomeLayoutStyle.MINIMAL -> Alignment.End
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            time,
            style = if (cardStyle == LauncherHomeCardStyle.COMPACT) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.displayMedium
            },
            fontWeight = FontWeight.Light,
            color = Color.White,
        )
        Text(
            date,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.82f),
        )
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
        GlazeActionTile("Apps", onOpenApps, Modifier.weight(1f))
        GlazeActionTile("Search", onOpenSearch, Modifier.weight(1f))
        GlazeActionTile("Tune", onOpenSettings, Modifier.weight(1f))
    }
}

@Composable
private fun GlazeActionTile(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = GlazeMetrics.space3, vertical = 10.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun HomePageIndicator(pageCount: Int, selectedPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount.coerceAtMost(7)) { index ->
            Surface(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == selectedPage) 7.dp else 5.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = if (index == selectedPage) 0.92f else 0.38f),
            ) {}
        }
    }
}

@Composable
private fun HomeEditorSheet(
    isDefaultHome: Boolean,
    onWallpaper: () -> Unit,
    onApps: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onSetHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space3),
        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
    ) {
        Column {
            Text(
                "Home screen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Personalize the workspace without covering your wallpaper.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            HomeEditorAction("Wallpaper", "◫", onWallpaper, Modifier.weight(1f))
            HomeEditorAction("Apps", "▦", onApps, Modifier.weight(1f))
            HomeEditorAction("Search", "⌕", onSearch, Modifier.weight(1f))
            HomeEditorAction("Settings", "⚙", onSettings, Modifier.weight(1f))
        }
        if (!isDefaultHome) {
            GlazeSettingsAction(
                title = "Set as Home",
                summary = "Use GoreeCloud Launcher for the Home gesture",
                value = "Choose",
                onClick = onSetHome,
            )
        }
        Spacer(Modifier.height(GlazeMetrics.space2))
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
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
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
    val drawerSurfaceColor = if (glass) {
        MaterialTheme.colorScheme.background.copy(alpha = 0.86f)
    } else {
        MaterialTheme.colorScheme.background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GlazeAtmosphere.canvasBlack.copy(alpha = if (glass) 0.10f else 0.02f),
                        GlazeAtmosphere.canvasBlack.copy(alpha = if (glass) 0.32f else 0.12f),
                    ),
                ),
            ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = drawerSurfaceColor,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                    ) {
                        Text(
                            "Apps",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (experiencePreferences.showDrawerAppCount) {
                            Text(
                                filteredApps.size.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1)) {
                        GlazeRoundAction("⚙", onOpenSettings)
                        GlazeRoundAction("⌄", onHome)
                    }
                }

                Spacer(Modifier.height(GlazeMetrics.space2))
                if (experiencePreferences.drawerSearchPosition == LauncherDrawerSearchPosition.TOP) {
                    GlazeAppSearchField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(GlazeMetrics.space3))
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (filteredApps.isEmpty() && query.isNotBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "No installed apps match “" + query.trim() + "”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        when (drawerLayoutMode) {
                            LauncherDrawerLayoutMode.GRID -> LazyVerticalGrid(
                                columns = GridCells.Fixed(preferences.drawerColumns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = GlazeMetrics.space4),
                                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                                verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                            ) {
                                items(filteredApps, key = { it.workspaceKey() }) { app ->
                                    LauncherAppTile(
                                        app = app,
                                        iconScale = preferences.iconScale,
                                        showLabel = preferences.showLabels,
                                        compact = false,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onManageApp(app) },
                                        modifier = Modifier.height(90.dp),
                                    )
                                }
                            }
                            LauncherDrawerLayoutMode.COMPACT -> LazyVerticalGrid(
                                columns = GridCells.Fixed(preferences.drawerColumns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = GlazeMetrics.space4),
                                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                                verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                            ) {
                                items(filteredApps, key = { it.workspaceKey() }) { app ->
                                    LauncherAppTile(
                                        app = app,
                                        iconScale = preferences.iconScale,
                                        showLabel = preferences.showLabels,
                                        compact = true,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onManageApp(app) },
                                        modifier = Modifier.height(76.dp),
                                    )
                                }
                            }
                            LauncherDrawerLayoutMode.LIST -> LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = GlazeMetrics.space4),
                                verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                            ) {
                                lazyItems(filteredApps, key = { it.workspaceKey() }) { app ->
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
                }

                if (experiencePreferences.drawerSearchPosition == LauncherDrawerSearchPosition.BOTTOM) {
                    Spacer(Modifier.height(GlazeMetrics.space2))
                    GlazeAppSearchField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
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
    onSetHomeLayoutStyle: (LauncherHomeLayoutStyle) -> Unit,
    onSetShowHomeQuickActions: (Boolean) -> Unit,
    onSetShowHomePageIndicator: (Boolean) -> Unit,
    onSetDockStyle: (LauncherDockStyle) -> Unit,
    onSetDrawerBackdrop: (LauncherDrawerBackdrop) -> Unit,
    onSetDrawerSearchPosition: (LauncherDrawerSearchPosition) -> Unit,
    onSetShowDrawerAppCount: (Boolean) -> Unit,
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
                        "Home screen",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Workspace, dock, apps and Glaze",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GlazeTextAction("Done", onBack)
            }

            SettingsSection("Layout", "Choose the character of the Home screen") {
                Text(
                    "Home style",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Classic", "Spacious", "Minimal"),
                    selected = when (experiencePreferences.homeLayoutStyle) {
                        LauncherHomeLayoutStyle.CLASSIC -> "Classic"
                        LauncherHomeLayoutStyle.SPACIOUS -> "Spacious"
                        LauncherHomeLayoutStyle.MINIMAL -> "Minimal"
                    },
                    onChoice = {
                        onSetHomeLayoutStyle(
                            when (it) {
                                "Spacious" -> LauncherHomeLayoutStyle.SPACIOUS
                                "Minimal" -> LauncherHomeLayoutStyle.MINIMAL
                                else -> LauncherHomeLayoutStyle.CLASSIC
                            },
                        )
                    },
                )

                Text(
                    "Clock",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Large", "Compact", "Off"),
                    selected = when (experiencePreferences.homeCardStyle) {
                        LauncherHomeCardStyle.CLOCK -> "Large"
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
                    "Grid",
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
                SettingSwitch(
                    "Show page indicator",
                    experiencePreferences.showHomePageIndicator,
                    onSetShowHomePageIndicator,
                )
                SettingSwitch(
                    "Show quick actions",
                    experiencePreferences.showHomeQuickActions,
                    onSetShowHomeQuickActions,
                )
                SettingSwitch("Lock layout", preferences.layoutLocked, onSetLayoutLocked)
            }

            SettingsSection("Dock", "Pinned apps at the bottom of Home") {
                ChoiceRow(
                    choices = listOf("Clear", "Glass", "Edge"),
                    selected = when (experiencePreferences.dockStyle) {
                        LauncherDockStyle.TRANSPARENT -> "Clear"
                        LauncherDockStyle.GLASS -> "Glass"
                        LauncherDockStyle.EDGE -> "Edge"
                    },
                    onChoice = {
                        onSetDockStyle(
                            when (it) {
                                "Glass" -> LauncherDockStyle.GLASS
                                "Edge" -> LauncherDockStyle.EDGE
                                else -> LauncherDockStyle.TRANSPARENT
                            },
                        )
                    },
                )
                Text(
                    "Long-press any app to add or remove it from the dock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsSection("Search", "Keep search available without dominating Home") {
                ChoiceRow(
                    choices = listOf("Swipe down", "Show pill"),
                    selected = if (preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT) {
                        "Show pill"
                    } else {
                        "Swipe down"
                    },
                    onChoice = {
                        onSetIndexHomeMode(
                            if (it == "Show pill") GoreeCloudIndexHomeMode.PERMANENT
                            else GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
                        )
                    },
                )
            }

            SettingsSection("App drawer", "Dense, searchable installed-app view") {
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
                    "Search position",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Top", "Bottom"),
                    selected = if (
                        experiencePreferences.drawerSearchPosition == LauncherDrawerSearchPosition.BOTTOM
                    ) "Bottom" else "Top",
                    onChoice = {
                        onSetDrawerSearchPosition(
                            if (it == "Bottom") LauncherDrawerSearchPosition.BOTTOM
                            else LauncherDrawerSearchPosition.TOP,
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
                    selected = if (experiencePreferences.drawerBackdrop == LauncherDrawerBackdrop.GLASS) {
                        "Glass"
                    } else {
                        "Solid"
                    },
                    onChoice = {
                        onSetDrawerBackdrop(
                            if (it == "Solid") LauncherDrawerBackdrop.SOLID
                            else LauncherDrawerBackdrop.GLASS,
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

            SettingsSection("Icons & appearance", "Scale and theme") {
                Text(
                    "Icon size",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
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
                GlazeSettingsAction(
                    title = "Theme Manager",
                    summary = "System, Light, Dark and Deep Dark",
                    value = themeMode.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                    onClick = onOpenThemeManager,
                )
            }

            SettingsSection("Gestures", "Implemented Home shortcuts") {
                SettingsReadOnlyRow("Swipe up", "Open Apps")
                SettingsReadOnlyRow("Swipe down", "Open GoreeCloud Search")
                SettingsReadOnlyRow("Long-press Home", "Open Home editor")
                SettingsReadOnlyRow("Long-press app", "Favorites and dock")
            }

            if (!isDefaultHome) {
                SettingsSection("System", "Default HOME role") {
                    GlazeSettingsAction(
                        title = "Default Home app",
                        summary = "Use GoreeCloud Launcher for the Home gesture",
                        value = "Set Home",
                        onClick = onRequestHomeRole,
                    )
                }
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
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
) {
    val content: @Composable () -> Unit = {
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
                    modifier = Modifier.size(62.dp),
                )
            }
        }
    }

    when (style) {
        LauncherDockStyle.TRANSPARENT -> content()
        LauncherDockStyle.GLASS -> Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(GlazeMetrics.radius2ExtraLarge),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            content = content,
        )
        LauncherDockStyle.EDGE -> Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = GlazeMetrics.radiusExtraLarge,
                topEnd = GlazeMetrics.radiusExtraLarge,
                bottomStart = 10.dp,
                bottomEnd = 10.dp,
            ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.52f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            content = content,
        )
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
) {
    val icon = rememberLauncherAppIcon(app)
    val base = if (compact) 50f else 52f
    val iconSize = (base * iconScale.coerceIn(0.85f, 1.15f)).dp

    Column(
        modifier = modifier
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
