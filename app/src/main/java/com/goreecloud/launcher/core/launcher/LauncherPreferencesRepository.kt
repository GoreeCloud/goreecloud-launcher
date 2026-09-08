package com.goreecloud.launcher.core.launcher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.launcherPreferencesStore by preferencesDataStore(name = "launcher_preferences")

enum class GoreeCloudIndexHomeMode(val storageValue: String) {
    PERMANENT("permanent"),
    SWIPE_DOWN_ONLY("swipe_down_only");

    companion object {
        fun fromStorage(value: String?): GoreeCloudIndexHomeMode =
            entries.firstOrNull { it.storageValue == value } ?: PERMANENT
    }
}

data class LauncherPreferences(
    val homeColumns: Int = 4,
    val homeRows: Int = 5,
    val drawerColumns: Int = 5,
    val showLabels: Boolean = true,
    val iconScale: Float = 1.0f,
    val layoutLocked: Boolean = false,
    val indexHomeMode: GoreeCloudIndexHomeMode = GoreeCloudIndexHomeMode.PERMANENT,
) {
    val homeCapacity: Int get() = homeColumns * homeRows

    fun sanitized(): LauncherPreferences = copy(
        homeColumns = homeColumns.coerceIn(4, 6),
        homeRows = homeRows.coerceIn(4, 7),
        drawerColumns = drawerColumns.coerceIn(4, 6),
        iconScale = iconScale.coerceIn(0.85f, 1.15f),
    )
}

class LauncherPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : LauncherPortablePreferenceWriter {
    constructor(context: Context) : this(context.launcherPreferencesStore)

    private object Keys {
        val homeColumns = intPreferencesKey("home_columns")
        val homeRows = intPreferencesKey("home_rows")
        val drawerColumns = intPreferencesKey("drawer_columns")
        val showLabels = booleanPreferencesKey("show_labels")
        val iconScale = floatPreferencesKey("icon_scale")
        val layoutLocked = booleanPreferencesKey("layout_locked")
        val indexHomeMode = stringPreferencesKey("index_home_mode")
        val portableRestoreJournal = stringPreferencesKey("portable_restore_journal_v1")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val defaults = LauncherPreferences()

    val preferences: Flow<LauncherPreferences> = dataStore.data
        .map(::portablePreferencesFrom)
        .distinctUntilChanged()

    fun setHomeGrid(columns: Int, rows: Int) {
        val normalized = LauncherPreferences(homeColumns = columns, homeRows = rows).sanitized()
        scope.launch {
            dataStore.edit { values ->
                values[Keys.homeColumns] = normalized.homeColumns
                values[Keys.homeRows] = normalized.homeRows
            }
        }
    }

    fun setDrawerColumns(columns: Int) {
        val normalized = columns.coerceIn(4, 6)
        scope.launch {
            dataStore.edit { values ->
                values[Keys.drawerColumns] = normalized
            }
        }
    }

    fun setShowLabels(show: Boolean) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.showLabels] = show
            }
        }
    }

    fun setIconScale(scale: Float) {
        val normalized = scale.coerceIn(0.85f, 1.15f)
        scope.launch {
            dataStore.edit { values ->
                values[Keys.iconScale] = normalized
            }
        }
    }

    fun setLayoutLocked(locked: Boolean) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.layoutLocked] = locked
            }
        }
    }

    fun setIndexHomeMode(mode: GoreeCloudIndexHomeMode) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.indexHomeMode] = mode.storageValue
            }
        }
    }

    suspend fun readPortablePreferences(): LauncherPreferences = preferences.first()

    suspend fun readPortableRestoreJournal(): LauncherPortableRestoreJournalReadResult {
        val raw = dataStore.data.first()[Keys.portableRestoreJournal]
            ?: return LauncherPortableRestoreJournalReadResult.Absent
        return when (val decoded = LauncherPortableRestoreJournalCodec.decode(raw)) {
            is LauncherPortableRestoreJournalCodec.DecodeResult.Success ->
                LauncherPortableRestoreJournalReadResult.Present(decoded.journal)
            is LauncherPortableRestoreJournalCodec.DecodeResult.Invalid ->
                LauncherPortableRestoreJournalReadResult.Invalid(decoded.reason)
        }
    }

    /**
     * Persist one recovery journal before Room mutation. Any pre-existing journal fails closed so a
     * new restore cannot hide unresolved recovery evidence from an earlier attempt.
     */
    suspend fun beginPortableRestoreJournal(journal: LauncherPortableRestoreJournal): Boolean {
        val encoded = LauncherPortableRestoreJournalCodec.encode(journal)
        var stored = false
        dataStore.edit { values ->
            if (values[Keys.portableRestoreJournal] == null) {
                values[Keys.portableRestoreJournal] = encoded
                stored = true
            }
        }
        return stored
    }

    /**
     * Atomically write the target portable preferences and clear the exact matching journal.
     *
     * Finalization is refused if either the journal changed or the current portable preferences no
     * longer equal the journal's previous state, protecting a concurrent preference edit.
     */
    suspend fun finalizePortableRestoreJournal(journal: LauncherPortableRestoreJournal): Boolean {
        val encoded = LauncherPortableRestoreJournalCodec.encode(journal)
        var finalized = false
        dataStore.edit { values ->
            if (
                values[Keys.portableRestoreJournal] == encoded &&
                portablePreferencesFrom(values) == journal.previousPreferences
            ) {
                writePortablePreferences(values, journal.targetPreferences)
                values.remove(Keys.portableRestoreJournal)
                finalized = true
            }
        }
        return finalized
    }

    /** Remove only the caller's exact journal. An absent journal is already a safe cleared state. */
    suspend fun clearPortableRestoreJournalIfMatches(
        journal: LauncherPortableRestoreJournal,
    ): Boolean {
        val encoded = LauncherPortableRestoreJournalCodec.encode(journal)
        var safe = false
        dataStore.edit { values ->
            when (values[Keys.portableRestoreJournal]) {
                null -> safe = true
                encoded -> {
                    values.remove(Keys.portableRestoreJournal)
                    safe = true
                }
                else -> safe = false
            }
        }
        return safe
    }

    /**
     * Replace the complete v1 portable preference subset in one DataStore transaction.
     *
     * The portable codec is reused as the defensive validation authority so this path never
     * silently clamps malformed external values through [LauncherPreferences.sanitized].
     */
    override suspend fun replacePortablePreferences(preferences: LauncherPreferences) {
        LauncherPortablePreferences.encode(preferences)
        dataStore.edit { values ->
            writePortablePreferences(values, preferences)
        }
    }

    /**
     * Compensate a failed Room/DataStore restore without overwriting a concurrent preference edit.
     *
     * The rollback is safe when the store still contains either the just-applied portable value or
     * the original value (for example when the failed DataStore edit never committed). Any third
     * state is treated as a concurrent change and is left untouched.
     */
    suspend fun rollbackPortablePreferencesAfterFailedApply(
        expectedApplied: LauncherPreferences,
        previous: LauncherPreferences,
    ): Boolean {
        LauncherPortablePreferences.encode(expectedApplied)
        LauncherPortablePreferences.encode(previous)

        var safe = false
        dataStore.edit { values ->
            when (portablePreferencesFrom(values)) {
                previous -> safe = true
                expectedApplied -> {
                    writePortablePreferences(values, previous)
                    safe = true
                }
                else -> safe = false
            }
        }
        return safe
    }

    private fun portablePreferencesFrom(values: Preferences): LauncherPreferences =
        LauncherPreferences(
            homeColumns = values[Keys.homeColumns] ?: defaults.homeColumns,
            homeRows = values[Keys.homeRows] ?: defaults.homeRows,
            drawerColumns = values[Keys.drawerColumns] ?: defaults.drawerColumns,
            showLabels = values[Keys.showLabels] ?: defaults.showLabels,
            iconScale = values[Keys.iconScale] ?: defaults.iconScale,
            layoutLocked = values[Keys.layoutLocked] ?: defaults.layoutLocked,
            indexHomeMode = GoreeCloudIndexHomeMode.fromStorage(values[Keys.indexHomeMode]),
        ).sanitized()

    private fun writePortablePreferences(
        values: MutablePreferences,
        preferences: LauncherPreferences,
    ) {
        values[Keys.homeColumns] = preferences.homeColumns
        values[Keys.homeRows] = preferences.homeRows
        values[Keys.drawerColumns] = preferences.drawerColumns
        values[Keys.showLabels] = preferences.showLabels
        values[Keys.iconScale] = preferences.iconScale
        values[Keys.layoutLocked] = preferences.layoutLocked
        values[Keys.indexHomeMode] = preferences.indexHomeMode.storageValue
    }
}
