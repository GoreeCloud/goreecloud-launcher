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
    SETTING,
    ACTION,
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

enum class LauncherSearchDestination {
    HOME,
    APPS,
    SETTINGS,
}

data class LauncherNavigateSearchAction(
    val destination: LauncherSearchDestination,
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

/**
 * Launcher-owned local actions that make Universal Search an action surface as well as a discovery
 * surface. These entries require no network access and do not transfer authority to another
 * GoreeCloud service.
 */
class LauncherCoreActionsSearchProvider : LauncherSearchProvider {
    override val id: String = PROVIDER_ID

    override fun search(rawQuery: String): List<LauncherSearchResult> =
        entries.mapNotNull { entry ->
            val score = LauncherSearchTextRanking.score(
                title = entry.title,
                subtitle = listOfNotNull(entry.subtitle, entry.searchTerms)
                    .joinToString(separator = " "),
                rawQuery = rawQuery,
            ) ?: return@mapNotNull null

            LauncherSearchResult(
                providerId = id,
                resultId = entry.id,
                title = entry.title,
                subtitle = entry.subtitle,
                category = entry.category,
                score = score + CORE_ACTION_SCORE_BIAS,
                action = LauncherNavigateSearchAction(entry.destination),
            )
        }

    private data class Entry(
        val id: String,
        val title: String,
        val subtitle: String?,
        val searchTerms: String,
        val category: LauncherSearchCategory,
        val destination: LauncherSearchDestination,
    )

    companion object {
        const val PROVIDER_ID = "launcher.core-actions"
        private const val CORE_ACTION_SCORE_BIAS = 25

        private val entries = listOf(
            Entry(
                id = "launcher-settings",
                title = "Launcher settings",
                subtitle = "Home, apps, dock, search and Glaze",
                searchTerms = "settings preferences customize configuration",
                category = LauncherSearchCategory.SETTING,
                destination = LauncherSearchDestination.SETTINGS,
            ),
            Entry(
                id = "apps",
                title = "Apps",
                subtitle = "Browse installed applications",
                searchTerms = "drawer applications installed browse",
                category = LauncherSearchCategory.ACTION,
                destination = LauncherSearchDestination.APPS,
            ),
            Entry(
                id = "home",
                title = "Home",
                subtitle = "Return to the primary Launcher surface",
                searchTerms = "launcher start workspace",
                category = LauncherSearchCategory.ACTION,
                destination = LauncherSearchDestination.HOME,
            ),
        )
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
 * Small deterministic local ranking policy used by the built-in Launcher providers.
 *
 * Blank queries preserve browse behavior. Non-blank queries prioritize exact title, title prefix,
 * title substring, then searchable metadata matches. This is intentionally local and
 * telemetry-free.
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
