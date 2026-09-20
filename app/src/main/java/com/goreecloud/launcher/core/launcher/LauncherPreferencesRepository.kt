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

enum class LauncherDrawerLayoutMode(val storageValue: String) {
    GRID("grid"),
    COMPACT("compact"),
    LIST("list");

    companion object {
        fun fromStorage(value: String?): LauncherDrawerLayoutMode =
            entries.firstOrNull { it.storageValue == value } ?: GRID
    }
}

enum class LauncherHomeCardStyle(val storageValue: String) {
    CLOCK("clock"),
    COMPACT("compact"),
    OFF("off");

    companion object {
        fun fromStorage(value: String?): LauncherHomeCardStyle =
            entries.firstOrNull { it.storageValue == value } ?: CLOCK
    }
}

enum class LauncherDrawerBackdrop(val storageValue: String) {
    GLASS("glass"),
    SOLID("solid");

    companion object {
        fun fromStorage(value: String?): LauncherDrawerBackdrop =
            entries.firstOrNull { it.storageValue == value } ?: GLASS
    }
}

enum class LauncherDrawerSearchPlacement(val storageValue: String) {
    TOP("top"),
    BOTTOM("bottom");

    companion object {
        fun fromStorage(value: String?): LauncherDrawerSearchPlacement =
            entries.firstOrNull { it.storageValue == value } ?: BOTTOM
    }
}

enum class LauncherDrawerNavigation(val storageValue: String) {
    SCROLL("scroll"),
    PAGES("pages");

    companion object {
        fun fromStorage(value: String?): LauncherDrawerNavigation =
            entries.firstOrNull { it.storageValue == value } ?: PAGES
    }
}

enum class LauncherHomeGlanceAlignment(val storageValue: String) {
    LEFT("left"),
    CENTER("center");

    companion object {
        fun fromStorage(value: String?): LauncherHomeGlanceAlignment =
            entries.firstOrNull { it.storageValue == value } ?: LEFT
    }
}

enum class LauncherHomeSearchPlacement(val storageValue: String) {
    TOP("top"),
    BOTTOM("bottom");

    companion object {
        fun fromStorage(value: String?): LauncherHomeSearchPlacement =
            entries.firstOrNull { it.storageValue == value } ?: BOTTOM
    }
}

enum class LauncherDockStyle(val storageValue: String) {
    GLASS("glass"),
    CLEAR("clear"),
    EDGE("edge");

    companion object {
        fun fromStorage(value: String?): LauncherDockStyle =
            entries.firstOrNull { it.storageValue == value } ?: GLASS
    }
}

enum class LauncherWallpaperShade(val storageValue: String) {
    OFF("off"),
    SOFT("soft"),
    STRONG("strong");

    companion object {
        fun fromStorage(value: String?): LauncherWallpaperShade =
            entries.firstOrNull { it.storageValue == value } ?: SOFT
    }
}

data class LauncherExperiencePreferences(
    val homeCardStyle: LauncherHomeCardStyle = LauncherHomeCardStyle.CLOCK,
    val showHomeQuickActions: Boolean = false,
    val showHomePageIndicator: Boolean = true,
    val drawerBackdrop: LauncherDrawerBackdrop = LauncherDrawerBackdrop.GLASS,
    val drawerSearchPlacement: LauncherDrawerSearchPlacement = LauncherDrawerSearchPlacement.BOTTOM,
    val drawerNavigation: LauncherDrawerNavigation = LauncherDrawerNavigation.PAGES,
    val drawerPageRows: Int = 5,
    val showDrawerAppCount: Boolean = false,
    val homeGlanceAlignment: LauncherHomeGlanceAlignment = LauncherHomeGlanceAlignment.LEFT,
    val homeSearchPlacement: LauncherHomeSearchPlacement = LauncherHomeSearchPlacement.BOTTOM,
    val dockStyle: LauncherDockStyle = LauncherDockStyle.GLASS,
    val wallpaperShade: LauncherWallpaperShade = LauncherWallpaperShade.SOFT,
    val starterLayoutApplied: Boolean = false,
)

data class LauncherPreferences(
    val homeColumns: Int = 4,
    val homeRows: Int = 5,
    val drawerColumns: Int = 5,
    val showLabels: Boolean = true,
    val iconScale: Float = 1.0f,
    val layoutLocked: Boolean = false,
    val indexHomeMode: GoreeCloudIndexHomeMode = GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
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
        val drawerLayoutMode = stringPreferencesKey("drawer_layout_mode")
        val homeCardStyle = stringPreferencesKey("home_card_style")
        val showHomeQuickActions = booleanPreferencesKey("show_home_quick_actions")
        val showHomePageIndicator = booleanPreferencesKey("show_home_page_indicator")
        val drawerBackdrop = stringPreferencesKey("drawer_backdrop")
        val drawerSearchPlacement = stringPreferencesKey("drawer_search_placement")
        val drawerNavigation = stringPreferencesKey("drawer_navigation")
        val drawerPageRows = intPreferencesKey("drawer_page_rows")
        val showDrawerAppCount = booleanPreferencesKey("show_drawer_app_count")
        val homeGlanceAlignment = stringPreferencesKey("home_glance_alignment")
        val homeSearchPlacement = stringPreferencesKey("home_search_placement")
        val dockStyle = stringPreferencesKey("dock_style")
        val wallpaperShade = stringPreferencesKey("wallpaper_shade")
        val starterLayoutApplied = booleanPreferencesKey("starter_layout_applied")
        val portableRestoreJournal = stringPreferencesKey("portable_restore_journal_v1")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val defaults = LauncherPreferences()

    val preferences: Flow<LauncherPreferences> = dataStore.data
        .map(::portablePreferencesFrom)
        .distinctUntilChanged()

    /**
     * Drawer presentation mode is intentionally stored outside the strict v1 portable preference
     * subset. Adding it here must not silently change the seven-field backup/recovery contract.
     */
    val drawerLayoutMode: Flow<LauncherDrawerLayoutMode> = dataStore.data
        .map { values -> LauncherDrawerLayoutMode.fromStorage(values[Keys.drawerLayoutMode]) }
        .distinctUntilChanged()

    /**
     * Launcher-owned visual preferences that intentionally remain outside the strict seven-field
     * v1 portable preference subset. These settings may evolve during Development without silently
     * changing backup/recovery compatibility.
     */
    val experiencePreferences: Flow<LauncherExperiencePreferences> = dataStore.data
        .map { values ->
            LauncherExperiencePreferences(
                homeCardStyle = LauncherHomeCardStyle.fromStorage(values[Keys.homeCardStyle]),
                showHomeQuickActions = values[Keys.showHomeQuickActions] ?: false,
                showHomePageIndicator = values[Keys.showHomePageIndicator] ?: true,
                drawerBackdrop = LauncherDrawerBackdrop.fromStorage(values[Keys.drawerBackdrop]),
                drawerSearchPlacement = LauncherDrawerSearchPlacement.fromStorage(values[Keys.drawerSearchPlacement]),
                drawerNavigation = LauncherDrawerNavigation.fromStorage(values[Keys.drawerNavigation]),
                drawerPageRows = (values[Keys.drawerPageRows] ?: 5).coerceIn(4, 6),
                showDrawerAppCount = values[Keys.showDrawerAppCount] ?: false,
                homeGlanceAlignment = LauncherHomeGlanceAlignment.fromStorage(values[Keys.homeGlanceAlignment]),
                homeSearchPlacement = LauncherHomeSearchPlacement.fromStorage(values[Keys.homeSearchPlacement]),
                dockStyle = LauncherDockStyle.fromStorage(values[Keys.dockStyle]),
                wallpaperShade = LauncherWallpaperShade.fromStorage(values[Keys.wallpaperShade]),
                starterLayoutApplied = values[Keys.starterLayoutApplied] ?: false,
            )
        }
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

    fun setDrawerLayoutMode(mode: LauncherDrawerLayoutMode) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.drawerLayoutMode] = mode.storageValue
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

    fun setHomeCardStyle(style: LauncherHomeCardStyle) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.homeCardStyle] = style.storageValue
            }
        }
    }

    fun setShowHomeQuickActions(show: Boolean) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.showHomeQuickActions] = show
            }
        }
    }

    fun setShowHomePageIndicator(show: Boolean) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.showHomePageIndicator] = show
            }
        }
    }

    fun setDrawerBackdrop(backdrop: LauncherDrawerBackdrop) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.drawerBackdrop] = backdrop.storageValue
            }
        }
    }

    fun setDrawerSearchPlacement(placement: LauncherDrawerSearchPlacement) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.drawerSearchPlacement] = placement.storageValue
            }
        }
    }

    fun setDrawerNavigation(navigation: LauncherDrawerNavigation) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.drawerNavigation] = navigation.storageValue
            }
        }
    }

    fun setDrawerPageRows(rows: Int) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.drawerPageRows] = rows.coerceIn(4, 6)
            }
        }
    }

    fun setHomeGlanceAlignment(alignment: LauncherHomeGlanceAlignment) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.homeGlanceAlignment] = alignment.storageValue
            }
        }
    }

    fun setShowDrawerAppCount(show: Boolean) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.showDrawerAppCount] = show
            }
        }
    }

    fun setHomeSearchPlacement(placement: LauncherHomeSearchPlacement) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.homeSearchPlacement] = placement.storageValue
            }
        }
    }

    fun setDockStyle(style: LauncherDockStyle) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.dockStyle] = style.storageValue
            }
        }
    }

    fun setWallpaperShade(shade: LauncherWallpaperShade) {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.wallpaperShade] = shade.storageValue
            }
        }
    }

    fun markStarterLayoutApplied() {
        scope.launch {
            dataStore.edit { values ->
                values[Keys.starterLayoutApplied] = true
            }
        }
    }

    suspend fun readPortablePreferences(): LauncherPreferences = preferences.first()

    /**
     * Strict recovery-only read of the seven persisted portable preferences.
     *
     * Ordinary UI reads intentionally sanitize legacy/out-of-range local values for resilience.
     * Recovery cannot use that behavior as evidence: an interrupted restore may be finalized or
     * cleared only when the raw persisted values are already inside the canonical portable domain.
     */
    suspend fun readPortablePreferencesForRecovery(): LauncherPortableRecoveryPreferenceReadResult {
        return when (val decoded = portableRecoveryPreferencesFrom(dataStore.data.first())) {
            is LauncherPortableStoredPreferencePolicy.DecodeResult.Success ->
                LauncherPortableRecoveryPreferenceReadResult.Success(decoded.preferences)
            is LauncherPortableStoredPreferencePolicy.DecodeResult.Invalid ->
                LauncherPortableRecoveryPreferenceReadResult.Invalid(decoded.reason)
        }
    }

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
     * Finalization is refused if either the journal changed or the raw current portable preferences
     * are not canonically equal to the journal's previous state, protecting both concurrent edits
     * and recovery from silently sanitized/corrupted persisted values.
     */
    suspend fun finalizePortableRestoreJournal(journal: LauncherPortableRestoreJournal): Boolean {
        val encoded = LauncherPortableRestoreJournalCodec.encode(journal)
        var finalized = false
        dataStore.edit { values ->
            val current = portableRecoveryPreferencesFrom(values)
            if (
                values[Keys.portableRestoreJournal] == encoded &&
                current is LauncherPortableStoredPreferencePolicy.DecodeResult.Success &&
                current.preferences == journal.previousPreferences
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

    private fun portableRecoveryPreferencesFrom(
        values: Preferences,
    ): LauncherPortableStoredPreferencePolicy.DecodeResult =
        LauncherPortableStoredPreferencePolicy.decode(
            stored = LauncherPortableStoredPreferences(
                homeColumns = values[Keys.homeColumns],
                homeRows = values[Keys.homeRows],
                drawerColumns = values[Keys.drawerColumns],
                showLabels = values[Keys.showLabels],
                iconScale = values[Keys.iconScale],
                layoutLocked = values[Keys.layoutLocked],
                indexHomeMode = values[Keys.indexHomeMode],
            ),
            defaults = defaults,
        )

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
