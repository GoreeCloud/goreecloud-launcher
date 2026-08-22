package com.goreecloud.launcher.core.workspace

import android.content.Context
import android.content.pm.LauncherActivityInfo
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val ENTRY_SEPARATOR = "\u001F"
private val Context.workspaceDataStore by preferencesDataStore(name = "launcher_workspace")

data class WorkspaceState(
    val initialized: Boolean = false,
    val favoriteKeys: List<String> = emptyList(),
    val dockKeys: List<String> = emptyList(),
)

fun LauncherActivityInfo.workspaceKey(): String =
    "${user.identifier}:${componentName.flattenToString()}"

class WorkspaceRepository(private val context: Context) {
    private object Keys {
        val initialized = booleanPreferencesKey("initialized")
        val favorites = stringPreferencesKey("favorites")
        val dock = stringPreferencesKey("dock")
    }

    val state: Flow<WorkspaceState> = context.workspaceDataStore.data
        .map { preferences ->
            WorkspaceState(
                initialized = preferences[Keys.initialized] ?: false,
                favoriteKeys = decode(preferences[Keys.favorites]),
                dockKeys = decode(preferences[Keys.dock]),
            )
        }
        .distinctUntilChanged()

    suspend fun ensureDefaults(favoriteKeys: List<String>, dockKeys: List<String>) {
        context.workspaceDataStore.edit { preferences ->
            if (preferences[Keys.initialized] == true) return@edit
            preferences[Keys.favorites] = encode(favoriteKeys.distinct())
            preferences[Keys.dock] = encode(dockKeys.distinct().take(MAX_DOCK_ITEMS))
            preferences[Keys.initialized] = true
        }
    }

    suspend fun toggleFavorite(key: String) {
        context.workspaceDataStore.edit { preferences ->
            val current = decode(preferences[Keys.favorites]).toMutableList()
            if (!current.remove(key)) current.add(key)
            preferences[Keys.favorites] = encode(current)
            preferences[Keys.initialized] = true
        }
    }

    suspend fun toggleDock(key: String) {
        context.workspaceDataStore.edit { preferences ->
            val current = decode(preferences[Keys.dock]).toMutableList()
            if (!current.remove(key) && current.size < MAX_DOCK_ITEMS) current.add(key)
            preferences[Keys.dock] = encode(current)
            preferences[Keys.initialized] = true
        }
    }

    private fun encode(values: List<String>): String = values.joinToString(ENTRY_SEPARATOR)

    private fun decode(value: String?): List<String> =
        value.orEmpty().split(ENTRY_SEPARATOR).filter { it.isNotBlank() }

    private companion object {
        const val MAX_DOCK_ITEMS = 5
    }
}
