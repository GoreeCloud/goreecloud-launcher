package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the Glaze UI 2.1 Stable geometry, spacing, and
 * interaction-target subset currently consumed by GoreeCloud Launcher.
 *
 * Spacing property names intentionally preserve the canonical 2.1 token keys:
 * space.1/2/3/4/5/6/8/10/12/16 map to 4/8/12/16/20/24/32/40/48/64 dp.
 * Geometry maps radius.sm/md/control/lg/xl/2xl/pill to
 * 10/14/16/22/28/32/999 dp.
 *
 * The active mobile interaction floor follows currentContract.touchMinimum at
 * 48 dp. The retained compatibility token target.minimum=44 does not lower the
 * current 2.1 consumer contract. touchAssistanceTarget maps the 56 dp
 * currentContract.touchAssistanceMinimum for later accessibility-resolution
 * integration.
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
    val space8: Dp = 32.dp
    val space10: Dp = 40.dp
    val space12: Dp = 48.dp
    val space16: Dp = 64.dp

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
