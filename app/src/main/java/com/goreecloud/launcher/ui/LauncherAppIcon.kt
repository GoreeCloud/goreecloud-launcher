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
 * Reuses one bounded process-local badged-icon bitmap across Launcher surfaces.
 * Cache misses decode asynchronously; callers keep their existing fallback presentation until the
 * Android-provided icon is available.
 */
@Composable
internal fun rememberLauncherAppIcon(app: LauncherActivityInfo): ImageBitmap? {
    var icon by remember(app.componentName, app.user, app) {
        mutableStateOf(LauncherAppIconCache.peek(app)?.asImageBitmap())
    }

    LaunchedEffect(app.componentName, app.user, app) {
        if (icon == null) {
            icon = LauncherAppIconCache.load(app)?.asImageBitmap()
        }
    }

    return icon
}
