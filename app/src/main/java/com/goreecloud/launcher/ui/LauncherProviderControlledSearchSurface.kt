package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goreecloud.launcher.core.launcher.LaunchApplicationSearchAction
import com.goreecloud.launcher.core.launcher.LauncherFilesSearchProvider
import com.goreecloud.launcher.core.launcher.LauncherLaunchShortcutSearchAction
import com.goreecloud.launcher.core.launcher.LauncherOpenDocumentSearchAction
import com.goreecloud.launcher.core.launcher.LauncherLocalSearchPermissions
import com.goreecloud.launcher.core.launcher.LauncherLocalSearchDiagnostics
import com.goreecloud.launcher.core.launcher.LauncherLocalSearchIssue
import com.goreecloud.launcher.core.launcher.LauncherMessagesSearchProvider
import com.goreecloud.launcher.core.launcher.LauncherOpenUriSearchAction
import com.goreecloud.launcher.core.launcher.LauncherRuntimeSearchProviderRegistry
import com.goreecloud.launcher.core.launcher.LauncherNavigateSearchAction
import com.goreecloud.launcher.core.launcher.LauncherSearchCategory
import com.goreecloud.launcher.core.launcher.LauncherSearchDestination
import com.goreecloud.launcher.core.launcher.LauncherSearchExecutionPolicy
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderControlState
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderPreferenceDecodeResult
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderPreferenceSnapshot
import com.goreecloud.launcher.core.launcher.LauncherSearchPresentationPolicy
import com.goreecloud.launcher.core.launcher.LauncherSearchProviderUserControlPolicy
import com.goreecloud.launcher.core.launcher.LauncherSearchResult
import com.goreecloud.launcher.core.launcher.LauncherUniversalSearch
import com.goreecloud.launcher.ui.theme.GlazeMetrics

@Composable
internal fun LauncherProviderControlledSearchSurface(
    apps: List<LauncherActivityInfo>,
    searchProviderPreferences: LauncherSearchProviderPreferenceDecodeResult?,
    fileSearchRoots: List<Uri>,
    onSetSearchProviderPreferences: (LauncherSearchProviderPreferenceSnapshot) -> Unit,
    onSetSearchProviderEnabled: (LauncherSearchProviderControlState, String, Boolean) -> Unit,
    onChooseFileSearchRoot: () -> Unit,
    onRemoveFileSearchRoot: (Uri) -> Unit,
    onResetSearchProviderPreferences: () -> Unit,
    onLaunchApp: (LauncherActivityInfo) -> Unit,
    onLaunchShortcut: (LauncherLaunchShortcutSearchAction) -> Unit,
    onOpenSearchUri: (LauncherOpenUriSearchAction) -> Unit,
    onOpenDocument: (LauncherOpenDocumentSearchAction) -> Unit,
    onSearchWithConnectedProvider: (String, String) -> Unit,
    onNavigate: (LauncherSearchDestination) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var showSources by rememberSaveable { mutableStateOf(false) }
    val catalog = remember(apps, context, fileSearchRoots) {
        LauncherRuntimeSearchProviderRegistry.catalog(context, apps, fileSearchRoots)
    }
    val controls = remember(catalog, searchProviderPreferences) {
        searchProviderPreferences?.let {
            LauncherSearchProviderUserControlPolicy.normalize(catalog, it)
        } ?: LauncherSearchProviderUserControlPolicy.normalize(
            catalog = catalog,
            requestedEnabledProviderIds = emptySet(),
            requestedProviderOrder = emptyList(),
        )
    }
    val providers = remember(catalog, controls, searchProviderPreferences) {
        if (searchProviderPreferences == null) emptyList()
        else LauncherSearchProviderUserControlPolicy.automaticProviders(catalog, controls)
    }
    var results by remember(providers, query) {
        mutableStateOf<List<LauncherSearchResult>>(emptyList())
    }
    var complete by remember(providers, query, searchProviderPreferences) {
        mutableStateOf(false)
    }
    val explicitHandoffs = remember(query, controls) {
        LauncherSearchPresentationPolicy.explicitHandoffProviders(
            rawQuery = query,
            providerControls = controls,
        )
    }

    LaunchedEffect(providers, query, searchProviderPreferences) {
        if (searchProviderPreferences == null) {
            results = emptyList()
            complete = false
            return@LaunchedEffect
        }
        if (query.isBlank()) {
            results = emptyList()
            complete = true
            return@LaunchedEffect
        }
        complete = false
        results = LauncherUniversalSearch.searchAsync(
            rawQuery = query,
            providers = providers,
            policy = LauncherSearchExecutionPolicy.cancellationOnly(),
        )
        complete = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = GlazeMetrics.space3, vertical = GlazeMetrics.space2),
        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    start = GlazeMetrics.space3,
                    top = GlazeMetrics.space2,
                    bottom = GlazeMetrics.space2,
                    end = GlazeMetrics.space2,
                ),
                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (showSources) "Search sources" else "Universal Search",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (showSources) "Choose where Launcher can search"
                        else "Search privately across enabled sources",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Done")
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            onClick = { showSources = !showSources },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = GlazeMetrics.space3,
                    vertical = GlazeMetrics.space2,
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (showSources) "Back to Search" else "Manage search sources",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    if (showSources) "←" else "Manage ›",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (showSources) {
            LauncherSearchSourceManager(
                persisted = searchProviderPreferences,
                controls = controls,
                onSet = onSetSearchProviderPreferences,
                fileSearchRoots = fileSearchRoots,
                onSetEnabled = onSetSearchProviderEnabled,
                onChooseFileSearchRoot = onChooseFileSearchRoot,
                onRemoveFileSearchRoot = onRemoveFileSearchRoot,
                onReset = onResetSearchProviderPreferences,
                modifier = Modifier.weight(1f),
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                GlazeAppSearchField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    requestFocus = true,
                    placeholder = "Search this device",
                    inputTestTag = "launcher-universal-search-field",
                )
            }
            if (explicitHandoffs.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                ) {
                    Column(
                        modifier = Modifier.padding(GlazeMetrics.space3),
                        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                    ) {
                        Text(
                            "Search with",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Your query is sent only to a provider you tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                        ) {
                            explicitHandoffs.forEach { provider ->
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { onSearchWithConnectedProvider(provider.providerId, query) },
                                ) { Text(provider.displayName) }
                            }
                        }
                    }
                }
            }

            val providerIssues by LauncherLocalSearchDiagnostics.issues.collectAsState()
            val enabledIssues = providerIssues.filterKeys { controls.isEnabled(it) }
            if (query.isNotBlank() && enabledIssues.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        "Some sources could not be searched. Open Manage search sources for details.",
                        modifier = Modifier.padding(GlazeMetrics.space3),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    ) {
                        Column(
                            modifier = Modifier.padding(GlazeMetrics.space4),
                            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                        ) {
                            Text(
                                when {
                                    searchProviderPreferences == null -> "Loading sources"
                                    providers.isEmpty() -> "Enable local search sources"
                                    !complete -> "Searching…"
                                    query.isBlank() -> "Find anything on your device"
                                    else -> "No matching results",
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                when {
                                    query.isBlank() -> "Enter a name, number, app, setting or filename. " +
                                        "Contacts, calls and messages are included only when enabled and permitted."
                                    enabledIssues.isNotEmpty() -> "Review the source status before trying again."
                                    else -> "Try another term or review your enabled sources.",
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                ) {
                    Text(
                        "${results.size} matching results",
                        modifier = Modifier.padding(horizontal = GlazeMetrics.space3, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Group the ranked stream without rescoring entries within any category.
                // Only categories with actual matches are shown; blank queries no longer
                // flood the surface with every installed app and every Launcher action.
                val sections = listOf(
                    LauncherSearchCategory.APPLICATION to "Apps",
                    LauncherSearchCategory.CONTACT to "Contacts",
                    LauncherSearchCategory.CALL_HISTORY to "Calls",
                    LauncherSearchCategory.MESSAGE to "Messages",
                    LauncherSearchCategory.FILE to "Files",
                    LauncherSearchCategory.SHORTCUT to "App shortcuts",
                    LauncherSearchCategory.ACTION to "Actions",
                    LauncherSearchCategory.SETTING to "Settings",
                    LauncherSearchCategory.CONNECTED_SOURCE to "Connected",
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space1),
                ) {
                    sections.forEach { (category, title) ->
                        val matches = results.filter { it.category == category }
                        if (matches.isNotEmpty()) {
                            item(key = "section:" + category.name) {
                                Text(
                                    title,
                                    modifier = Modifier.padding(
                                        top = GlazeMetrics.space2,
                                        start = GlazeMetrics.space2,
                                        bottom = 2.dp,
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            items(matches, key = { it.providerId + ":" + it.resultId }) { result ->
                                LauncherProviderSearchRow(result) {
                                    when (val action = result.action) {
                                        is LaunchApplicationSearchAction -> onLaunchApp(action.app)
                                        is LauncherLaunchShortcutSearchAction -> onLaunchShortcut(action)
                                        is LauncherOpenUriSearchAction -> onOpenSearchUri(action)
                                        is LauncherOpenDocumentSearchAction -> onOpenDocument(action)
                                        is LauncherNavigateSearchAction -> onNavigate(action.destination)
                                        else -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherSearchSourceManager(
    persisted: LauncherSearchProviderPreferenceDecodeResult?,
    controls: LauncherSearchProviderControlState,
    fileSearchRoots: List<Uri>,
    onSet: (LauncherSearchProviderPreferenceSnapshot) -> Unit,
    onSetEnabled: (LauncherSearchProviderControlState, String, Boolean) -> Unit,
    onChooseFileSearchRoot: () -> Unit,
    onRemoveFileSearchRoot: (Uri) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ready = persisted != null
    LazyColumn(
        modifier = modifier.fillMaxWidth().testTag("launcher-search-source-manager"),
        verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(Modifier.padding(GlazeMetrics.space3)) {
                    Text("Privacy-first provider controls", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (ready) "Only enabled local sources receive typed queries; network and third-party sources require an explicit Search with action."
                        else "Loading saved controls; automatic Search stays off.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onReset, enabled = ready) { Text("Use safe defaults") }
                }
            }
        }
        items(controls.orderedOptions, key = { it.providerId }) { option ->
            val index = controls.orderedOptions.indexOfFirst { it.providerId == option.providerId }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                ),
            ) {
                Column(Modifier.padding(GlazeMetrics.space3)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(option.displayName, fontWeight = FontWeight.SemiBold)
                            Text(
                                buildString {
                                    append(option.privacySummary)
                                    if (option.providerId == LauncherFilesSearchProvider.PROVIDER_ID) {
                                        append(" · ")
                                        append(
                                            when (fileSearchRoots.size) {
                                                0 -> "No folders selected"
                                                1 -> "1 folder selected"
                                                else -> fileSearchRoots.size.toString() + " folders selected"
                                            },
                                        )
                                    }
                                    if (
                                        !LauncherLocalSearchPermissions.isGranted(
                                            context,
                                            option.providerId,
                                        )
                                    ) {
                                        append(" · Android permission required")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = controls.isEnabled(option.providerId),
                                onCheckedChange = { enabled ->
                                    onSetEnabled(controls, option.providerId, enabled)
                                },
                                enabled = ready,
                                modifier = Modifier.testTag(
                                    "launcher-search-source-" + option.providerId,
                                ),
                            )
                            if (option.providerId == LauncherFilesSearchProvider.PROVIDER_ID) {
                                TextButton(
                                    onClick = onChooseFileSearchRoot,
                                    enabled = ready,
                                ) {
                                    Text(
                                        if (fileSearchRoots.isEmpty()) "Choose folder"
                                        else "Add folder",
                                    )
                                }
                            }
                        }
                    }
                    if (
                        option.providerId == LauncherFilesSearchProvider.PROVIDER_ID &&
                        fileSearchRoots.isNotEmpty()
                    ) {
                        fileSearchRoots.forEach { root ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                            ) {
                                Text(
                                    root.lastPathSegment
                                        ?.substringAfterLast(':')
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "Selected folder",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                TextButton(
                                    onClick = { onRemoveFileSearchRoot(root) },
                                    enabled = ready,
                                ) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = {
                                onSet(LauncherSearchProviderUserControlPolicy.moveProviderBy(
                                    controls, option.providerId, -1,
                                ))
                            },
                            enabled = ready && index > 0,
                        ) { Text("Earlier") }
                        TextButton(
                            onClick = {
                                onSet(LauncherSearchProviderUserControlPolicy.moveProviderBy(
                                    controls, option.providerId, 1,
                                ))
                            },
                            enabled = ready && index in 0 until controls.orderedOptions.lastIndex,
                        ) { Text("Later") }
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherProviderSearchRow(
    result: LauncherSearchResult,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(GlazeMetrics.radiusLarge),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(GlazeMetrics.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
        ) {
            Column(Modifier.weight(1f)) {
                Text(result.title, fontWeight = FontWeight.SemiBold)
                result.subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                when (result.category) {
                    LauncherSearchCategory.APPLICATION -> "App"
                    LauncherSearchCategory.SHORTCUT -> "Shortcut"
                    LauncherSearchCategory.CONTACT -> "Contact"
                    LauncherSearchCategory.CALL_HISTORY -> "Call"
                    LauncherSearchCategory.MESSAGE -> "Message"
                    LauncherSearchCategory.FILE -> "File"
                    LauncherSearchCategory.CONNECTED_SOURCE -> "Connected"
                    LauncherSearchCategory.SETTING -> "Setting"
                    LauncherSearchCategory.ACTION -> "Action"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
