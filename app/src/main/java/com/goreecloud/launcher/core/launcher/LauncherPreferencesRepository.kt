package com.goreecloud.launcher.core.launcher

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.launcherPreferencesStore by preferencesDataStore(name = "launcher_preferences")

data class LauncherPreferences(
    val homeColumns: Int = 4,
    val homeRows: Int = 5,
    val drawerColumns: Int = 5,
    val showLabels: Boolean = true,
    val iconScale: Float = 1.0f,
) {
    val homeCapacity: Int get() = homeColumns * homeRows

    fun sanitized(): LauncherPreferences = copy(
        homeColumns = homeColumns.coerceIn(4, 6),
        homeRows = homeRows.coerceIn(4, 7),
        drawerColumns = drawerColumns.coerceIn(4, 6),
        iconScale = iconScale.coerceIn(0.85f, 1.15f),
    )
}

class LauncherPreferencesRepository(private val context: Context) {
    private object Keys {
        val homeColumns = intPreferencesKey("home_columns")
        val homeRows = intPreferencesKey("home_rows")
        val drawerColumns = intPreferencesKey("drawer_columns")
        val showLabels = booleanPreferencesKey("show_labels")
        val iconScale = floatPreferencesKey("icon_scale")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val defaults = LauncherPreferences()

    val preferences: Flow<LauncherPreferences> = context.launcherPreferencesStore.data
        .map { values ->
            LauncherPreferences(
                homeColumns = values[Keys.homeColumns] ?: defaults.homeColumns,
                homeRows = values[Keys.homeRows] ?: defaults.homeRows,
                drawerColumns = values[Keys.drawerColumns] ?: defaults.drawerColumns,
                showLabels = values[Keys.showLabels] ?: defaults.showLabels,
                iconScale = values[Keys.iconScale] ?: defaults.iconScale,
            ).sanitized()
        }
        .distinctUntilChanged()

    fun setHomeGrid(columns: Int, rows: Int) {
        val normalized = LauncherPreferences(homeColumns = columns, homeRows = rows).sanitized()
        scope.launch {
            context.launcherPreferencesStore.edit { values ->
                values[Keys.homeColumns] = normalized.homeColumns
                values[Keys.homeRows] = normalized.homeRows
            }
        }
    }

    fun setDrawerColumns(columns: Int) {
        val normalized = columns.coerceIn(4, 6)
        scope.launch {
            context.launcherPreferencesStore.edit { values ->
                values[Keys.drawerColumns] = normalized
            }
        }
    }

    fun setShowLabels(show: Boolean) {
        scope.launch {
            context.launcherPreferencesStore.edit { values ->
                values[Keys.showLabels] = show
            }
        }
    }

    fun setIconScale(scale: Float) {
        val normalized = scale.coerceIn(0.85f, 1.15f)
        scope.launch {
            context.launcherPreferencesStore.edit { values ->
                values[Keys.iconScale] = normalized
            }
        }
    }
}
