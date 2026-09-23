package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goreecloud.launcher.core.launcher.LauncherBadgeAppKey
import com.goreecloud.launcher.core.launcher.LauncherNotificationBadges

/**
 * Counts only: no notification title, sender, icon or message content is ever exposed in UI.
 * Work-profile counts require Android to grant this listener access to that profile.
 */
@Composable
internal fun LauncherAppBadgeMark(
    app: LauncherActivityInfo,
    modifier: Modifier = Modifier,
) {
    val enabled by LauncherNotificationBadges.enabled.collectAsState()
    val granted by LauncherNotificationBadges.accessGranted.collectAsState()
    val counts by LauncherNotificationBadges.counts.collectAsState()
    val count = if (enabled && granted) LauncherNotificationBadges.countFor(app, counts) else 0
    LauncherBadgeMark(count, modifier)
}

@Composable
internal fun LauncherFolderBadgeMark(
    apps: List<LauncherActivityInfo>,
    modifier: Modifier = Modifier,
) {
    val enabled by LauncherNotificationBadges.enabled.collectAsState()
    val granted by LauncherNotificationBadges.accessGranted.collectAsState()
    val counts by LauncherNotificationBadges.counts.collectAsState()
    val number = if (enabled && granted) {
        apps.sumOf { app ->
            counts[LauncherBadgeAppKey(app.componentName.packageName, app.user)] ?: 0
        }
    } else 0
    LauncherBadgeMark(number, modifier)
}

@Composable
private fun LauncherBadgeMark(count: Int, modifier: Modifier) {
    if (count <= 0) return
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 19.dp, minHeight = 19.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 3.dp)) {
            Text(
                if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}
