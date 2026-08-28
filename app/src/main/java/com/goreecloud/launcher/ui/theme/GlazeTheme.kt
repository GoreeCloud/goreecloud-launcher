package com.goreecloud.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class GlazeThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Glaze UI 2.0 Stable color mapping for the Launcher surfaces currently in use.
 * Values come from the promoted `tokens/glaze.tokens.json` Light/Dark foundation
 * and identity roles. Deep Dark remains an explicit application acceptance gap
 * rather than being approximated with an unreviewed palette.
 */
private val light = lightColorScheme(
    primary = Color(0xFF366CF6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x2E366CF6),
    onPrimaryContainer = Color(0xFF172033),
    secondary = Color(0xFF7C5CFF),
    background = Color(0xFFEEF3F9),
    onBackground = Color(0xFF172033),
    surface = Color(0xC2FFFFFF),
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xDBF4F7FB),
    onSurfaceVariant = Color(0xFF67748A),
)

private val dark = darkColorScheme(
    primary = Color(0xFF7AA2FF),
    onPrimary = Color(0xFF0B1020),
    primaryContainer = Color(0x3D7AA2FF),
    onPrimaryContainer = Color(0xFFF3F6FB),
    secondary = Color(0xFFA594FF),
    background = Color(0xFF0D1119),
    onBackground = Color(0xFFF3F6FB),
    surface = Color(0xC719202D),
    onSurface = Color(0xFFF3F6FB),
    surfaceVariant = Color(0xDB1F2736),
    onSurfaceVariant = Color(0xFFA1AEC0),
)

@Composable
fun GlazeTheme(mode: GlazeThemeMode, content: @Composable () -> Unit) {
    val useDark = when (mode) {
        GlazeThemeMode.SYSTEM -> isSystemInDarkTheme()
        GlazeThemeMode.LIGHT -> false
        GlazeThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (useDark) dark else light, content = content)
}
