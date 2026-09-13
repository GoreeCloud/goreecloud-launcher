package com.goreecloud.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.goreecloud.launcher.core.workspace.WorkspaceHomeOverviewPolicy
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspaceRenderedHomePage
import com.goreecloud.launcher.ui.theme.GlazeMetrics

@Composable
fun HomeEditEntryControl(
    onOpenOverview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpenOverview,
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { contentDescription = "Edit Home pages" },
        shape = RoundedCornerShape(GlazeMetrics.radiusControl),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GlazeMetrics.space3, vertical = GlazeMetrics.space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
        ) {
            Text("✦", style = MaterialTheme.typography.labelLarge)
            Text("Edit Home", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun HomeOverviewEditSurface(
    pages: List<WorkspaceRenderedHomePage>,
    selectedPageId: String,
    homeColumns: Int,
    homeRows: Int,
    layoutLocked: Boolean,
    onSelectPage: (String) -> Unit,
    onMovePage: (String, Int) -> Unit,
    onCreatePage: () -> Unit,
    onDeletePage: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orderedPages = remember(pages) { pages.sortedBy { it.rank } }
    val primaryRankHealthy = orderedPages.firstOrNull()?.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
    var deleteCandidateId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderedPages, deleteCandidateId) {
        if (deleteCandidateId != null && orderedPages.none { it.pageId == deleteCandidateId }) {
            deleteCandidateId = null
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(GlazeMetrics.space4),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Edit Home",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Select, reorder, add, or safely remove Home pages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("Done") }
            }

            if (layoutLocked) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.86f),
                ) {
                    Text(
                        "Layout is locked. Page selection stays available, but add, reorder, and delete controls are disabled until the layout is unlocked in Launcher settings.",
                        modifier = Modifier.padding(GlazeMetrics.space3),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            if (!primaryRankHealthy && orderedPages.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.90f),
                ) {
                    Text(
                        "Home page ordering is inconsistent. Structural page changes are disabled until the canonical primary page is restored to rank zero.",
                        modifier = Modifier.padding(GlazeMetrics.space3),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            if (orderedPages.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f),
                ) {
                    Text(
                        "Home pages are not available yet.",
                        modifier = Modifier.padding(GlazeMetrics.space4),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
                ) {
                    itemsIndexed(orderedPages, key = { _, page -> page.pageId }) { index, page ->
                        val selected = page.pageId == selectedPageId
                        val isPrimary = page.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
                        val pageIsEmpty = page.appKeys.isEmpty() && page.unsupportedItemCount == 0
                        val actions = WorkspaceHomeOverviewPolicy.pageActions(
                            pageIndex = index,
                            pageCount = orderedPages.size,
                            isPrimaryPage = isPrimary,
                            isCompletelyEmpty = pageIsEmpty,
                            primaryRankHealthy = primaryRankHealthy,
                            layoutLocked = layoutLocked,
                        )

                        HomeOverviewPageCard(
                            page = page,
                            pageNumber = index + 1,
                            selected = selected,
                            isPrimary = isPrimary,
                            homeColumns = homeColumns,
                            homeRows = homeRows,
                            canMoveEarlier = actions.canMoveEarlier,
                            canMoveLater = actions.canMoveLater,
                            canDelete = actions.canDelete,
                            onSelect = { onSelectPage(page.pageId) },
                            onMoveEarlier = { onMovePage(page.pageId, index - 1) },
                            onMoveLater = { onMovePage(page.pageId, index + 1) },
                            onDelete = { deleteCandidateId = page.pageId },
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick = onCreatePage,
                enabled = WorkspaceHomeOverviewPolicy.canCreatePage(
                    pageCount = orderedPages.size,
                    layoutLocked = layoutLocked,
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text("Add Home page")
            }

            Text(
                "Delete is offered only for a non-primary page containing no apps and no unsupported workspace items. Preview cells show item count, not authoritative Room cell coordinates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    deleteCandidateId?.let { pageId ->
        val pageIndex = orderedPages.indexOfFirst { it.pageId == pageId }
        val page = orderedPages.getOrNull(pageIndex)
        val canStillDelete = page != null && WorkspaceHomeOverviewPolicy.pageActions(
            pageIndex = pageIndex,
            pageCount = orderedPages.size,
            isPrimaryPage = page.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID,
            isCompletelyEmpty = page.appKeys.isEmpty() && page.unsupportedItemCount == 0,
            primaryRankHealthy = primaryRankHealthy,
            layoutLocked = layoutLocked,
        ).canDelete

        AlertDialog(
            onDismissRequest = { deleteCandidateId = null },
            shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
            title = { Text("Delete empty Home page?") },
            text = {
                Text(
                    if (canStillDelete) {
                        "This removes Page ${pageIndex + 1}. The page is currently empty."
                    } else {
                        "This page is no longer eligible for deletion."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (canStillDelete) onDeletePage(pageId)
                        deleteCandidateId = null
                    },
                    enabled = canStillDelete,
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateId = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun HomeOverviewPageCard(
    page: WorkspaceRenderedHomePage,
    pageNumber: Int,
    selected: Boolean,
    isPrimary: Boolean,
    homeColumns: Int,
    homeRows: Int,
    canMoveEarlier: Boolean,
    canMoveLater: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit,
    onDelete: () -> Unit,
) {
    val itemCount = page.appKeys.size + page.unsupportedItemCount
    val label = if (isPrimary) "Primary Home" else "Page $pageNumber"

    Surface(
        modifier = Modifier
            .width(228.dp)
            .semantics(mergeDescendants = false) {
                contentDescription = buildString {
                    append(label)
                    append(", ")
                    append(itemCount)
                    append(if (itemCount == 1) " item" else " items")
                    if (selected) append(", selected")
                }
            }
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f)
        },
        tonalElevation = if (selected) 4.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(GlazeMetrics.space3),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, fontWeight = FontWeight.SemiBold)
                if (selected) {
                    Text(
                        "Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            HomeOverviewPagePreview(
                appCount = page.appKeys.size,
                unsupportedItemCount = page.unsupportedItemCount,
                columns = homeColumns,
                rows = homeRows,
            )

            Text(
                buildString {
                    append(page.appKeys.size)
                    append(if (page.appKeys.size == 1) " app" else " apps")
                    if (page.unsupportedItemCount > 0) {
                        append(" • ")
                        append(page.unsupportedItemCount)
                        append(" other")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!isPrimary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                ) {
                    OutlinedButton(
                        onClick = onMoveEarlier,
                        enabled = canMoveEarlier,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) { Text("Earlier") }
                    OutlinedButton(
                        onClick = onMoveLater,
                        enabled = canMoveLater,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) { Text("Later") }
                }

                TextButton(
                    onClick = onDelete,
                    enabled = canDelete,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(if (canDelete) "Delete empty page" else "Delete unavailable")
                }
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun HomeOverviewPagePreview(
    appCount: Int,
    unsupportedItemCount: Int,
    columns: Int,
    rows: Int,
) {
    val safeColumns = columns.coerceIn(4, 6)
    val safeRows = rows.coerceIn(4, 7)
    val capacity = safeColumns * safeRows
    val appCells = appCount.coerceAtMost(capacity)
    val otherCells = unsupportedItemCount.coerceAtMost((capacity - appCells).coerceAtLeast(0))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlazeMetrics.radiusControl),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
    ) {
        Column(
            modifier = Modifier.padding(GlazeMetrics.space2),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(safeRows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(safeColumns) { column ->
                        val cellIndex = row * safeColumns + column
                        val occupiedByApp = cellIndex < appCells
                        val occupiedByOther = cellIndex in appCells until (appCells + otherCells)
                        Surface(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            shape = RoundedCornerShape(4.dp),
                            color = when {
                                occupiedByApp -> MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                                occupiedByOther -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.72f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)
                            },
                        ) {}
                    }
                }
            }
        }
    }
}
