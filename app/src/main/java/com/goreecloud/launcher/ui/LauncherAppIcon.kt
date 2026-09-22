package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.goreecloud.launcher.core.launcher.LauncherAppIconCache

/**
 * Reads cached icon state synchronously and performs any cold decode on the shared IO cache.
 * The caller's existing fallback stays visible while a cold icon is loading.
 *
 * Compose state is keyed by the authoritative cache invalidation stamp rather than the transient
 * LauncherActivityInfo instance. Full inventory refreshes therefore keep warm icons stable, while
 * package/profile invalidation still forces a correct reload.
 */
@Composable
internal fun rememberLauncherAppIcon(app: LauncherActivityInfo): ImageBitmap? {
    val cacheStamp = LauncherAppIconCache.stamp(app)
    var icon by remember(app.componentName, app.user, cacheStamp) {
        mutableStateOf(LauncherAppIconCache.peek(app)?.asImageBitmap())
    }

    LaunchedEffect(app.componentName, app.user, cacheStamp) {
        if (icon == null) {
            icon = LauncherAppIconCache.load(app)?.asImageBitmap()
        }
    }

    return icon
}
