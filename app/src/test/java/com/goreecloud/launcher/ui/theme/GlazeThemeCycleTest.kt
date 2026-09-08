package com.goreecloud.launcher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class GlazeThemeCycleTest {
    @Test
    fun themeCycleIncludesDeepDarkAndReturnsToSystem() {
        assertEquals(GlazeThemeMode.LIGHT, nextGlazeThemeMode(GlazeThemeMode.SYSTEM))
        assertEquals(GlazeThemeMode.DARK, nextGlazeThemeMode(GlazeThemeMode.LIGHT))
        assertEquals(GlazeThemeMode.DEEP_DARK, nextGlazeThemeMode(GlazeThemeMode.DARK))
        assertEquals(GlazeThemeMode.SYSTEM, nextGlazeThemeMode(GlazeThemeMode.DEEP_DARK))
    }
}
