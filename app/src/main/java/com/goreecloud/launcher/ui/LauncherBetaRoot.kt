package com.goreecloud.launcher.ui

import android.app.WallpaperManager
import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.core.launcher.LauncherPreferences
import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.theme.GlazeMetrics
import com.goreecloud.launcher.ui.theme.GlazeThemeMode

enum class LauncherSurfaceMode {
    HOME,
    DRAWER,
    SETTINGS,
}

@Composable
fun LauncherBetaRoot(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    preferences: LauncherPreferences,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
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
    onSurfaceModeChanged: (LauncherSurfaceMode) -> Unit,
) {
    var surfaceModeName by rememberSaveable { mutableStateOf(LauncherSurfaceMode.HOME.name) }
    val surfaceMode = runCatching { LauncherSurfaceMode.valueOf(surfaceModeName) }
        .getOrDefault(LauncherSurfaceMode.HOME)
    var selectedApp by remember { mutableStateOf<LauncherActivityInfo?>(null) }

    LaunchedEffect(surfaceMode) {
        onSurfaceModeChanged(surfaceMode)
    }

    when (surfaceMode) {
        LauncherSurfaceMode.HOME -> HomeSurface(
            apps = apps,
            workspace = workspace,
            preferences = preferences,
            isDefaultHome = isDefaultHome,
            onRequestHomeRole = onRequestHomeRole,
            onLaunchApp = onLaunchApp,
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
            onCycleTheme = onCycleTheme,
            onBack = { surfaceModeName = LauncherSurfaceMode.HOME.name },
        )
    }

    selectedApp?.let { app ->
        AppPlacementDialog(
            app = app,
            workspace = workspace,
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
    val wallpaper = rememberWallpaper()

    Box(Modifier.fillMaxSize()) {
        if (wallpaper != null) {
            Image(
                bitmap = wallpaper,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }
        Box(
            Modifier.fillMaxSize().background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.26f)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GlazeMetrics.space4),
        ) {
            // The authoritative page selector is drawn above Home by MainActivity.
            Spacer(Modifier.height(72.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(GlazeMetrics.radiusControl),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                ) {
                    TextButton(onClick = onOpenSettings) {
                        Text("Launcher settings")
                    }
                }
            }

            if (!isDefaultHome) {
                Spacer(Modifier.height(GlazeMetrics.space3))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(GlazeMetrics.space4),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Use GoreeCloud as Home", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Set it as your default launcher to test the full Home experience.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Spacer(Modifier.width(GlazeMetrics.space3))
                        Button(onClick = onRequestHomeRole) { Text("Set default") }
                    }
                }
            }

            Spacer(Modifier.height(GlazeMetrics.space3))

            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                if (favoriteApps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                        ) {
                            Text(
                                "Your Home is empty. Open Apps, long-press an app, and add it to Home.",
                                modifier = Modifier.padding(GlazeMetrics.space5),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else {
                    val preferredTileHeight = maxHeight / preferences.homeRows.toFloat()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(preferences.homeColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = GlazeMetrics.space2),
                        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                    ) {
                        items(favoriteApps, key = { it.workspaceKey() }) { app ->
                            LauncherAppTile(
                                app = app,
                                iconScale = preferences.iconScale,
                                showLabel = preferences.showLabels,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onManageApp(app) },
                                modifier = Modifier.height(preferredTileHeight.coerceAtLeast(76.dp)),
                                transparent = true,
                            )
                        }
                    }
                }
            }

            if (dockApps.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GlazeMetrics.radius2ExtraLarge),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(
                            horizontal = GlazeMetrics.space2,
                            vertical = GlazeMetrics.space2,
                        ),
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
                                modifier = Modifier.width(68.dp).height(72.dp),
                                transparent = true,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(GlazeMetrics.space2))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(GlazeMetrics.radiusControl),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                tonalElevation = 2.dp,
                onClick = onOpenDrawer,
            ) {
                Text(
                    "Apps",
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = GlazeMetrics.comfortableTarget)
                        .padding(vertical = GlazeMetrics.space3),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(GlazeMetrics.space2))
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
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) apps
        else apps.filter { app ->
            app.label.toString().lowercase().contains(normalized) ||
                app.componentName.packageName.lowercase().contains(normalized)
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Apps", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${apps.size} installed app${if (apps.size == 1) "" else "s"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1)) {
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                    TextButton(onClick = onHome) { Text("Home") }
                }
            }

            Spacer(Modifier.height(GlazeMetrics.space3))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search apps") },
                placeholder = { Text("Name or package") },
                shape = RoundedCornerShape(GlazeMetrics.radiusControl),
            )
            Spacer(Modifier.height(GlazeMetrics.space3))

            if (filteredApps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(preferences.drawerColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = GlazeMetrics.space6),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    items(filteredApps, key = { it.workspaceKey() }) { app ->
                        LauncherAppTile(
                            app = app,
                            iconScale = preferences.iconScale,
                            showLabel = preferences.showLabels,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onManageApp(app) },
                            modifier = Modifier.height(104.dp),
                            transparent = false,
                        )
                    }
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
                .padding(horizontal = GlazeMetrics.space5, vertical = GlazeMetrics.space4),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space4),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Launcher settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Home, Apps and appearance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onBack) { Text("Done") }
            }

            SettingsCard(title = "Home screen") {
                Text("Grid", fontWeight = FontWeight.SemiBold)
                Text(
                    "${preferences.homeColumns} × ${preferences.homeRows}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(GlazeMetrics.space2))
                GridPresetRow(
                    options = listOf(4 to 5, 4 to 6, 5 to 5, 5 to 6),
                    selected = preferences.homeColumns to preferences.homeRows,
                    onSelect = { (columns, rows) -> onSetHomeGrid(columns, rows) },
                )
                GridPresetRow(
                    options = listOf(6 to 5, 6 to 6, 6 to 7),
                    selected = preferences.homeColumns to preferences.homeRows,
                    onSelect = { (columns, rows) -> onSetHomeGrid(columns, rows) },
                )
            }

            SettingsCard(title = "Apps screen") {
                Text("Columns", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(GlazeMetrics.space2))
                ChoiceRow(
                    labels = listOf("4", "5", "6"),
                    selectedIndex = (preferences.drawerColumns - 4).coerceIn(0, 2),
                    onSelected = { onSetDrawerColumns(it + 4) },
                )
            }

            SettingsCard(title = "Icons and labels") {
                Text("Icon size", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(GlazeMetrics.space2))
                ChoiceRow(
                    labels = listOf("Small", "Medium", "Large"),
                    selectedIndex = when {
                        preferences.iconScale < 0.95f -> 0
                        preferences.iconScale > 1.05f -> 2
                        else -> 1
                    },
                    onSelected = { index ->
                        onSetIconScale(listOf(0.85f, 1.0f, 1.15f)[index])
                    },
                )
                Spacer(Modifier.height(GlazeMetrics.space3))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("App labels", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Show app names under icons",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = preferences.showLabels,
                        onCheckedChange = onSetShowLabels,
                    )
                }
            }

            SettingsCard(title = "Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Theme", fontWeight = FontWeight.SemiBold)
                        Text(
                            themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FilledTonalButton(onClick = { onCycleTheme(themeMode) }) {
                        Text("Change")
                    }
                }
            }

            SettingsCard(title = "Default Home app") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isDefaultHome) "GoreeCloud Launcher is your current Home app."
                        else "GoreeCloud Launcher is not your current Home app.",
                        modifier = Modifier.weight(1f),
                    )
                    if (!isDefaultHome) {
                        Spacer(Modifier.width(GlazeMetrics.space3))
                        Button(onClick = onRequestHomeRole) { Text("Set default") }
                    }
                }
            }
            Spacer(Modifier.height(GlazeMetrics.space4))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(GlazeMetrics.space4),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(Modifier.padding(vertical = GlazeMetrics.space2))
            content()
        }
    }
}

@Composable
private fun GridPresetRow(
    options: List<Pair<Int, Int>>,
    selected: Pair<Int, Int>,
    onSelect: (Pair<Int, Int>) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
    ) {
        options.forEach { option ->
            if (option == selected) {
                FilledTonalButton(
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f),
                ) { Text("${option.first}×${option.second}") }
            } else {
                OutlinedButton(
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f),
                ) { Text("${option.first}×${option.second}") }
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
    ) {
        labels.forEachIndexed { index, label ->
            if (index == selectedIndex) {
                FilledTonalButton(
                    onClick = { onSelected(index) },
                    modifier = Modifier.weight(1f),
                ) { Text(label) }
            } else {
                OutlinedButton(
                    onClick = { onSelected(index) },
                    modifier = Modifier.weight(1f),
                ) { Text(label) }
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    transparent: Boolean,
) {
    val icon = rememberAppIcon(app, size = 128)
    val iconSize = (54f * iconScale).dp

    Column(
        modifier = modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = GlazeMetrics.space1, vertical = GlazeMetrics.space1),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size((68f * iconScale).dp).then(
                if (transparent) Modifier
                else Modifier.background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                )
            ),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = app.label.toString(),
                    modifier = Modifier.size(iconSize),
                )
            } else {
                Text(
                    app.label.toString().take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(GlazeMetrics.space1))
            Surface(
                color = if (transparent) MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                shape = RoundedCornerShape(GlazeMetrics.radiusControl),
            ) {
                Text(
                    app.label.toString(),
                    modifier = Modifier.padding(horizontal = GlazeMetrics.space1),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AppPlacementDialog(
    app: LauncherActivityInfo,
    workspace: WorkspaceState,
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
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        title = { Text(app.label.toString()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3)) {
                Text(
                    "Choose where this app appears. Long-press Home icons anytime to return here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PlacementSection(
                    title = "Home screen",
                    isMember = isFavorite,
                    position = favoriteIndex,
                    count = workspace.favoriteKeys.size,
                    toggleLabel = if (isFavorite) "Remove" else "Add to Home",
                    toggleEnabled = true,
                    onToggle = onToggleFavorite,
                    onMoveEarlier = { onMoveFavorite(WorkspaceMoveDirection.EARLIER) },
                    onMoveLater = { onMoveFavorite(WorkspaceMoveDirection.LATER) },
                )
                PlacementSection(
                    title = "Dock",
                    isMember = isDocked,
                    position = dockIndex,
                    count = workspace.dockKeys.size,
                    toggleLabel = when {
                        isDocked -> "Remove"
                        dockFull -> "Dock full"
                        else -> "Add to Dock"
                    },
                    toggleEnabled = !dockFull,
                    onToggle = onToggleDock,
                    onMoveEarlier = { onMoveDock(WorkspaceMoveDirection.EARLIER) },
                    onMoveLater = { onMoveDock(WorkspaceMoveDirection.LATER) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Done") }
        },
    )
}

@Composable
private fun PlacementSection(
    title: String,
    isMember: Boolean,
    position: Int,
    count: Int,
    toggleLabel: String,
    toggleEnabled: Boolean,
    onToggle: () -> Unit,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.fillMaxWidth().padding(GlazeMetrics.space3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    if (isMember) {
                        Text(
                            "Position ${position + 1} of $count",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                FilledTonalButton(onClick = onToggle, enabled = toggleEnabled) {
                    Text(toggleLabel)
                }
            }
            if (isMember && count > 1) {
                Spacer(Modifier.height(GlazeMetrics.space2))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    OutlinedButton(
                        onClick = onMoveEarlier,
                        enabled = position > 0,
                        modifier = Modifier.weight(1f),
                    ) { Text("Earlier") }
                    OutlinedButton(
                        onClick = onMoveLater,
                        enabled = position < count - 1,
                        modifier = Modifier.weight(1f),
                    ) { Text("Later") }
                }
            }
        }
    }
}

@Composable
private fun rememberAppIcon(app: LauncherActivityInfo, size: Int): ImageBitmap? =
    remember(app.componentName, app.user, size) {
        runCatching { app.getBadgedIcon(0).toBitmap(size, size).asImageBitmap() }.getOrNull()
    }

@Composable
private fun rememberWallpaper(): ImageBitmap? {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val metrics = context.resources.displayMetrics
            WallpaperManager.getInstance(context).drawable
                .toBitmap(
                    width = metrics.widthPixels.coerceAtLeast(1),
                    height = metrics.heightPixels.coerceAtLeast(1),
                )
                .asImageBitmap()
        }.getOrNull()
    }
}
