package com.goreecloud.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class GlazeThemeMode { SYSTEM, LIGHT, DARK }

/**
 * GLAZE UI V1.0 Light/Dark foundation mapping for the Launcher surfaces
 * currently in use. Values are mapped from the exact post-reset V1 source
 * revision recorded by [GlazeMetrics].
 *
 * Durable reading/settings surfaces remain solid while bounded interactive
 * chrome may use the V1 panel/overlay equivalents. Deep Dark remains an
 * explicit application acceptance and implementation gap rather than being
 * approximated from a pre-reset palette. Complete Reduced Transparency,
 * Increased Contrast, Forced Colors/native equivalents, and adaptive behavior
 * remain separately acceptance-gated for Launcher.
 */
private val light = lightColorScheme(
    primary = Color(0xFF3478F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x1F3478F6),
    onPrimaryContainer = Color(0xFF151A23),
    secondary = Color(0xFF7657F6),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF151A23),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151A23),
    surfaceVariant = Color(0xE0FFFFFF),
    onSurfaceVariant = Color(0xFF5D6675),
)

private val dark = darkColorScheme(
    primary = Color(0xFF8DB5FF),
    onPrimary = Color(0xFF0B0D11),
    primaryContainer = Color(0x1F8DB5FF),
    onPrimaryContainer = Color(0xFFF5F7FA),
    secondary = Color(0xFFA990FF),
    background = Color(0xFF0B0D11),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF12151B),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xDB181D26),
    onSurfaceVariant = Color(0xFFB0B7C3),
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
