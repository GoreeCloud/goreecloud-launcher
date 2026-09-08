package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.core.launcher.GoreeCloudIndexHomeMode
import com.goreecloud.launcher.core.launcher.LauncherPreferences
import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.theme.GlazeMetrics
import com.goreecloud.launcher.ui.theme.GlazeThemeMode

enum class LauncherSurfaceMode { HOME, DRAWER, SETTINGS }

@Composable
fun LauncherBetaRoot(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onOpenUniversalSearch: () -> Unit,
    onToggleFavorite: (LauncherActivityInfo) -> Unit,
    onToggleDock: (LauncherActivityInfo) -> Unit,
    onMoveFavorite: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveDock: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    themeMode: GlazeThemeMode,
    onCycleTheme: (GlazeThemeMode) -> Unit,
    onSetHomeGrid: (Int, Int) -> Unit,
    onSetDrawerColumns: (Int) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetIconScale: (Float) -> Unit,
    onSetLayoutLocked: (Boolean) -> Unit,
    onSetIndexHomeMode: (GoreeCloudIndexHomeMode) -> Unit,
    onSurfaceModeChanged: (LauncherSurfaceMode) -> Unit,
) {
    var surfaceModeName by rememberSaveable { mutableStateOf(LauncherSurfaceMode.HOME.name) }
    val surfaceMode = runCatching { LauncherSurfaceMode.valueOf(surfaceModeName) }
        .getOrDefault(LauncherSurfaceMode.HOME)
    var selectedApp by remember { mutableStateOf<LauncherActivityInfo?>(null) }

    LaunchedEffect(surfaceMode) { onSurfaceModeChanged(surfaceMode) }

    when (surfaceMode) {
        LauncherSurfaceMode.HOME -> HomeSurface(
            apps = apps,
            workspace = workspace,
            preferences = preferences,
            isDefaultHome = isDefaultHome,
            onRequestHomeRole = onRequestHomeRole,
            onLaunchApp = onLaunchApp,
            onOpenUniversalSearch = onOpenUniversalSearch,
            onManageApp = { selectedApp = it },
            onOpenDrawer = { surfaceModeName = LauncherSurfaceMode.DRAWER.name },
            onOpenSettings = { surfaceModeName = LauncherSurfaceMode.SETTINGS.name },
        )
        LauncherSurfaceMode.DRAWER -> AppDrawerSurface(
            apps = apps,
            preferences = preferences,
            onLaunchApp = onLaunchApp,
            onManageApp = { selectedApp = it },
            onHome = { surfaceModeName = LauncherSurfaceMode.HOME.name },
            onOpenSettings = { surfaceModeName = LauncherSurfaceMode.SETTINGS.name },
        )
        LauncherSurfaceMode.SETTINGS -> LauncherSettingsSurface(
            preferences = preferences,
            themeMode = themeMode,
            isDefaultHome = isDefaultHome,
            onRequestHomeRole = onRequestHomeRole,
            onSetHomeGrid = onSetHomeGrid,
            onSetDrawerColumns = onSetDrawerColumns,
            onSetShowLabels = onSetShowLabels,
            onSetIconScale = onSetIconScale,
            onSetLayoutLocked = onSetLayoutLocked,
            onSetIndexHomeMode = onSetIndexHomeMode,
            onCycleTheme = onCycleTheme,
            onBack = { surfaceModeName = LauncherSurfaceMode.HOME.name },
        )
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
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onOpenUniversalSearch: () -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val favoriteApps = remember(appsByKey, workspace.favoriteKeys) {
        workspace.favoriteKeys.mapNotNull(appsByKey::get)
    }
    val dockApps = remember(appsByKey, workspace.dockKeys) {
        workspace.dockKeys.mapNotNull(appsByKey::get).take(MAX_DOCK_ITEMS)
    }
    val swipeThreshold = with(LocalDensity.current) { 64.dp.toPx() }

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
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.045f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isDefaultHome) {
                    Surface(
                        onClick = onRequestHomeRole,
                        shape = RoundedCornerShape(GlazeMetrics.radiusPill),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
                        tonalElevation = 2.dp,
                    ) {
                        Text(
                            "Set as Home",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(GlazeMetrics.radiusPill),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                ) {
                    Text(
                        "•••",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            if (preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenUniversalSearch,
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("⌕", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Search GoreeCloud",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (favoriteApps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Swipe up for apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val tileHeight = (maxHeight / preferences.homeRows.toFloat()).coerceAtLeast(72.dp)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(preferences.homeColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(favoriteApps, key = { it.workspaceKey() }) { app ->
                            LauncherAppTile(
                                app = app,
                                iconScale = preferences.iconScale,
                                showLabel = preferences.showLabels,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onManageApp(app) },
                                modifier = Modifier.height(tileHeight),
                            )
                        }
                    }
                }
            }

            if (dockApps.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    tonalElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dockApps.forEach { app ->
                            LauncherAppTile(
                                app = app,
                                iconScale = preferences.iconScale,
                                showLabel = false,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onManageApp(app) },
                                modifier = Modifier.width(68.dp).height(68.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.width(42.dp).height(4.dp),
                    shape = RoundedCornerShape(GlazeMetrics.radiusPill),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                ) {}
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}

@Composable
private fun AppDrawerSurface(
    apps: List<LauncherActivityInfo>,
    preferences: LauncherPreferences,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onManageApp: (LauncherActivityInfo) -> Unit,
    onHome: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredApps = remember(apps, query) {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) apps else apps.filter {
            it.label.toString().lowercase().contains(needle) ||
                it.componentName.packageName.lowercase().contains(needle)
        }
    }
    val dismissThreshold = with(LocalDensity.current) { 64.dp.toPx() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
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
                    modifier = Modifier.width(38.dp).height(4.dp),
                    shape = RoundedCornerShape(GlazeMetrics.radiusPill),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                ) {}
            }
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Apps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Row {
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                    TextButton(onClick = onHome) { Text("Done") }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search apps") },
                shape = RoundedCornerShape(22.dp),
            )
            Spacer(Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(preferences.drawerColumns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredApps, key = { it.workspaceKey() }) { app ->
                    LauncherAppTile(
                        app = app,
                        iconScale = preferences.iconScale,
                        showLabel = preferences.showLabels,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onManageApp(app) },
                        modifier = Modifier.height(96.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LauncherSettingsSurface(
    preferences: LauncherPreferences,
    themeMode: GlazeThemeMode,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onSetHomeGrid: (Int, Int) -> Unit,
    onSetDrawerColumns: (Int) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetIconScale: (Float) -> Unit,
    onSetLayoutLocked: (Boolean) -> Unit,
    onSetIndexHomeMode: (GoreeCloudIndexHomeMode) -> Unit,
    onCycleTheme: (GlazeThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Launcher", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("Done") }
            }

            SettingsCard("Home screen") {
                Text("Grid: ${preferences.homeColumns} × ${preferences.homeRows}")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(4 to 5, 4 to 6, 5 to 6).forEach { option ->
                        OutlinedButton(onClick = { onSetHomeGrid(option.first, option.second) }) {
                            Text("${option.first}×${option.second}")
                        }
                    }
                }
                SettingSwitch("Lock layout", preferences.layoutLocked, onSetLayoutLocked)
            }

            SettingsCard("Apps") {
                Text("Drawer columns: ${preferences.drawerColumns}")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(4, 5, 6).forEach { columns ->
                        OutlinedButton(onClick = { onSetDrawerColumns(columns) }) { Text(columns.toString()) }
                    }
                }
                SettingSwitch("Show app labels", preferences.showLabels, onSetShowLabels)
            }

            SettingsCard("Icon size") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Small" to 0.85f, "Medium" to 1f, "Large" to 1.15f).forEach { (label, value) ->
                        OutlinedButton(onClick = { onSetIconScale(value) }) { Text(label) }
                    }
                }
            }

            SettingsCard("GoreeCloud Search") {
                Text(
                    "Swipe down always opens GoreeCloud Index.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { onSetIndexHomeMode(GoreeCloudIndexHomeMode.PERMANENT) }) {
                        Text("Show pill")
                    }
                    OutlinedButton(onClick = { onSetIndexHomeMode(GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY) }) {
                        Text("Swipe only")
                    }
                }
            }

            SettingsCard("Appearance") {
                Text("Theme: ${themeMode.name.lowercase().replaceFirstChar { it.uppercase() }}")
                FilledTonalButton(onClick = { onCycleTheme(themeMode) }) { Text("Change theme") }
            }

            if (!isDefaultHome) {
                Button(onClick = onRequestHomeRole, modifier = Modifier.fillMaxWidth()) {
                    Text("Set GoreeCloud as default Home")
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherAppTile(
    app: LauncherActivityInfo,
    iconScale: Float,
    showLabel: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier,
) {
    val icon = remember(app.componentName, app.user) {
        runCatching { app.getBadgedIcon(0).toBitmap(128, 128).asImageBitmap() }.getOrNull()
    }
    val iconSize = (54f * iconScale.coerceIn(0.85f, 1.15f)).dp

    Column(
        modifier = modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.label.toString(),
                modifier = Modifier.size(iconSize),
            )
        } else {
            Surface(
                modifier = Modifier.size(iconSize),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(app.label.toString().take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                app.label.toString(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
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
