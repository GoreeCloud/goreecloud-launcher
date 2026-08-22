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

private val Context.workspaceDataStore by preferencesDataStore(name = "launcher_workspace")

data class WorkspaceState(
    val initialized: Boolean = false,
    val favoriteKeys: List<String> = emptyList(),
    val dockKeys: List<String> = emptyList(),
)

enum class WorkspaceMoveDirection(val offset: Int) {
    EARLIER(-1),
    LATER(1),
}

fun LauncherActivityInfo.workspaceKey(): String =
    "${user.hashCode()}:${componentName.flattenToString()}"

internal object WorkspaceCodec {
    private const val ENTRY_SEPARATOR = "\u001F"
    const val MAX_DOCK_ITEMS = 5

    fun encode(values: List<String>): String = values.joinToString(ENTRY_SEPARATOR)

    fun decode(value: String?): List<String> =
        value.orEmpty().split(ENTRY_SEPARATOR).filter { it.isNotBlank() }

    fun toggled(values: List<String>, key: String, limit: Int? = null): List<String> {
        val current = values.toMutableList()
        if (current.remove(key)) return current
        if (limit == null || current.size < limit) current.add(key)
        return current
    }

    fun moved(values: List<String>, key: String, direction: WorkspaceMoveDirection): List<String> {
        val fromIndex = values.indexOf(key)
        if (fromIndex == -1 || values.size < 2) return values

        val toIndex = (fromIndex + direction.offset).coerceIn(0, values.lastIndex)
        if (fromIndex == toIndex) return values

        return values.toMutableList().apply {
            removeAt(fromIndex)
            add(toIndex, key)
        }
    }
}

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
                favoriteKeys = WorkspaceCodec.decode(preferences[Keys.favorites]),
                dockKeys = WorkspaceCodec.decode(preferences[Keys.dock]),
            )
        }
        .distinctUntilChanged()

    suspend fun ensureDefaults(favoriteKeys: List<String>, dockKeys: List<String>) {
        context.workspaceDataStore.edit { preferences ->
            if (preferences[Keys.initialized] == true) return@edit
            preferences[Keys.favorites] = WorkspaceCodec.encode(favoriteKeys.distinct())
            preferences[Keys.dock] = WorkspaceCodec.encode(
                dockKeys.distinct().take(WorkspaceCodec.MAX_DOCK_ITEMS)
            )
            preferences[Keys.initialized] = true
        }
    }

    suspend fun toggleFavorite(key: String) {
        context.workspaceDataStore.edit { preferences ->
            val current = WorkspaceCodec.decode(preferences[Keys.favorites])
            preferences[Keys.favorites] = WorkspaceCodec.encode(WorkspaceCodec.toggled(current, key))
            preferences[Keys.initialized] = true
        }
    }

    suspend fun toggleDock(key: String) {
        context.workspaceDataStore.edit { preferences ->
            val current = WorkspaceCodec.decode(preferences[Keys.dock])
            preferences[Keys.dock] = WorkspaceCodec.encode(
                WorkspaceCodec.toggled(current, key, WorkspaceCodec.MAX_DOCK_ITEMS)
            )
            preferences[Keys.initialized] = true
        }
    }

    suspend fun moveFavorite(key: String, direction: WorkspaceMoveDirection) {
        context.workspaceDataStore.edit { preferences ->
            val current = WorkspaceCodec.decode(preferences[Keys.favorites])
            preferences[Keys.favorites] = WorkspaceCodec.encode(
                WorkspaceCodec.moved(current, key, direction)
            )
            preferences[Keys.initialized] = true
        }
    }

    suspend fun moveDock(key: String, direction: WorkspaceMoveDirection) {
        context.workspaceDataStore.edit { preferences ->
            val current = WorkspaceCodec.decode(preferences[Keys.dock])
            preferences[Keys.dock] = WorkspaceCodec.encode(
                WorkspaceCodec.moved(current, key, direction)
            )
            preferences[Keys.initialized] = true
        }
    }
}
