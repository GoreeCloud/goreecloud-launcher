package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class GlazeMetricsContractTest {
    @Test
    fun `launcher targets exact GLAZE UI V1 reset baseline`() {
        assertEquals("1.0.0", GlazeMetrics.targetVersion)
        assertEquals("70909bbdccad378fb7281ae1842e2f5beed64c38", GlazeMetrics.sourceRevision)
    }

    @Test
    fun `v1 interaction floors preserve normal and Touch Assistance semantics`() {
        assertEquals(48.dp, GlazeMetrics.minimumTarget)
        assertEquals(56.dp, GlazeMetrics.touchAssistanceTarget)
        assertEquals(48.dp, GlazeMetrics.interactionTarget(touchAssistance = false))
        assertEquals(56.dp, GlazeMetrics.interactionTarget(touchAssistance = true))
    }

    @Test
    fun `launcher maps canonical v1 spacing and foundation radius tiers`() {
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
    fun `launcher owned 40dp convenience is not used as a v1 provenance marker`() {
        assertEquals(40.dp, GlazeMetrics.space10)
    }
}
