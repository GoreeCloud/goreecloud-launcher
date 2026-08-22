package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.ui.theme.GlazeThemeMode

@Composable
fun LauncherRoot(
    apps: List<LauncherActivityInfo>,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    themeMode: GlazeThemeMode,
    onCycleTheme: (GlazeThemeMode) -> Unit,
) {
    var drawerOpen by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(drawerOpen, label = "launcher_surface") { drawer ->
            if (drawer) AppDrawer(apps, onLaunchApp) { drawerOpen = false }
            else HomeSurface(
                apps, isDefaultHome, onRequestHomeRole,
                { drawerOpen = true }, onLaunchApp, themeMode, onCycleTheme
            )
        }
    }
}

@Composable
private fun HomeSurface(
    apps: List<LauncherActivityInfo>,
    isDefaultHome: Boolean,
    onRequestHomeRole: () -> Unit,
    onOpenDrawer: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    themeMode: GlazeThemeMode,
    onCycleTheme: (GlazeThemeMode) -> Unit,
) {
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(apps.take(12), key = { "${it.user.hashCode()}:${it.componentName.flattenToString()}" }) {
                AppTile(it) { onLaunchApp(it) }
            }
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
private fun AppDrawer(
    apps: List<LauncherActivityInfo>,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onClose: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredApps = remember(apps, query) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) apps
        else apps.filter { app ->
            app.label.toString().lowercase().contains(normalizedQuery) ||
                app.componentName.packageName.lowercase().contains(normalizedQuery)
        }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("All apps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                items(filteredApps, key = { "${it.user.hashCode()}:${it.componentName.flattenToString()}" }) {
                    AppTile(it) { onLaunchApp(it) }
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: LauncherActivityInfo, onClick: () -> Unit) {
    val icon = remember(app.componentName, app.user) {
        runCatching { app.getIcon(0).toBitmap(128, 128).asImageBitmap() }.getOrNull()
    }
    Column(
        Modifier.clickable(onClick = onClick).padding(4.dp),
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
