package com.goreecloud.launcher.core.launcher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.launcherLocalUsageStore by preferencesDataStore(
    name = "launcher_local_usage",
)

/**
 * Minimal local-only launch-frequency store.
 *
 * Only an application workspace key and an aggregate launch count are retained. No timestamps,
 * queries, destinations, dwell time, network data, or cross-application behavior are collected.
 */
class LauncherLocalUsageRepository(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.launcherLocalUsageStore)

    private object Keys {
        val launchCounts = stringPreferencesKey("launch_counts_v1")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val launchCounts: Flow<Map<String, Long>> = dataStore.data
        .map { values -> LauncherLocalUsageCodec.decode(values[Keys.launchCounts]) }
        .distinctUntilChanged()

    fun recordLaunch(appKey: String): Job = scope.launch {
        if (appKey.isBlank()) return@launch
        dataStore.edit { values ->
            val counts = LauncherLocalUsageCodec
                .decode(values[Keys.launchCounts])
                .toMutableMap()
            counts[appKey] = (counts[appKey] ?: 0L)
                .coerceAtMost(Long.MAX_VALUE - 1L) + 1L
            values[Keys.launchCounts] = LauncherLocalUsageCodec.encode(
                counts.entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Long>> { it.value }
                            .thenBy { it.key },
                    )
                    .take(MAX_TRACKED_APPS)
                    .associate { it.key to it.value },
            )
        }
    }

    fun clear(): Job = scope.launch {
        dataStore.edit { values -> values.remove(Keys.launchCounts) }
    }

    companion object {
        private const val MAX_TRACKED_APPS = 256
    }
}

internal object LauncherLocalUsageCodec {
    private const val RECORD_SEPARATOR = "\u001E"
    private const val FIELD_SEPARATOR = "\u001F"

    fun encode(counts: Map<String, Long>): String =
        counts.entries
            .asSequence()
            .filter { (key, count) -> key.isNotBlank() && count > 0L }
            .sortedBy { it.key }
            .joinToString(RECORD_SEPARATOR) { (key, count) ->
                encodeKey(key) + FIELD_SEPARATOR + count
            }

    fun decode(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        val result = linkedMapOf<String, Long>()
        raw.split(RECORD_SEPARATOR).forEach { record ->
            val fields = record.split(FIELD_SEPARATOR)
            if (fields.size != 2) return@forEach
            val key = decodeKey(fields[0]) ?: return@forEach
            val count = fields[1].toLongOrNull()?.takeIf { it > 0L } ?: return@forEach
            result[key] = count
        }
        return result
    }

    private fun encodeKey(value: String): String =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeKey(value: String): String? {
        val decoded = runCatching {
            String(
                Base64.getUrlDecoder().decode(value),
                StandardCharsets.UTF_8,
            )
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null

        return decoded.takeIf { encodeKey(it) == value }
    }
}
