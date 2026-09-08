package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the GLAZE UI V1.2 Living Glaze geometry currently
 * consumed by GoreeCloud Launcher.
 *
 * The V1.2 correction intentionally reduces the oversized prototype feel seen
 * during the Nord N200 physical-device acceptance pass while preserving Android
 * touch-target floors.
 */
object GlazeMetrics {
    const val targetVersion = "1.2.0-dev"
    const val sourceRevision = "living-glaze-mobile-correction"

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

    // Living Glaze uses restrained geometry on dense mobile surfaces.
    val radiusSmall: Dp = 10.dp
    val radiusMedium: Dp = 16.dp
    val radiusControl: Dp = 14.dp
    val radiusLarge: Dp = 18.dp
    val radiusExtraLarge: Dp = 22.dp
    val radius2ExtraLarge: Dp = 24.dp
    val radiusPill: Dp = 999.dp

    val opticalMicro: Dp = 8.dp
    val opticalControl: Dp = 14.dp
    val opticalContainer: Dp = 20.dp
    val opticalHero: Dp = 28.dp
    val opticalCapsule: Dp = 999.dp

    val minimumTarget: Dp = 48.dp
    val comfortableTarget: Dp = 48.dp
    val touchAssistanceTarget: Dp = 56.dp

    fun interactionTarget(touchAssistance: Boolean): Dp =
        if (touchAssistance) touchAssistanceTarget else minimumTarget
}
