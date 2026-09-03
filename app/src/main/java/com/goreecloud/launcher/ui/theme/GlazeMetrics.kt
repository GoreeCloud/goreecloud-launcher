package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the official GLAZE UI V1.0 foundation geometry,
 * spacing, and interaction-target subset currently consumed by GoreeCloud Launcher.
 *
 * Current design-system authority:
 * - product identity: GLAZE UI V1.0
 * - machine version: 1.0.0
 * - canonical source revision: 70909bbdccad378fb7281ae1842e2f5beed64c38
 *
 * Canonical V1 spacing consumed directly here is 4/8/12/16/20/24/32/48/64 dp.
 * `space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as
 * a canonical V1 token.
 *
 * V1 foundation radii are 12 dp small, 20 dp standard, 28 dp soft/panel, and pill.
 * The existing Launcher property names are retained as source-compatible semantic
 * aliases while mapping only to those V1 foundation tiers.
 *
 * V1 keeps a 48 dp normal touch-oriented target floor and a 56 dp Touch Assistance
 * / far-view target where applicable. This mapping is Development evidence only;
 * it does not establish rendered, accessibility, representative-device, release,
 * or production acceptance for Launcher.
 */
object GlazeMetrics {
    const val targetVersion = "1.0.0"
    const val sourceRevision = "70909bbdccad378fb7281ae1842e2f5beed64c38"

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

    val radiusSmall: Dp = 12.dp
    val radiusMedium: Dp = 20.dp
    val radiusControl: Dp = 12.dp
    val radiusLarge: Dp = 20.dp
    val radiusExtraLarge: Dp = 28.dp
    val radius2ExtraLarge: Dp = 28.dp
    val radiusPill: Dp = 999.dp

    val minimumTarget: Dp = 48.dp
    val comfortableTarget: Dp = 48.dp
    val touchAssistanceTarget: Dp = 56.dp

    fun interactionTarget(touchAssistance: Boolean): Dp =
        if (touchAssistance) touchAssistanceTarget else minimumTarget
}
