package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeAtmosphereContractTest {
    @Test
    fun `v1_2 material stays neutral while accent stays bounded`() {
        assertEquals(Color(0xA8FFFFFF), GlazeAtmosphere.frostWhite)
        assertEquals(Color(0xA61C1D20), GlazeAtmosphere.frostGraphite)
        assertEquals(Color(0xB012151A), GlazeAtmosphere.frostDeepDark)
        assertEquals(Color(0xFF78A7FF), GlazeAtmosphere.iceBlueAccent)
        assertTrue(GlazeAtmosphere.decorativeAccentMaxAlpha <= 0.12f)
        assertEquals(1, GlazeAtmosphere.defaultAccentFieldsMax)
    }
}
