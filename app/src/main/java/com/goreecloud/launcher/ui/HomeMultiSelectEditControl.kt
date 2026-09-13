package com.goreecloud.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goreecloud.launcher.core.workspace.db.WorkspaceHomeMultiSelectPolicy
import com.goreecloud.launcher.core.workspace.db.WorkspaceRenderedHomePage
import com.goreecloud.launcher.ui.theme.GlazeMetrics

/**
 * Explicit, accessible entry point for bounded multi-select editing of secondary HOME apps.
 *
 * The control intentionally lives inside Home overview/edit mode instead of changing normal
 * launcher tap/long-press behavior. Group moves are delegated to the activity/runtime layer, which
 * preserves Room as the only post-cutover workspace mutation authority.
 */
@Composable
fun HomeMultiSelectEditControl(
    page: WorkspaceRenderedHomePage?,
    pages: List<WorkspaceRenderedHomePage>,
    appLabelsByKey: Map<String, String>,
    layoutLocked: Boolean,
    onMoveSelectedApps: (sourcePageId: String, appKeys: List<String>, targetPageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (page == null) return

    val targetPages = remember(pages, page.pageId) {
        WorkspaceHomeMultiSelectPolicy.targetPages(pages, page.pageId)
    }
    val canSelect = WorkspaceHomeMultiSelectPolicy.canSelectApps(
        page = page,
        pages = pages,
        layoutLocked = layoutLocked,
    )
    var selectionOpen by remember(page.pageId) { mutableStateOf(false) }
    var targetOpen by remember(page.pageId) { mutableStateOf(false) }
    var selectedKeys by remember(page.pageId) { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(page.appKeys, layoutLocked, targetPages) {
        selectedKeys = selectedKeys.intersect(page.appKeys.toSet())
        if (layoutLocked || targetPages.isEmpty()) {
            selectionOpen = false
            targetOpen = false
            selectedKeys = emptySet()
        }
    }

    if (!canSelect) return

    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = GlazeMetrics.space4, vertical = GlazeMetrics.space3),
        shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 4.dp,
    ) {
        FilledTonalButton(
            onClick = { selectionOpen = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(GlazeMetrics.space2)
                .semantics { contentDescription = "Select multiple apps on this Home page" },
        ) {
            Text("Select apps on this page")
        }
    }

    if (selectionOpen) {
        AlertDialog(
            onDismissRequest = {
                selectionOpen = false
                selectedKeys = emptySet()
            },
            shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
            title = { Text("Select apps to move") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    Text(
                        "Choose one or more apps from this secondary Home page. Selection order follows the current page order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    page.appKeys.distinct().forEach { appKey ->
                        val checked = appKey in selectedKeys
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedKeys = if (checked) {
                                        selectedKeys - appKey
                                    } else {
                                        selectedKeys + appKey
                                    }
                                }
                                .padding(vertical = GlazeMetrics.space1),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { nowChecked ->
                                    selectedKeys = if (nowChecked) {
                                        selectedKeys + appKey
                                    } else {
                                        selectedKeys - appKey
                                    }
                                },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appLabelsByKey[appKey] ?: "Unavailable app",
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (appLabelsByKey[appKey] == null) {
                                    Text(
                                        text = appKey,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectionOpen = false
                        targetOpen = true
                    },
                    enabled = selectedKeys.isNotEmpty(),
                ) {
                    Text("Choose destination")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectionOpen = false
                        selectedKeys = emptySet()
                    }
                ) { Text("Cancel") }
            },
        )
    }

    if (targetOpen) {
        val orderedSelection = WorkspaceHomeMultiSelectPolicy.orderedSelection(page, selectedKeys)
        val orderedPages = remember(pages) { pages.sortedBy { it.rank } }
        AlertDialog(
            onDismissRequest = { targetOpen = false },
            shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
            title = {
                Text(
                    "Move ${orderedSelection.size} app${if (orderedSelection.size == 1) "" else "s"}"
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    Text(
                        "Choose another secondary Home page. Moves are applied in current page order and any partial result is reported explicitly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    targetPages.forEach { target ->
                        val targetIndex = orderedPages.indexOfFirst { it.pageId == target.pageId }
                        FilledTonalButton(
                            onClick = {
                                if (orderedSelection.isNotEmpty()) {
                                    onMoveSelectedApps(page.pageId, orderedSelection, target.pageId)
                                }
                                targetOpen = false
                                selectedKeys = emptySet()
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            Text(if (targetIndex >= 0) "Page ${targetIndex + 1}" else "Home page")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { targetOpen = false }) { Text("Back") }
            },
        )
    }
}
