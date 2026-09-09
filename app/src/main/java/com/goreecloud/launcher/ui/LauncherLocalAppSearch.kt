package com.goreecloud.launcher.ui

import java.util.Locale

/**
 * Launcher-owned matching for the local installed-app inventory.
 *
 * This deliberately contains no GoreeCloud Index provider/ranking logic. Extended GoreeCloud
 * results remain an explicit handoff to Index after Launcher has presented its local results.
 */
internal fun matchesLauncherAppSearch(
    label: String,
    packageName: String,
    className: String,
    query: String,
): Boolean {
    val terms = query
        .trim()
        .lowercase(Locale.ROOT)
        .split(Regex("\\s+"))
        .filter(String::isNotEmpty)
    if (terms.isEmpty()) return true

    val searchable = buildString {
        append(label.lowercase(Locale.ROOT))
        append(' ')
        append(packageName.lowercase(Locale.ROOT))
        append(' ')
        append(className.lowercase(Locale.ROOT))
    }
    return terms.all(searchable::contains)
}
