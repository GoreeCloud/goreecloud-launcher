package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GlazeMetricsContractTest {
    @Test
    fun `launcher targets exact Glaze UI 2_2 Stable release`() {
        assertEquals("2.2.0", GlazeMetrics.stableVersion)
        assertEquals("fb5ecde4a8258503789ffde08ac46a2e524ef71e", GlazeMetrics.stablePromotionHead)
        assertEquals("6731098b28dd0393faa878c70d989a221d714a20", GlazeMetrics.stableReleaseRevision)
    }

    @Test
    fun `current interaction floors preserve normal and Touch Assistance semantics`() {
        assertEquals(48.dp, GlazeMetrics.minimumTarget)
        assertEquals(56.dp, GlazeMetrics.touchAssistanceTarget)
        assertEquals(48.dp, GlazeMetrics.interactionTarget(touchAssistance = false))
        assertEquals(56.dp, GlazeMetrics.interactionTarget(touchAssistance = true))
    }

    @Test
    fun `ordinary Launcher composition records the 2_2 System Glaze budget`() {
        assertEquals(1, GlazeMetrics.systemGlazeDominantPanelMax)
        assertEquals(3, GlazeMetrics.systemGlazeSmallFloatingControlsMax)
        assertFalse(GlazeMetrics.nestedBackdropBlurAllowed)
    }

    @Test
    fun `compatible geometry remains stable across 2_1 to 2_2 migration`() {
        assertEquals(4.dp, GlazeMetrics.space1)
        assertEquals(8.dp, GlazeMetrics.space2)
        assertEquals(14.dp, GlazeMetrics.radiusMedium)
        assertEquals(16.dp, GlazeMetrics.radiusControl)
        assertEquals(22.dp, GlazeMetrics.radiusLarge)
        assertEquals(28.dp, GlazeMetrics.radiusExtraLarge)
    }
}
