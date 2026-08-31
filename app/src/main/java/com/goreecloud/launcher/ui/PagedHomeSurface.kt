package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomeSpatialDirection
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspaceRenderedHomePage
import com.goreecloud.launcher.core.workspace.db.context
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
    val pageStripScroll = rememberScrollState()
    val pageActionsScroll = rememberScrollState()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(pageStripScroll),
                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.forEachIndexed { index, page ->
                    val selected = page.pageId == selectedPageId
                    val pageContext = page.context()
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
                        Column(
                            modifier = Modifier.padding(
                                horizontal = GlazeMetrics.space3,
                                vertical = GlazeMetrics.space2,
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Page ${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Text(
                                text = pageContext.compactLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
                TextButton(onClick = onCreatePage) {
                    Text("Add page")
                }
            }

            if (selectedIndex >= 0 && pages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(pageActionsScroll),
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
    pages: List<WorkspaceRenderedHomePage>,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onMoveAppToPage: (LauncherActivityInfo, String) -> Unit,
    onMoveAppWithinPage: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveAppOneCell: (LauncherActivityInfo, WorkspaceHomeSpatialDirection) -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val pageApps = remember(appsByKey, page.appKeys) { page.appKeys.mapNotNull(appsByKey::get) }
    val targetPages = remember(pages, page.pageId) { pages.filterNot { it.pageId == page.pageId } }

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
                "This secondary page is rendered from terminal Room authority. Apps can launch, move to another authoritative Home page, move to the nearest free cell earlier/later, or request an exact one-cell move. Exact-cell requests fail closed when the target is occupied or outside the authoritative grid.",
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
                        "This authoritative Home page is empty. It can be removed from the page switcher or receive an app moved from another secondary page.",
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
                        PagedAppTile(
                            app = app,
                            targetPages = targetPages,
                            onLaunchApp = onLaunchApp,
                            onMoveAppToPage = onMoveAppToPage,
                            onMoveAppWithinPage = onMoveAppWithinPage,
                            onMoveAppOneCell = onMoveAppOneCell,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PagedAppTile(
    app: LauncherActivityInfo,
    targetPages: List<WorkspaceRenderedHomePage>,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onMoveAppToPage: (LauncherActivityInfo, String) -> Unit,
    onMoveAppWithinPage: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveAppOneCell: (LauncherActivityInfo, WorkspaceHomeSpatialDirection) -> Unit,
) {
    val icon = remember(app) { app.getBadgedIcon(0).toBitmap(width = 96, height = 96).asImageBitmap() }
    var movePageMenuExpanded by remember(app) { mutableStateOf(false) }
    var moveCellMenuExpanded by remember(app) { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(vertical = GlazeMetrics.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.clickable { onLaunchApp(app) },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.height(52.dp))
            Spacer(Modifier.height(GlazeMetrics.space2))
            Text(
                text = app.label.toString(),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1)) {
            TextButton(
                onClick = { onMoveAppWithinPage(app, WorkspaceMoveDirection.EARLIER) },
                modifier = Modifier.defaultMinSize(minHeight = GlazeMetrics.comfortableTarget),
            ) { Text("Earlier") }
            TextButton(
                onClick = { onMoveAppWithinPage(app, WorkspaceMoveDirection.LATER) },
                modifier = Modifier.defaultMinSize(minHeight = GlazeMetrics.comfortableTarget),
            ) { Text("Later") }
        }
        Box {
            TextButton(
                onClick = { moveCellMenuExpanded = true },
                modifier = Modifier.defaultMinSize(minHeight = GlazeMetrics.comfortableTarget),
            ) { Text("Move cell") }
            DropdownMenu(expanded = moveCellMenuExpanded, onDismissRequest = { moveCellMenuExpanded = false }) {
                WorkspaceHomeSpatialDirection.entries.forEach { direction ->
                    DropdownMenuItem(
                        text = { Text(direction.displayLabel()) },
                        onClick = {
                            moveCellMenuExpanded = false
                            onMoveAppOneCell(app, direction)
                        },
                    )
                }
            }
        }
        if (targetPages.isNotEmpty()) {
            Box {
                TextButton(
                    onClick = { movePageMenuExpanded = true },
                    modifier = Modifier.defaultMinSize(minHeight = GlazeMetrics.comfortableTarget),
                ) { Text("Move page") }
                DropdownMenu(expanded = movePageMenuExpanded, onDismissRequest = { movePageMenuExpanded = false }) {
                    targetPages.forEach { target ->
                        DropdownMenuItem(
                            text = { Text("Move to Page ${target.rank + 1}") },
                            onClick = {
                                movePageMenuExpanded = false
                                onMoveAppToPage(app, target.pageId)
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun WorkspaceHomeSpatialDirection.displayLabel(): String = when (this) {
    WorkspaceHomeSpatialDirection.LEFT -> "Move left one cell"
    WorkspaceHomeSpatialDirection.RIGHT -> "Move right one cell"
    WorkspaceHomeSpatialDirection.UP -> "Move up one cell"
    WorkspaceHomeSpatialDirection.DOWN -> "Move down one cell"
}
