package com.goreecloud.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GlazeThemeResolutionTest {
    @Test
    fun systemModeTracksSystemLightAndDarkState() {
        val systemLight = glazeColorSchemeFor(GlazeThemeMode.SYSTEM, systemDark = false)
        val systemDark = glazeColorSchemeFor(GlazeThemeMode.SYSTEM, systemDark = true)

        assertEquals(Color(0xFFF5F7FA), systemLight.background)
        assertEquals(Color(0xFF0B0D11), systemDark.background)
        assertNotEquals(systemLight.background, systemDark.background)
    }

    @Test
    fun explicitModesIgnoreSystemDarkState() {
        val explicitLightWhenSystemDark = glazeColorSchemeFor(GlazeThemeMode.LIGHT, systemDark = true)
        val explicitDarkWhenSystemLight = glazeColorSchemeFor(GlazeThemeMode.DARK, systemDark = false)
        val explicitDeepDarkWhenSystemLight = glazeColorSchemeFor(GlazeThemeMode.DEEP_DARK, systemDark = false)

        assertEquals(Color(0xFFF5F7FA), explicitLightWhenSystemDark.background)
        assertEquals(Color(0xFF0B0D11), explicitDarkWhenSystemLight.background)
        assertEquals(Color(0xFF05070A), explicitDeepDarkWhenSystemLight.background)
    }

    @Test
    fun deepDarkUsesExactV11StructuralCanvasAndPanelMapping() {
        val scheme = glazeColorSchemeFor(GlazeThemeMode.DEEP_DARK, systemDark = false)

        assertEquals(Color(0xFF05070A), scheme.background)
        assertEquals(Color(0xFF0D1015), scheme.surface)
        assertEquals(Color(0xE612161D), scheme.surfaceVariant)
        assertEquals(Color(0xFFF5F7FA), scheme.onBackground)
        assertEquals(Color(0xFFF5F7FA), scheme.onSurface)
        assertEquals(Color(0xFFABB4C2), scheme.onSurfaceVariant)
    }
}
