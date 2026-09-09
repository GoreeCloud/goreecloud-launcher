package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class GlazeMetricsContractTest {
    @Test
    fun `launcher targets exact GLAZE UI V1_3 Stable authority`() {
        assertEquals("1.3.0", GlazeMetrics.targetVersion)
        assertEquals("Adaptive Resonance", GlazeMetrics.releaseTheme)
        assertEquals("fc7cc91d2eace8da2371371c2855c24cbcb326a1", GlazeMetrics.sourceRevision)
        assertEquals(
            "tokens/glaze-v1.2-optical-foundation.candidate.json",
            GlazeMetrics.opticalContract,
        )
        assertEquals(
            "contracts/v1.3/adaptive-resonance.plan.json",
            GlazeMetrics.adaptiveContract,
        )
        assertEquals("css/glaze-v1.3.0.css", GlazeMetrics.stableWebEntrypoint)
        assertEquals("js/glaze-v1.3.0.mjs", GlazeMetrics.stableRuntimeEntrypoint)
        assertEquals("1.2.0", GlazeMetrics.rollbackBaselineVersion)
    }

    @Test
    fun `v1 interaction floors preserve normal and Touch Assistance semantics`() {
        assertEquals(48.dp, GlazeMetrics.minimumTarget)
        assertEquals(56.dp, GlazeMetrics.touchAssistanceTarget)
        assertEquals(48.dp, GlazeMetrics.interactionTarget(touchAssistance = false))
        assertEquals(56.dp, GlazeMetrics.interactionTarget(touchAssistance = true))
    }

    @Test
    fun `launcher preserves inherited spacing and structural radius tiers`() {
        assertEquals(4.dp, GlazeMetrics.space1)
        assertEquals(8.dp, GlazeMetrics.space2)
        assertEquals(12.dp, GlazeMetrics.space3)
        assertEquals(16.dp, GlazeMetrics.space4)
        assertEquals(20.dp, GlazeMetrics.space5)
        assertEquals(24.dp, GlazeMetrics.space6)
        assertEquals(32.dp, GlazeMetrics.space8)
        assertEquals(48.dp, GlazeMetrics.space12)
        assertEquals(64.dp, GlazeMetrics.space16)

        assertEquals(12.dp, GlazeMetrics.radiusSmall)
        assertEquals(20.dp, GlazeMetrics.radiusMedium)
        assertEquals(12.dp, GlazeMetrics.radiusControl)
        assertEquals(20.dp, GlazeMetrics.radiusLarge)
        assertEquals(28.dp, GlazeMetrics.radiusExtraLarge)
        assertEquals(28.dp, GlazeMetrics.radius2ExtraLarge)
        assertEquals(999.dp, GlazeMetrics.radiusPill)
    }

    @Test
    fun `v1_3 inherits optical geometry separately from structural radii`() {
        assertEquals(8.dp, GlazeMetrics.opticalMicro)
        assertEquals(16.dp, GlazeMetrics.opticalControl)
        assertEquals(24.dp, GlazeMetrics.opticalContainer)
        assertEquals(32.dp, GlazeMetrics.opticalHero)
        assertEquals(999.dp, GlazeMetrics.opticalCapsule)
    }

    @Test
    fun `launcher owned 40dp convenience is not used as a Glaze provenance marker`() {
        assertEquals(40.dp, GlazeMetrics.space10)
    }
}
