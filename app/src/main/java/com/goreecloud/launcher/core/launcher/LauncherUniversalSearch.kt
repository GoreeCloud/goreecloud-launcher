package com.goreecloud.launcher.core.launcher

import android.content.pm.LauncherActivityInfo
import java.text.Normalizer
import java.util.Locale

/**
 * Launcher-owned search provider contract.
 *
 * Providers return normalized result metadata and an optional action. A provider failure is
 * isolated by [LauncherUniversalSearch] so one optional source cannot disable core search.
 */
interface LauncherSearchProvider {
    val id: String

    fun search(rawQuery: String): List<LauncherSearchResult>
}

enum class LauncherSearchCategory {
    APPLICATION,
}

interface LauncherSearchAction

data class LauncherSearchResult(
    val providerId: String,
    val resultId: String,
    val title: String,
    val subtitle: String?,
    val category: LauncherSearchCategory,
    val score: Int,
    val action: LauncherSearchAction? = null,
)

data class LaunchApplicationSearchAction(
    val app: LauncherActivityInfo,
) : LauncherSearchAction

/**
 * First built-in Universal Search provider. Android LauncherApps remains application-inventory
 * authority; this provider only projects that authoritative inventory into Launcher search.
 */
class LauncherInstalledAppsSearchProvider(
    private val apps: List<LauncherActivityInfo>,
) : LauncherSearchProvider {
    override val id: String = PROVIDER_ID

    override fun search(rawQuery: String): List<LauncherSearchResult> =
        apps.mapNotNull { app ->
            val label = app.label.toString()
            val packageName = app.componentName.packageName
            val score = LauncherSearchTextRanking.score(
                title = label,
                subtitle = packageName,
                rawQuery = rawQuery,
            ) ?: return@mapNotNull null

            LauncherSearchResult(
                providerId = id,
                resultId = app.user.hashCode().toString() + ":" + app.componentName.flattenToString(),
                title = label,
                subtitle = packageName,
                category = LauncherSearchCategory.APPLICATION,
                score = score,
                action = LaunchApplicationSearchAction(app),
            )
        }

    companion object {
        const val PROVIDER_ID = "launcher.installed-apps"
    }
}

object LauncherUniversalSearch {
    fun search(
        rawQuery: String,
        providers: List<LauncherSearchProvider>,
    ): List<LauncherSearchResult> =
        providers
            .flatMap { provider ->
                runCatching { provider.search(rawQuery) }
                    .getOrDefault(emptyList())
            }
            .distinctBy { result -> result.providerId to result.resultId }
            .sortedWith(
                compareByDescending<LauncherSearchResult> { it.score }
                    .thenBy { LauncherSearchTextRanking.normalize(it.title) }
                    .thenBy { it.providerId }
                    .thenBy { it.resultId },
            )
}

/**
 * Small deterministic local ranking policy used by the built-in installed-app provider.
 *
 * Blank queries preserve browse behavior. Non-blank queries prioritize exact title, title prefix,
 * title substring, then package-name matches. This is intentionally local and telemetry-free.
 */
object LauncherSearchTextRanking {
    fun score(
        title: String,
        subtitle: String?,
        rawQuery: String,
    ): Int? {
        val query = normalize(rawQuery).trim()
        if (query.isEmpty()) return 0

        val normalizedTitle = normalize(title)
        val normalizedSubtitle = normalize(subtitle.orEmpty())

        return when {
            normalizedTitle == query -> 400
            normalizedTitle.startsWith(query) -> 300
            normalizedTitle.contains(query) -> 200
            normalizedSubtitle.contains(query) -> 100
            else -> null
        }
    }

    fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFC).lowercase(Locale.ROOT)
}
