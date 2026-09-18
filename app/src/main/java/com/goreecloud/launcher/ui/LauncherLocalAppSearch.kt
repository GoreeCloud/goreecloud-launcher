package com.goreecloud.launcher.ui

import java.text.Normalizer
import java.util.Locale

/**
 * Launcher-owned matching for the local installed-app inventory.
 *
 * This deliberately contains no GoreeCloud Index provider or ranking logic. Extended GoreeCloud
 * results remain an explicit handoff to Index after Launcher has presented local installed apps.
 */
internal fun matchesLauncherAppSearch(
    label: String,
    packageName: String,
    className: String,
    query: String,
): Boolean {
    val terms = normalizeLauncherSearchText(query)
        .split(LAUNCHER_SEARCH_WHITESPACE)
        .filter(String::isNotEmpty)
        .distinct()
    if (terms.isEmpty()) return true

    val searchable = buildString {
        append(normalizeLauncherSearchText(label))
        append(' ')
        append(normalizeLauncherSearchText(packageName))
        append(' ')
        append(normalizeLauncherSearchText(className))
    }
    return terms.all(searchable::contains)
}

private fun normalizeLauncherSearchText(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFC).lowercase(Locale.ROOT)

private val LAUNCHER_SEARCH_WHITESPACE = Regex("[\\s\\p{Z}]+")
