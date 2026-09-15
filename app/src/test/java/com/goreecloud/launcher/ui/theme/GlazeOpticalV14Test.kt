package com.goreecloud.launcher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeOpticalV14Test {
    @Test
    fun `busy bright backgrounds increase frost`() {
        val calm = GlazeOpticalV14.resolve(
            GlazeOpticalInput(
                backgroundComplexity = GlazeBackgroundComplexity.SIMPLE,
                backgroundLuminance = GlazeBackgroundLuminance.MID,
            )
        )
        val busy = GlazeOpticalV14.resolve(
            GlazeOpticalInput(
                backgroundComplexity = GlazeBackgroundComplexity.COMPLEX,
                backgroundLuminance = GlazeBackgroundLuminance.BRIGHT,
            )
        )

        assertTrue(busy.frostStrength > calm.frostStrength)
        assertTrue(calm.frostStrength in 0.20f..0.88f)
        assertTrue(busy.frostStrength in 0.20f..0.88f)
    }

    @Test
    fun `semantic importance increases protection and reduces blur`() {
        val low = GlazeOpticalV14.resolve(GlazeOpticalInput(semanticImportance = 0f))
        val high = GlazeOpticalV14.resolve(GlazeOpticalInput(semanticImportance = 1f))
        assertTrue(high.semanticProtection > low.semanticProtection)
        assertTrue(high.blurScale < low.blurScale)
    }

    @Test
    fun `memory tint is capped at eight percent`() {
        val resolved = GlazeOpticalV14.resolve(GlazeOpticalInput(memoryTintInfluence = 1f))
        assertEquals(0.08f, resolved.memoryTintInfluence, 0.0001f)
    }

    @Test
    fun `reduced transparency fails closed to solid accessible mode`() {
        val resolved = GlazeOpticalV14.resolve(
            GlazeOpticalInput(
                memoryTintInfluence = 0.08f,
                accessibility = GlazeOpticalAccessibility(reducedTransparency = true),
            )
        )
        assertEquals(GlazeOpticalState.Mode.SOLID_ACCESSIBLE, resolved.mode)
        assertEquals(0f, resolved.blurScale, 0.0001f)
        assertEquals(0f, resolved.memoryTintInfluence, 0.0001f)
        assertFalse(resolved.decorativeTintAllowed)
    }

    @Test
    fun `forced colors fail closed to solid accessible mode`() {
        val resolved = GlazeOpticalV14.resolve(
            GlazeOpticalInput(accessibility = GlazeOpticalAccessibility(forcedColors = true))
        )
        assertEquals(GlazeOpticalState.Mode.SOLID_ACCESSIBLE, resolved.mode)
        assertEquals(1f, resolved.semanticProtection, 0.0001f)
        assertFalse(resolved.decorativeTintAllowed)
    }

    @Test
    fun `increased contrast suppresses decorative warmth and tint`() {
        val resolved = GlazeOpticalV14.resolve(
            GlazeOpticalInput(
                daypart = GlazeDaypart.DUSK,
                memoryTintInfluence = 0.08f,
                accessibility = GlazeOpticalAccessibility(increasedContrast = true),
            )
        )
        assertEquals(0f, resolved.warmth, 0.0001f)
        assertEquals(0f, resolved.memoryTintInfluence, 0.0001f)
        assertFalse(resolved.decorativeTintAllowed)
    }

    @Test
    fun `launcher targets exact stable Glaze UI v1_4_1 release`() {
        assertEquals("1.4.1", GlazeOpticalV14.targetVersion)
        assertEquals("1.4.1", GlazeMetrics.targetVersion)
        assertEquals("1.4.0", GlazeOpticalV14.rollbackVersion)
        assertEquals(GlazeOpticalV14.stableSourceRevision, GlazeMetrics.sourceRevision)
    }
}
