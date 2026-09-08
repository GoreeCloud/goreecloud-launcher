package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Bounded GLAZE UI V1.3 Adaptive Resonance presentation primitives using the
 * inherited V1.2 Frosted Neutral material foundation.
 *
 * Neutral glass is the material; color is an accent. These values never establish protection, privacy, identity,
 * recovery, availability, focus, selection, or other authoritative state.
 * Accessibility and producer-owned semantics always take precedence over decorative treatment.
 * Launcher does not derive adaptive material color from wallpaper, application content, or other producer truth.
 */
object GlazeAtmosphere {
    // Neutral frosted material references inherited by the V1.3 native mapping.
    val frostWhite = Color(0xA8FFFFFF)
    val frostWhiteDense = Color(0xD9FFFFFF)
    val frostGraphite = Color(0xA61C1D20)
    val frostGraphiteDense = Color(0xD91A1C21)
    val frostDeepDark = Color(0xB012151A)

    // Accent is deliberately bounded and never used as the material substrate.
    val iceBlueAccent = Color(0xFF78A7FF)
    val violetAccent = Color(0xFF9B8CFF)

    // Neutral outlines preserve separation when transparency/effects degrade.
    val lightNeutralLine = Color(0x1A505050)
    val darkNeutralLine = Color(0x1AFFFFFF)

    const val decorativeAccentMaxAlpha = 0.12f
    const val defaultAccentFieldsMax = 1
}
