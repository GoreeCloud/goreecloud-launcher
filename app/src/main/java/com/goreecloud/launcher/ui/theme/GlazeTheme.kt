package com.goreecloud.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class GlazeThemeMode { SYSTEM, LIGHT, DARK, DEEP_DARK }

/**
 * GLAZE UI V1.2 Living Glaze mapping for the Launcher.
 *
 * Neutral glass is the material. GoreeCloud blue is the accent. The launcher
 * deliberately avoids tinting every surface blue so wallpaper, icons and
 * content remain the dominant visual layer.
 */
private val light = lightColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x263B82F6),
    onPrimaryContainer = Color(0xFF12223A),
    secondary = Color(0xFF174EA6),
    background = Color(0xFFF7FAFF),
    onBackground = Color(0xFF111820),
    surface = Color(0xFFFDFEFF),
    onSurface = Color(0xFF111820),
    surfaceVariant = Color(0xD9F7FAFF),
    onSurfaceVariant = Color(0xFF56616F),
    outline = Color(0x667A8796),
    outlineVariant = Color(0x337A8796),
    scrim = Color(0xFF08111C),
)

private val dark = darkColorScheme(
    primary = Color(0xFF79A9FF),
    onPrimary = Color(0xFF07101D),
    primaryContainer = Color(0x263B82F6),
    onPrimaryContainer = Color(0xFFF4F8FF),
    secondary = Color(0xFFA8C8FF),
    background = Color(0xFF0C1118),
    onBackground = Color(0xFFF4F7FB),
    surface = Color(0xFF151B23),
    onSurface = Color(0xFFF4F7FB),
    surfaceVariant = Color(0xD918202A),
    onSurfaceVariant = Color(0xFFC1C9D4),
    outline = Color(0x668D98A7),
    outlineVariant = Color(0x338D98A7),
    scrim = Color(0xFF000000),
)

private val deepDark = darkColorScheme(
    primary = Color(0xFF79A9FF),
    onPrimary = Color(0xFF05080D),
    primaryContainer = Color(0x263B82F6),
    onPrimaryContainer = Color(0xFFF4F8FF),
    secondary = Color(0xFFA8C8FF),
    background = Color(0xFF05080D),
    onBackground = Color(0xFFF4F7FB),
    surface = Color(0xFF0B1017),
    onSurface = Color(0xFFF4F7FB),
    surfaceVariant = Color(0xE610161E),
    onSurfaceVariant = Color(0xFFBBC5D2),
    outline = Color(0x665E6978),
    outlineVariant = Color(0x335E6978),
    scrim = Color(0xFF000000),
)

@Composable
fun GlazeTheme(mode: GlazeThemeMode, content: @Composable () -> Unit) {
    val scheme = when (mode) {
        GlazeThemeMode.SYSTEM -> if (isSystemInDarkTheme()) dark else light
        GlazeThemeMode.LIGHT -> light
        GlazeThemeMode.DARK -> dark
        GlazeThemeMode.DEEP_DARK -> deepDark
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
