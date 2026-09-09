package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import com.goreecloud.launcher.ui.theme.GlazeThemeMode
import kotlin.math.abs

enum class LauncherSurfaceMode { HOME, DRAWER, SETTINGS }

internal enum class HomeVerticalGesture { NONE, OPEN_DRAWER, OPEN_SEARCH }

internal fun classifyHomeVerticalGesture(
    deltaX: Float,
    deltaY: Float,
    threshold: Float,
): HomeVerticalGesture {
    if (threshold <= 0f || abs(deltaY) < threshold || abs(deltaY) <= abs(deltaX)) {
        return HomeVerticalGesture.NONE
    }
    return if (deltaY < 0f) HomeVerticalGesture.OPEN_DRAWER else HomeVerticalGesture.OPEN_SEARCH
}

private fun Modifier.observeHomeVerticalGestures(
    swipeThreshold: Float,
    onOpenDrawer: () -> Unit,
    onOpenUniversalSearch: () -> Unit,
): Modifier = pointerInput(swipeThreshold, onOpenDrawer, onOpenUniversalSearch) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val start = down.position

        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break

            when (
                classifyHomeVerticalGesture(
                    deltaX = change.position.x - start.x,
                    deltaY = change.position.y - start.y,
                    threshold = swipeThreshold,
                )
            ) {
                HomeVerticalGesture.OPEN_DRAWER -> {
                    onOpenDrawer()
                    break
                }
                HomeVerticalGesture.OPEN_SEARCH -> {
                    onOpenUniversalSearch()
                    break
                }
                HomeVerticalGesture.NONE -> Unit
            }
        }
    }
}

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
    homeResetGeneration: Long = 0L,
) {
    var surfaceModeName by rememberSaveable { mutableStateOf(LauncherSurfaceMode.HOME.name) }
    val surfaceMode = runCatching { LauncherSurfaceMode.valueOf(surfaceModeName) }
        .getOrDefault(LauncherSurfaceMode.HOME)
    var selectedApp by remember { mutableStateOf<LauncherActivityInfo?>(null) }

    LaunchedEffect(homeResetGeneration) {
        if (homeResetGeneration > 0L) {
            surfaceModeName = LauncherSurfaceMode.HOME.name
            selectedApp = null
        }
    }
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
            .observeHomeVerticalGestures(
                swipeThreshold = swipeThreshold,
                onOpenDrawer = onOpenDrawer,
                onOpenUniversalSearch = onOpenUniversalSearch,
            ),
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.02f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isDefaultHome) {
                    AssistChip(
                        onClick = onRequestHomeRole,
                        label = { Text("Set as Home") },
                    )
                    Spacer(Modifier.width(8.dp))
                }
                FilledIconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
                    ),
                ) {
                    Text("•••", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (preferences.indexHomeMode == GoreeCloudIndexHomeMode.PERMANENT) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenUniversalSearch,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                ) {
                    Row(
                        modifier = Modifier.height(46.dp).padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("⌕", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(9.dp))
                        Text(
                            "Search GoreeCloud",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (favoriteApps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Swipe up for apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                } else {
                    val tileHeight = (maxHeight / preferences.homeRows.toFloat()).coerceIn(72.dp, 106.dp)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(preferences.homeColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(favoriteApps, key = { it.workspaceKey() }) { app ->
                            LauncherAppTile(
                                app = app,
                                iconScale = preferences.iconScale,
                                showLabel = preferences.showLabels,
                                compact = true,
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
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.54f),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dockApps.forEach { app ->
                            LauncherAppTile(
                                app = app,
                                iconScale = preferences.iconScale,
                                showLabel = false,
                                compact = true,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onManageApp(app) },
                                modifier = Modifier.size(64.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.width(34.dp).height(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f),
                ) {}
            }
            Spacer(Modifier.height(2.dp))
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    modifier = Modifier.width(34.dp).height(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                ) {}
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Apps", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row {
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                    TextButton(onClick = onHome) { Text("Done") }
                }
            }

            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search apps") },
                shape = RoundedCornerShape(18.dp),
            )
            Spacer(Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(preferences.drawerColumns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filteredApps, key = { it.workspaceKey() }) { app ->
                    LauncherAppTile(
                        app = app,
                        iconScale = preferences.iconScale,
                        showLabel = preferences.showLabels,
                        compact = false,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onManageApp(app) },
                        modifier = Modifier.height(92.dp),
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Launcher", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("Done") }
            }

            SettingsSection("Home screen") {
                ChoiceRow(
                    choices = listOf("4×5", "4×6", "5×6"),
                    onChoice = {
                        when (it) {
                            "4×5" -> onSetHomeGrid(4, 5)
                            "4×6" -> onSetHomeGrid(4, 6)
                            else -> onSetHomeGrid(5, 6)
                        }
                    },
                )
                SettingSwitch("Lock layout", preferences.layoutLocked, onSetLayoutLocked)
            }

            SettingsSection("Apps") {
                ChoiceRow(
                    choices = listOf("4", "5", "6"),
                    onChoice = { onSetDrawerColumns(it.toInt()) },
                )
                SettingSwitch("Show app labels", preferences.showLabels, onSetShowLabels)
            }

            SettingsSection("Icon size") {
                ChoiceRow(
                    choices = listOf("Small", "Medium", "Large"),
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

            SettingsSection("GoreeCloud Search") {
                Text(
                    "Swipe down always opens GoreeCloud Index.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                ChoiceRow(
                    choices = listOf("Show pill", "Swipe only"),
                    onChoice = {
                        onSetIndexHomeMode(
                            if (it == "Show pill") GoreeCloudIndexHomeMode.PERMANENT
                            else GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
                        )
                    },
                )
            }

            SettingsSection("Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Theme")
                    TextButton(onClick = { onCycleTheme(themeMode) }) {
                        Text(themeMode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            if (!isDefaultHome) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = onRequestHomeRole, modifier = Modifier.fillMaxWidth()) {
                    Text("Set GoreeCloud as default Home")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        content()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    }
}

@Composable
private fun ChoiceRow(choices: List<String>, onChoice: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEach { label ->
            OutlinedButton(
                onClick = { onChoice(label) },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) { Text(label) }
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
    compact: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier,
) {
    val icon = remember(app.componentName, app.user) {
        runCatching { app.getBadgedIcon(0).toBitmap(144, 144).asImageBitmap() }.getOrNull()
    }
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
