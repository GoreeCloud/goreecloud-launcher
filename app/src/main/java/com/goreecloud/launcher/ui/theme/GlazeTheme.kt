package com.goreecloud.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class GlazeThemeMode { SYSTEM, LIGHT, DARK, DEEP_DARK }

/**
 * GLAZE UI V1.3 Adaptive Resonance structural appearance mapping for Launcher,
 * retaining the inherited V1.2 Frosted Neutral material foundation.
 * Neutral glass is the material and color remains a bounded accent.
 *
 * The translucent neutral surface roles intentionally remain independent of
 * protection, privacy, identity, recovery, availability, focus, selection,
 * and other producer-owned semantic state. Accessibility resolution always
 * takes precedence over decorative material treatment.
 */
private val light = lightColorScheme(
    primary = Color(0xFF3478F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x1F3478F6),
    onPrimaryContainer = Color(0xFF151A23),
    secondary = Color(0xFF7657F6),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF151A23),
    surface = Color(0x94FFFFFF),
    onSurface = Color(0xFF151A23),
    surfaceVariant = Color(0xD9FFFFFF),
    onSurfaceVariant = Color(0xFF5D6675),
    outlineVariant = GlazeAtmosphere.lightNeutralLine,
)

private val dark = darkColorScheme(
    primary = Color(0xFF8DB5FF),
    onPrimary = Color(0xFF0B0D11),
    primaryContainer = Color(0x1F8DB5FF),
    onPrimaryContainer = Color(0xFFF5F7FA),
    secondary = Color(0xFFA990FF),
    background = Color(0xFF0B0D11),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0x9E19191B),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xD91A1C21),
    onSurfaceVariant = Color(0xFFB0B7C3),
    outlineVariant = GlazeAtmosphere.darkNeutralLine,
)

private val deepDark = darkColorScheme(
    primary = Color(0xFF8DB5FF),
    onPrimary = Color(0xFF05070A),
    primaryContainer = Color(0x1F8DB5FF),
    onPrimaryContainer = Color(0xFFF5F7FA),
    secondary = Color(0xFFA990FF),
    background = Color(0xFF05070A),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xB012151A),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xD90D1015),
    onSurfaceVariant = Color(0xFFABB4C2),
    outlineVariant = GlazeAtmosphere.darkNeutralLine,
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
