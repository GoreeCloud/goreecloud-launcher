package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeAtmosphereContractTest {
    @Test
    fun `v1_4 atmosphere stays bounded and non semantic`() {
        assertEquals(Color(0xFF0F6B6F), GlazeAtmosphere.deepTeal)
        assertEquals(Color(0xFFD9A35F), GlazeAtmosphere.softAmber)
        assertEquals(Color(0xFFF2D7A6), GlazeAtmosphere.warmGlow)

        assertTrue(GlazeAtmosphere.lightTealAuraMaxAlpha <= 0.08f)
        assertTrue(GlazeAtmosphere.lightAmberAuraMaxAlpha <= 0.04f)
        assertTrue(GlazeAtmosphere.darkTealAuraMaxAlpha <= 0.12f)
        assertTrue(GlazeAtmosphere.darkAmberAuraMaxAlpha <= 0.06f)
        assertTrue(GlazeAtmosphere.deepDarkTealAuraMaxAlpha <= 0.16f)
        assertTrue(GlazeAtmosphere.deepDarkAmberAuraMaxAlpha <= 0.08f)
        assertEquals(2, GlazeAtmosphere.defaultAuraFieldsMax)
        assertEquals(1, GlazeAtmosphere.defaultTealFieldsMax)
        assertEquals(1, GlazeAtmosphere.defaultAmberFieldsMax)
    }
}
