package com.goreecloud.launcher.ui

import java.util.Locale

/**
 * Treat apps and folders as peers in the app drawer. Sorting must remain stable regardless
 * of a provider's item-list order or the work profile's separate application enumeration.
 * Only presentation order changes: folder membership and persisted Home positions are untouched.
 */
internal object LauncherDrawerSortingPolicy {
    fun <T> order(
        entries: List<T>,
        label: (T) -> String,
        key: (T) -> String,
    ): List<T> = entries.sortedWith(
        compareBy<T>(
            { label(it).lowercase(Locale.ROOT) },
            { key(it) },
        ),
    )
}
