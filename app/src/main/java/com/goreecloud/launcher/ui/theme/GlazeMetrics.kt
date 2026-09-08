package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the GLAZE UI V1.2 Frosted Neutral geometry,
 * inherited V1 spacing, and interaction-target subset currently consumed by
 * GoreeCloud Launcher.
 *
 * Current design-system authority:
 * - product identity: GLAZE UI V1.2
 * - machine version: 1.2.0
 * - Stable release source revision: f285b9145e27e6e7027b075c37299d101945c272
 *
 * V1.2 preserves the established structural spacing/radius contract and the
 * 8/16/24/32 dp optical geometry references while replacing chromatic material
 * atmosphere with Frosted Neutral. `space10` remains a Launcher-owned 40 dp
 * layout convenience and is not claimed as a canonical Glaze token.
 *
 * The 48 dp normal touch-oriented floor and 56 dp Touch Assistance / far-view
 * target remain preserved. This mapping is Development evidence only; it does
 * not establish rendered, accessibility, representative-device, release, or
 * production acceptance for Launcher.
 */
object GlazeMetrics {
    const val targetVersion = "1.2.0"
    const val sourceRevision = "f285b9145e27e6e7027b075c37299d101945c272"

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

    val opticalMicro: Dp = 8.dp
    val opticalControl: Dp = 16.dp
    val opticalContainer: Dp = 24.dp
    val opticalHero: Dp = 32.dp
    val opticalCapsule: Dp = 999.dp

    val minimumTarget: Dp = 48.dp
    val comfortableTarget: Dp = 48.dp
    val touchAssistanceTarget: Dp = 56.dp

    fun interactionTarget(touchAssistance: Boolean): Dp =
        if (touchAssistance) touchAssistanceTarget else minimumTarget
}
