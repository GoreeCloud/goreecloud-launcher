package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.core.workspace.WorkspaceState
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.theme.GlazeThemeMode

@Composable
fun LauncherRoot(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onToggleFavorite: (LauncherActivityInfo) -> Unit,
    onToggleDock: (LauncherActivityInfo) -> Unit,
    themeMode: GlazeThemeMode,
    onCycleTheme: (GlazeThemeMode) -> Unit,
) {
    var drawerOpen by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(drawerOpen, label = "launcher_surface") { drawer ->
            if (drawer) {
                AppDrawer(
                    apps = apps,
                    workspace = workspace,
                    onLaunchApp = onLaunchApp,
                    onToggleFavorite = onToggleFavorite,
                    onToggleDock = onToggleDock,
                    onClose = { drawerOpen = false },
                )
            } else {
                HomeSurface(
                    apps = apps,
                    workspace = workspace,
                    isDefaultHome = isDefaultHome,
                    onRequestHomeRole = onRequestHomeRole,
                    onOpenDrawer = { drawerOpen = true },
                    onLaunchApp = onLaunchApp,
                    themeMode = themeMode,
                    onCycleTheme = onCycleTheme,
                )
            }
        }
    }
}

@Composable
private fun HomeSurface(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onOpenDrawer: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    themeMode: GlazeThemeMode,
    onCycleTheme: (GlazeThemeMode) -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val favoriteApps = remember(appsByKey, workspace.favoriteKeys) {
        workspace.favoriteKeys.mapNotNull(appsByKey::get)
    }
    val dockApps = remember(appsByKey, workspace.dockKeys) {
        workspace.dockKeys.mapNotNull(appsByKey::get)
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text("GoreeCloud", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Glaze Home", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
                    .clickable { onCycleTheme(themeMode) }.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(24.dp))
        if (!isDefaultHome) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Make this your Home app", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Android keeps this choice under your control. GoreeCloud Launcher does not force itself as the default.")
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onRequestHomeRole) { Text("Choose default launcher") }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Text("Favorites", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        if (favoriteApps.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No favorites yet. Long-press an app in All apps to add one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(favoriteApps, key = { it.workspaceKey() }) { app ->
                    AppTile(app = app, onClick = { onLaunchApp(app) })
                }
            }
        }

        if (dockApps.isNotEmpty()) {
            DockStrip(apps = dockApps, onLaunchApp = onLaunchApp)
            Spacer(Modifier.height(12.dp))
        }

        Card(
            Modifier.fillMaxWidth().clickable(onClick = onOpenDrawer),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                "Search apps  •  Open app drawer",
                Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 20.dp),
                textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun DockStrip(
    apps: List<LauncherActivityInfo>,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            apps.take(5).forEach { app ->
                DockTile(app = app, onClick = { onLaunchApp(app) })
            }
        }
    }
}

@Composable
private fun DockTile(app: LauncherActivityInfo, onClick: () -> Unit) {
    val icon = rememberAppIcon(app, size = 96)
    Column(
        modifier = Modifier.width(58.dp).clickable(onClick = onClick).padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(48.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) Image(icon, app.label.toString(), Modifier.size(42.dp))
            else Text(app.label.toString().take(1).uppercase(), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            app.label.toString(),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppDrawer(
    apps: List<LauncherActivityInfo>,
    workspace: WorkspaceState,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onToggleFavorite: (LauncherActivityInfo) -> Unit,
    onToggleDock: (LauncherActivityInfo) -> Unit,
    onClose: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<LauncherActivityInfo?>(null) }
    val favoriteKeys = remember(workspace.favoriteKeys) { workspace.favoriteKeys.toSet() }
    val dockKeys = remember(workspace.dockKeys) { workspace.dockKeys.toSet() }
    val filteredApps = remember(apps, query) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) apps
        else apps.filter { app ->
            app.label.toString().lowercase().contains(normalizedQuery) ||
                app.componentName.packageName.lowercase().contains(normalizedQuery)
        }
    }

    selectedApp?.let { app ->
        val key = app.workspaceKey()
        val isFavorite = key in favoriteKeys
        val isDocked = key in dockKeys
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text(app.label.toString()) },
            text = { Text("Choose where this app appears. These choices are stored locally on this device.") },
            confirmButton = {
                TextButton(onClick = { onToggleFavorite(app) }) {
                    Text(if (isFavorite) "Remove favorite" else "Add favorite")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { onToggleDock(app) }) {
                        Text(if (isDocked) "Remove dock" else "Add to dock")
                    }
                    TextButton(onClick = { selectedApp = null }) { Text("Close") }
                }
            },
        )
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text("All apps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Long-press an app to manage Home and Dock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Home", Modifier.clickable(onClick = onClose).padding(12.dp), color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search apps") },
            placeholder = { Text("Name or package") },
            shape = RoundedCornerShape(24.dp),
        )
        Spacer(Modifier.height(14.dp))
        if (filteredApps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(76.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredApps, key = { it.workspaceKey() }) { app ->
                    AppTile(
                        app = app,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { selectedApp = app },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: LauncherActivityInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val icon = rememberAppIcon(app, size = 128)
    val interactionModifier = if (onLongClick != null) {
        Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Column(
        modifier.then(interactionModifier).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(62.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) Image(icon, app.label.toString(), Modifier.size(52.dp))
            else Text(app.label.toString().take(1).uppercase(), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Text(app.label.toString(), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun rememberAppIcon(app: LauncherActivityInfo, size: Int): ImageBitmap? =
    remember(app.componentName, app.user, size) {
        runCatching { app.getIcon(0).toBitmap(size, size).asImageBitmap() }.getOrNull()
    }
