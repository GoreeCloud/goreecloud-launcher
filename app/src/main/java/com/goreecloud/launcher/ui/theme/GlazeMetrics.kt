package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the Glaze UI 2.1 Stable geometry, spacing, and
 * interaction-target subset currently consumed by GoreeCloud Launcher.
 *
 * The mapped values follow the promoted 2.1 token contract: the 4/8/12/16/20/
 * 24/32/40/48/64 spacing scale, 10/14/16/22/28/32 utility geometry plus the
 * 999 dp pill radius, a 48 dp general interaction floor, and the 56 dp
 * touch-assistance target required by the 2.1 accessibility resolution model.
 *
 * This bounded mapping is an Adoption Candidate. It does not establish full
 * Launcher Glaze UI 2.1 application acceptance.
 */
object GlazeMetrics {
    val space1: Dp = 4.dp
    val space2: Dp = 8.dp
    val space3: Dp = 12.dp
    val space4: Dp = 16.dp
    val space5: Dp = 20.dp
    val space6: Dp = 24.dp
    val space7: Dp = 32.dp
    val space8: Dp = 40.dp
    val space9: Dp = 48.dp
    val space10: Dp = 64.dp

    val radiusSmall: Dp = 10.dp
    val radiusMedium: Dp = 14.dp
    val radiusControl: Dp = 16.dp
    val radiusLarge: Dp = 22.dp
    val radiusExtraLarge: Dp = 28.dp
    val radius2ExtraLarge: Dp = 32.dp
    val radiusPill: Dp = 999.dp

    val minimumTarget: Dp = 48.dp
    val comfortableTarget: Dp = 48.dp
    val touchAssistanceTarget: Dp = 56.dp
}
