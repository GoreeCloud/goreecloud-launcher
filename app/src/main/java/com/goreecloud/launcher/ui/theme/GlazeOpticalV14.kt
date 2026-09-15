package com.goreecloud.launcher.ui.theme

enum class GlazeBackgroundComplexity { SIMPLE, MODERATE, COMPLEX, UNKNOWN }
enum class GlazeBackgroundLuminance { DARK, MID, BRIGHT, UNKNOWN }
enum class GlazeOpticalDepth { BASE, RAISED, OVERLAY, MODAL }
enum class GlazeDaypart { DAWN, DAY, DUSK, NIGHT, UNKNOWN }

data class GlazeOpticalAccessibility(
    val forcedColors: Boolean = false,
    val reducedTransparency: Boolean = false,
    val increasedContrast: Boolean = false,
    val reducedMotion: Boolean = false,
)

data class GlazeOpticalInput(
    val backgroundComplexity: GlazeBackgroundComplexity = GlazeBackgroundComplexity.UNKNOWN,
    val backgroundLuminance: GlazeBackgroundLuminance = GlazeBackgroundLuminance.UNKNOWN,
    val depth: GlazeOpticalDepth = GlazeOpticalDepth.BASE,
    val daypart: GlazeDaypart = GlazeDaypart.UNKNOWN,
    val baseFrost: Float = 0.34f,
    val sensitivityFactor: Float = 0.38f,
    val semanticImportance: Float = 0.65f,
    val memoryTintInfluence: Float = 0f,
    val accessibility: GlazeOpticalAccessibility = GlazeOpticalAccessibility(),
)

data class GlazeOpticalState(
    val mode: Mode,
    val frostStrength: Float,
    val blurScale: Float,
    val semanticProtection: Float,
    val depthHueShift: Float,
    val warmth: Float,
    val memoryTintInfluence: Float,
    val decorativeTintAllowed: Boolean,
) {
    enum class Mode { ADAPTIVE_OPTICAL, SOLID_ACCESSIBLE }
}

/**
 * Native Android mapping of the GLAZE UI V1.4.1 Optical Hardening invariants
 * used by GoreeCloud Launcher.
 *
 * This resolver is deliberately local and deterministic. It consumes already-
 * derived context supplied by the Launcher and does not collect telemetry,
 * wallpaper pixels, camera data, analytics, or remote context. Any future signal
 * adapter remains subject to Launcher privacy/security authority.
 *
 * Shared Glaze V1.4.1 human/device qualification does not establish Launcher-
 * local rendered, assistive-technology, physical-device, Human Visual Excellence,
 * or performance acceptance.
 */
object GlazeOpticalV14 {
    const val targetVersion = "1.4.1"
    const val stableSourceRevision = "4fab9da0fad2e5c974e0e66ec88632c61745751c"
    const val maxMemoryTintInfluence = 0.08f

    const val launcherPhysicalDeviceAcceptanceEstablished = false
    const val launcherManualAssistiveTechnologyAcceptanceEstablished = false
    const val launcherHumanVisualExcellenceAccepted = false
    const val launcherRepresentativePerformanceAccepted = false

    fun resolve(input: GlazeOpticalInput = GlazeOpticalInput()): GlazeOpticalState {
        val accessibility = input.accessibility
        if (accessibility.forcedColors || accessibility.reducedTransparency) {
            return GlazeOpticalState(
                mode = GlazeOpticalState.Mode.SOLID_ACCESSIBLE,
                frostStrength = 1f,
                blurScale = 0f,
                semanticProtection = 1f,
                depthHueShift = 0f,
                warmth = 0f,
                memoryTintInfluence = 0f,
                decorativeTintAllowed = false,
            )
        }

        val variance = (complexityVariance(input.backgroundComplexity) +
            luminanceVariance(input.backgroundLuminance)).coerceIn(0f, 1f)
        var frost = (
            input.baseFrost.coerceIn(0.20f, 0.72f) +
                variance * input.sensitivityFactor.coerceIn(0f, 0.55f)
            ).coerceIn(0.20f, 0.88f)

        if (accessibility.increasedContrast) {
            frost = (frost + 0.10f).coerceIn(0.20f, 0.92f)
        }

        val semanticProtection = (
            0.50f + input.semanticImportance.coerceIn(0f, 1f) * 0.46f +
                if (accessibility.increasedContrast) 0.04f else 0f
            ).coerceIn(0.50f, 1f)
        val blurScale = (1f - semanticProtection * 0.42f).coerceIn(0.50f, 0.80f)

        return GlazeOpticalState(
            mode = GlazeOpticalState.Mode.ADAPTIVE_OPTICAL,
            frostStrength = frost,
            blurScale = blurScale,
            semanticProtection = semanticProtection,
            depthHueShift = depthHueShift(input.depth),
            warmth = if (accessibility.increasedContrast) 0f else warmthFor(input.daypart),
            memoryTintInfluence = if (accessibility.increasedContrast) {
                0f
            } else {
                input.memoryTintInfluence.coerceIn(0f, maxMemoryTintInfluence)
            },
            decorativeTintAllowed = !accessibility.increasedContrast,
        )
    }

    private fun complexityVariance(value: GlazeBackgroundComplexity): Float = when (value) {
        GlazeBackgroundComplexity.COMPLEX -> 0.90f
        GlazeBackgroundComplexity.MODERATE -> 0.55f
        GlazeBackgroundComplexity.SIMPLE -> 0.18f
        GlazeBackgroundComplexity.UNKNOWN -> 0.45f
    }

    private fun luminanceVariance(value: GlazeBackgroundLuminance): Float = when (value) {
        GlazeBackgroundLuminance.BRIGHT -> 0.12f
        GlazeBackgroundLuminance.DARK -> 0.06f
        GlazeBackgroundLuminance.MID -> 0.02f
        GlazeBackgroundLuminance.UNKNOWN -> 0.04f
    }

    private fun depthHueShift(value: GlazeOpticalDepth): Float = when (value) {
        GlazeOpticalDepth.BASE -> 0f
        GlazeOpticalDepth.RAISED -> 0.012f
        GlazeOpticalDepth.OVERLAY -> 0.022f
        GlazeOpticalDepth.MODAL -> 0.030f
    }

    private fun warmthFor(value: GlazeDaypart): Float = when (value) {
        GlazeDaypart.DAWN -> 0.035f
        GlazeDaypart.DAY -> 0f
        GlazeDaypart.DUSK -> 0.045f
        GlazeDaypart.NIGHT -> -0.018f
        GlazeDaypart.UNKNOWN -> 0f
    }
}
