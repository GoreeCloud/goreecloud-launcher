package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android mapping of the current GLAZE UI V1.3 Adaptive Resonance geometry,
 * inherited Frosted Neutral material foundation, spacing, and interaction-target subset
 * consumed by GoreeCloud Launcher.
 *
 * Current design-system authority:
 * - product identity: GLAZE UI V1.3 — Adaptive Resonance
 * - machine version: 1.3.0
 * - exact Stable integration revision: fc7cc91d2eace8da2371371c2855c24cbcb326a1
 * - rollback baseline: 1.2.0
 *
 * V1.3 inherits the established V1.2 neutral material, structural spacing/radius
 * contract, and 8/16/24/32 dp optical geometry references while adding bounded adaptive
 * expression, ergonomic composition, and resilience requirements. `space10` remains a
 * Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.
 *
 * The 48 dp normal touch-oriented floor and 56 dp Touch Assistance / far-view target
 * remain preserved. This mapping is Development evidence only; it does not establish
 * rendered, accessibility, representative-device, rollback, release, or production
 * acceptance for Launcher.
 */
object GlazeMetrics {
    const val targetVersion = "1.3.0"
    const val releaseTheme = "Adaptive Resonance"
    const val sourceRevision = "fc7cc91d2eace8da2371371c2855c24cbcb326a1"
    const val opticalContract = "tokens/glaze-v1.2-optical-foundation.candidate.json"
    const val adaptiveContract = "contracts/v1.3/adaptive-resonance.plan.json"
    const val stableWebEntrypoint = "css/glaze-v1.3.0.css"
    const val stableRuntimeEntrypoint = "js/glaze-v1.3.0.mjs"
    const val rollbackBaselineVersion = "1.2.0"

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
