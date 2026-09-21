package com.goreecloud.launcher.ui.theme

import android.animation.ValueAnimator
import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * Maps Android-owned runtime accessibility and animation signals into Launcher's repository-local
 * GLAZE UI V1.6 presentation context.
 *
 * The mapping is intentionally narrow. It does not infer a screen reader from touch exploration,
 * does not invent reduced-transparency or device-performance state, and does not establish
 * application conformance by itself.
 */
object GlazeV16AndroidPresentationContext {
    fun resolve(context: Context): GlazeV16PresentationContext {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        return fromSignals(
            animatorsEnabled = ValueAnimator.areAnimatorsEnabled(),
            highTextContrastEnabled = accessibilityManager?.isHighTextContrastEnabled == true,
            touchExplorationEnabled = accessibilityManager?.isTouchExplorationEnabled == true,
            fontScale = context.resources.configuration.fontScale,
        )
    }

    internal fun fromSignals(
        animatorsEnabled: Boolean,
        highTextContrastEnabled: Boolean,
        touchExplorationEnabled: Boolean,
        fontScale: Float,
    ): GlazeV16PresentationContext {
        val normalizedFontScale = fontScale.coerceAtLeast(1f)
        return GlazeV16PresentationContext(
            reducedMotion = !animatorsEnabled,
            increasedContrast = highTextContrastEnabled,
            largeText = normalizedFontScale > 1f,
            extraLargeText = normalizedFontScale >= EXTRA_LARGE_TEXT_SCALE,
            touchAssistance = touchExplorationEnabled,
            strongFocus = touchExplorationEnabled || highTextContrastEnabled,
        )
    }

    private const val EXTRA_LARGE_TEXT_SCALE = 1.3f
}
