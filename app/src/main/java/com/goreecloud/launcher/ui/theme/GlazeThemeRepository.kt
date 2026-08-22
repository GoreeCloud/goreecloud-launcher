package com.goreecloud.launcher.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.themeStore by preferencesDataStore(name = "glaze_theme")

class GlazeThemeRepository(private val context: Context) {
    private val key = stringPreferencesKey("appearance")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val defaultMode = GlazeThemeMode.SYSTEM
    val themeMode: Flow<GlazeThemeMode> = context.themeStore.data.map {
        it[key]?.let { value -> runCatching { GlazeThemeMode.valueOf(value) }.getOrNull() } ?: defaultMode
    }

    fun cycleMode(current: GlazeThemeMode) {
        val next = when (current) {
            GlazeThemeMode.SYSTEM -> GlazeThemeMode.LIGHT
            GlazeThemeMode.LIGHT -> GlazeThemeMode.DARK
            GlazeThemeMode.DARK -> GlazeThemeMode.SYSTEM
        }
        scope.launch { context.themeStore.edit { prefs -> prefs[key] = next.name } }
    }
}
