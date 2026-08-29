package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspaceRenderedHomePage
import com.goreecloud.launcher.core.workspace.workspaceKey
import com.goreecloud.launcher.ui.theme.GlazeMetrics

@Composable
fun HomePageSwitcher(
    pages: List<WorkspaceRenderedHomePage>,
    selectedPageId: String,
    onSelectPage: (String) -> Unit,
    onMovePage: (String, Int) -> Unit,
    onCreatePage: () -> Unit,
    onDeletePage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pages.isEmpty()) return
    val selectedIndex = pages.indexOfFirst { it.pageId == selectedPageId }
    val selectedPage = pages.getOrNull(selectedIndex)
    val canDeleteSelectedPage = pages.size > 1 &&
        selectedPage != null &&
        selectedPage.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID &&
        selectedPage.appKeys.isEmpty() &&
        selectedPage.unsupportedItemCount == 0

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GlazeMetrics.radiusControl),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GlazeMetrics.space2, vertical = GlazeMetrics.space1),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.forEachIndexed { index, page ->
                    val selected = page.pageId == selectedPageId
                    Surface(
                        modifier = Modifier
                            .clickable { onSelectPage(page.pageId) }
                            .padding(1.dp),
                        shape = RoundedCornerShape(GlazeMetrics.radiusControl),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ) {
                        Text(
                            text = "Page ${index + 1}",
                            modifier = Modifier.padding(
                                horizontal = GlazeMetrics.space3,
                                vertical = GlazeMetrics.space2,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                TextButton(onClick = onCreatePage) {
                    Text("Add page")
                }
            }

            if (selectedIndex >= 0 && pages.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { onMovePage(selectedPageId, selectedIndex - 1) },
                        enabled = selectedIndex > 0,
                    ) {
                        Text("Move earlier")
                    }
                    TextButton(
                        onClick = { onMovePage(selectedPageId, selectedIndex + 1) },
                        enabled = selectedIndex < pages.lastIndex,
                    ) {
                        Text("Move later")
                    }
                    if (canDeleteSelectedPage) {
                        TextButton(onClick = { onDeletePage(selectedPageId) }) {
                            Text("Delete empty page")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadOnlyPagedHomeSurface(
    apps: List<LauncherActivityInfo>,
    page: WorkspaceRenderedHomePage,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val pageApps = remember(appsByKey, page.appKeys) {
        page.appKeys.mapNotNull(appsByKey::get)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = GlazeMetrics.space5, vertical = GlazeMetrics.space4),
        ) {
            Spacer(Modifier.height(GlazeMetrics.space8))
            Text(
                "Workspace page",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Room page ${page.rank + 1}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(GlazeMetrics.space2))
            Text(
                "This secondary page is rendered from terminal Room authority. Launching is enabled; placement editing remains read-only. Empty non-primary pages can be removed through the page switcher only after Room revalidates their emptiness.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (page.unsupportedItemCount > 0) {
                Spacer(Modifier.height(GlazeMetrics.space4))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                ) {
                    Text(
                        "${page.unsupportedItemCount} workspace item${if (page.unsupportedItemCount == 1) " is" else "s are"} not rendered yet. Folder, shortcut, and widget presentation remain separate milestones.",
                        modifier = Modifier.padding(GlazeMetrics.space4),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(GlazeMetrics.space5))
            if (pageApps.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "This authoritative Home page is empty. It can be removed from the page switcher; item placement editing remains a separate milestone.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space5),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
                ) {
                    items(pageApps, key = { it.workspaceKey() }) { app ->
                        ReadOnlyPagedAppTile(app = app, onLaunchApp = onLaunchApp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyPagedAppTile(
    app: LauncherActivityInfo,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
) {
    val icon = remember(app) {
        app.getBadgedIcon(0).toBitmap(width = 96, height = 96).asImageBitmap()
    }
    Column(
        modifier = Modifier
            .clickable { onLaunchApp(app) }
            .padding(vertical = GlazeMetrics.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier.height(52.dp),
        )
        Spacer(Modifier.height(GlazeMetrics.space2))
        Text(
            text = app.label.toString(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}
