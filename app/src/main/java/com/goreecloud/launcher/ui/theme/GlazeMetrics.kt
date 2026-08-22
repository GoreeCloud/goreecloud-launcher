package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the Glaze UI 1.4 Stable semantic metrics consumed by
 * GoreeCloud Launcher. These values intentionally cover only the subset used by
 * the current launcher surfaces; broader design-system adoption remains explicit
 * and application-specific.
 */
object GlazeMetrics {
    val space1: Dp = 4.dp
    val space2: Dp = 8.dp
    val space3: Dp = 12.dp
    val space4: Dp = 16.dp
    val space5: Dp = 20.dp
    val space6: Dp = 24.dp
    val space8: Dp = 32.dp

    val radiusSmall: Dp = 10.dp
    val radiusMedium: Dp = 14.dp
    val radiusControl: Dp = 16.dp
    val radiusLarge: Dp = 22.dp
    val radiusExtraLarge: Dp = 28.dp
    val radius2ExtraLarge: Dp = 32.dp

    val minimumTarget: Dp = 44.dp
    val comfortableTarget: Dp = 48.dp
}
