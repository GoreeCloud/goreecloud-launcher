package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the Glaze UI 2.0 Stable geometry, spacing, and
 * interaction-target subset currently consumed by GoreeCloud Launcher.
 *
 * The property names are retained as compatibility aliases for the existing
 * Compose call sites while their values now follow the promoted 2.0 contract:
 * 4/8/12/16/24/32/48 spacing, 8/12/16/24/32 utility geometry, a 999 dp
 * capsule control radius, and a 48 dp general interaction floor.
 *
 * This bounded mapping is an Adoption Candidate. It does not establish full
 * Launcher Glaze UI 2.0 product acceptance.
 */
object GlazeMetrics {
    val space1: Dp = 4.dp
    val space2: Dp = 8.dp
    val space3: Dp = 12.dp
    val space4: Dp = 16.dp
    val space5: Dp = 24.dp
    val space6: Dp = 32.dp
    val space8: Dp = 48.dp

    val radiusSmall: Dp = 8.dp
    val radiusMedium: Dp = 12.dp
    val radiusControl: Dp = 999.dp
    val radiusLarge: Dp = 16.dp
    val radiusExtraLarge: Dp = 24.dp
    val radius2ExtraLarge: Dp = 32.dp

    val minimumTarget: Dp = 48.dp
    val comfortableTarget: Dp = 48.dp
}
