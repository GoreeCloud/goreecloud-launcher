package com.goreecloud.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class GlazeThemeMode { SYSTEM, LIGHT, DARK }

private val light = lightColorScheme(
    primary = Color(0xFF4259C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E5FF),
    onPrimaryContainer = Color(0xFF111A4F),
    secondary = Color(0xFF69558E),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF1B1B20),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1B1B20),
    surfaceVariant = Color(0xFFE8E7EE),
    onSurfaceVariant = Color(0xFF45464E),
)
private val dark = darkColorScheme(
    primary = Color(0xFFBAC3FF),
    onPrimary = Color(0xFF10226F),
    primaryContainer = Color(0xFF293D9B),
    onPrimaryContainer = Color(0xFFE0E5FF),
    secondary = Color(0xFFD3BDF6),
    background = Color(0xFF111116),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF17171D),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C5CF),
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
