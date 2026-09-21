package com.goreecloud.launcher.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeV16AndroidPresentationContextTest {
    @Test
    fun disabledAnimatorsMapToReducedMotionWithoutInventingOtherState() {
        val context = GlazeV16AndroidPresentationContext.fromSignals(
            animatorsEnabled = false,
            highTextContrastEnabled = false,
            touchExplorationEnabled = false,
            fontScale = 1f,
        )

        assertTrue(context.reducedMotion)
        assertFalse(context.reducedTransparency)
        assertFalse(context.increasedContrast)
        assertFalse(context.touchAssistance)
        assertFalse(context.screenReaderOptimized)
    }

    @Test
    fun accessibilitySignalsMapOnlyToOwnedPresentationSemantics() {
        val context = GlazeV16AndroidPresentationContext.fromSignals(
            animatorsEnabled = true,
            highTextContrastEnabled = true,
            touchExplorationEnabled = true,
            fontScale = 1.3f,
        )

        assertFalse(context.reducedMotion)
        assertTrue(context.increasedContrast)
        assertTrue(context.largeText)
        assertTrue(context.extraLargeText)
        assertTrue(context.touchAssistance)
        assertTrue(context.strongFocus)
        assertFalse(context.screenReaderOptimized)
    }
}
