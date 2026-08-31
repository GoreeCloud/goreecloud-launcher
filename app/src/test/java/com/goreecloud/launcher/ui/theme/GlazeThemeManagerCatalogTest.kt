package com.goreecloud.launcher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class GlazeThemeManagerCatalogTest {
    @Test
    fun catalogExposesEachPersistedAppearanceExactlyOnce() {
        assertEquals(
            listOf(GlazeThemeMode.SYSTEM, GlazeThemeMode.LIGHT, GlazeThemeMode.DARK),
            GlazeThemeManagerCatalog.choices.map { it.mode },
        )
    }

    @Test
    fun choiceLookupReturnsStableUserFacingMetadata() {
        val dark = GlazeThemeManagerCatalog.choiceFor(GlazeThemeMode.DARK)

        assertEquals("Dark", dark.title)
        assertEquals(GlazeThemeMode.DARK, dark.mode)
    }

    @Test
    fun persistedModeDecoderFallsBackForUnknownValues() {
        assertEquals(GlazeThemeMode.LIGHT, GlazeThemeModeCodec.decode("LIGHT"))
        assertEquals(GlazeThemeMode.SYSTEM, GlazeThemeModeCodec.decode("DEEP_DARK"))
        assertEquals(GlazeThemeMode.DARK, GlazeThemeModeCodec.decode(null, GlazeThemeMode.DARK))
    }
}
