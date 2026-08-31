package com.goreecloud.launcher.ui

import android.app.WallpaperManager
import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val primaryIndex = pages.indexOfFirst { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
    val primaryRankHealthy = primaryIndex == 0
    val pageListState = rememberLazyListState()
    var pageMenuExpanded by remember(selectedPageId) { mutableStateOf(false) }

    val canDeleteSelectedPage = pages.size > 1 &&
        selectedPage != null &&
        selectedPage.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID &&
        selectedPage.appKeys.isEmpty() &&
        selectedPage.unsupportedItemCount == 0
    val canMoveSelectedEarlier = primaryRankHealthy &&
        selectedPage != null &&
        selectedPage.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID &&
        selectedIndex > 1
    val canMoveSelectedLater = primaryRankHealthy &&
        selectedPage != null &&
        selectedPage.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID &&
        selectedIndex >= 1 &&
        selectedIndex < pages.lastIndex
    val hasPageActions = canMoveSelectedEarlier || canMoveSelectedLater || canDeleteSelectedPage

    LaunchedEffect(selectedPageId, selectedIndex, pages.size) {
        if (selectedIndex >= 0) pageListState.animateScrollToItem(selectedIndex)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlazeMetrics.radiusControl),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GlazeMetrics.space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                state = pageListState,
                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(
                    items = pages,
                    key = { _, page -> page.pageId },
                ) { index, page ->
                    val selected = page.pageId == selectedPageId
                    val pageContext = page.context()
                    Surface(
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {
                                contentDescription = pageContext.accessibilityLabel(index + 1, selected)
                            }
                            .clickable { onSelectPage(page.pageId) },
                        shape = RoundedCornerShape(GlazeMetrics.radiusControl),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                        },
                    ) {
                        Text(
                            text = if (selected) "Page ${index + 1}" else "${index + 1}",
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
            }

            TextButton(onClick = onCreatePage) { Text("Add") }

            if (hasPageActions) {
                Box {
                    TextButton(onClick = { pageMenuExpanded = true }) { Text("More") }
                    DropdownMenu(
                        expanded = pageMenuExpanded,
                        onDismissRequest = { pageMenuExpanded = false },
                    ) {
                        if (canMoveSelectedEarlier) {
                            DropdownMenuItem(
                                text = { Text("Move page earlier") },
                                onClick = {
                                    pageMenuExpanded = false
                                    onMovePage(selectedPageId, selectedIndex - 1)
                                },
                            )
                        }
                        if (canMoveSelectedLater) {
                            DropdownMenuItem(
                                text = { Text("Move page later") },
                                onClick = {
                                    pageMenuExpanded = false
                                    onMovePage(selectedPageId, selectedIndex + 1)
                                },
                            )
                        }
                        if (canDeleteSelectedPage) {
                            DropdownMenuItem(
                                text = { Text("Delete empty page") },
                                onClick = {
                                    pageMenuExpanded = false
                                    onDeletePage(selectedPageId)
                                },
                            )
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
    homeColumns: Int,
    showLabels: Boolean,
    iconScale: Float,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onMoveAppToPage: (LauncherActivityInfo, String) -> Unit,
    onMoveAppWithinPage: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveAppOneCell: (LauncherActivityInfo, WorkspaceHomeSpatialDirection) -> Unit,
) {
    val appsByKey = remember(apps) { apps.associateBy { it.workspaceKey() } }
    val pageApps = remember(appsByKey, page.appKeys) { page.appKeys.mapNotNull(appsByKey::get) }
    val targetPages = remember(pages, page.pageId) {
        pages.filterNot {
            it.pageId == page.pageId || it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        }
    }
    val wallpaper = rememberPagedWallpaper()

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
            Spacer(Modifier.height(72.dp))

            if (page.unsupportedItemCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = GlazeMetrics.space2),
                    shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f)
                    ),
                ) {
                    Text(
                        "${page.unsupportedItemCount} item${if (page.unsupportedItemCount == 1) "" else "s"} on this page still need folder, shortcut, or widget rendering support.",
                        modifier = Modifier.padding(GlazeMetrics.space3),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (pageApps.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                    ) {
                        Text(
                            "This Home page is empty.",
                            modifier = Modifier.padding(GlazeMetrics.space5),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(homeColumns.coerceIn(4, 6)),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = GlazeMetrics.space4),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
                ) {
                    items(pageApps, key = { it.workspaceKey() }) { app ->
                        PagedAppTile(
                            app = app,
                            showLabel = showLabels,
                            iconScale = iconScale,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagedAppTile(
    app: LauncherActivityInfo,
    showLabel: Boolean,
    iconScale: Float,
    targetPages: List<WorkspaceRenderedHomePage>,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onMoveAppToPage: (LauncherActivityInfo, String) -> Unit,
    onMoveAppWithinPage: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveAppOneCell: (LauncherActivityInfo, WorkspaceHomeSpatialDirection) -> Unit,
) {
    val icon = remember(app.componentName, app.user) {
        runCatching { app.getBadgedIcon(0).toBitmap(128, 128).asImageBitmap() }.getOrNull()
    }
    var manageOpen by remember(app.componentName, app.user) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .combinedClickable(
                onClick = { onLaunchApp(app) },
                onLongClick = { manageOpen = true },
            )
            .padding(GlazeMetrics.space1),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.label.toString(),
                modifier = Modifier.size((54f * iconScale.coerceIn(0.85f, 1.15f)).dp),
            )
        } else {
            Surface(
                modifier = Modifier.size((54f * iconScale.coerceIn(0.85f, 1.15f)).dp),
                shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(app.label.toString().take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(GlazeMetrics.space1))
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                shape = RoundedCornerShape(GlazeMetrics.radiusControl),
            ) {
                Text(
                    text = app.label.toString(),
                    modifier = Modifier.padding(horizontal = GlazeMetrics.space1),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    if (manageOpen) {
        PagedAppManagementDialog(
            app = app,
            targetPages = targetPages,
            onMoveAppToPage = onMoveAppToPage,
            onMoveAppWithinPage = onMoveAppWithinPage,
            onMoveAppOneCell = onMoveAppOneCell,
            onClose = { manageOpen = false },
        )
    }
}

@Composable
private fun PagedAppManagementDialog(
    app: LauncherActivityInfo,
    targetPages: List<WorkspaceRenderedHomePage>,
    onMoveAppToPage: (LauncherActivityInfo, String) -> Unit,
    onMoveAppWithinPage: (LauncherActivityInfo, WorkspaceMoveDirection) -> Unit,
    onMoveAppOneCell: (LauncherActivityInfo, WorkspaceHomeSpatialDirection) -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        title = { Text(app.label.toString()) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
            ) {
                Text(
                    "Move this app without exposing editing controls on the normal Home surface.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text("Order on page", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    OutlinedButton(
                        onClick = { onMoveAppWithinPage(app, WorkspaceMoveDirection.EARLIER) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Earlier") }
                    OutlinedButton(
                        onClick = { onMoveAppWithinPage(app, WorkspaceMoveDirection.LATER) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Later") }
                }

                Text("Move one cell", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    OutlinedButton(
                        onClick = { onMoveAppOneCell(app, WorkspaceHomeSpatialDirection.LEFT) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Left") }
                    OutlinedButton(
                        onClick = { onMoveAppOneCell(app, WorkspaceHomeSpatialDirection.RIGHT) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Right") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    OutlinedButton(
                        onClick = { onMoveAppOneCell(app, WorkspaceHomeSpatialDirection.UP) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Up") }
                    OutlinedButton(
                        onClick = { onMoveAppOneCell(app, WorkspaceHomeSpatialDirection.DOWN) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Down") }
                }

                if (targetPages.isNotEmpty()) {
                    Text("Move to another Home page", fontWeight = FontWeight.SemiBold)
                    targetPages.forEach { target ->
                        FilledTonalButton(
                            onClick = {
                                onMoveAppToPage(app, target.pageId)
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(target.context().moveTargetLabel(target.rank + 1))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Done") }
        },
    )
}

@Composable
private fun rememberPagedWallpaper(): ImageBitmap? {
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
