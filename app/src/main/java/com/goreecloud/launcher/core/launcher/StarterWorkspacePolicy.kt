package com.goreecloud.launcher.core.launcher

data class StarterWorkspaceCandidate(
    val key: String,
    val label: String,
    val packageName: String,
)

data class StarterWorkspaceSelection(
    val favoriteKeys: List<String>,
    val dockKeys: List<String>,
)

/**
 * Picks an intentional first-run launcher layout without pretending to know the user's final
 * preferences. The policy favors common phone tasks and GoreeCloud-branded apps, never launcher
 * packages, and is only applied once to a completely empty Development workspace.
 */
object StarterWorkspacePolicy {
    private val dockPriorityGroups = listOf(
        listOf("phone", "dialer"),
        listOf("messages", "messaging", "messenger"),
        listOf("browser", "chrome", "firefox", "internet"),
        listOf("camera"),
    )

    private val favoritePriorityGroups = listOf(
        listOf("calendar"),
        listOf("clock"),
        listOf("contacts"),
        listOf("gallery", "photos"),
        listOf("memos", "notes"),
        listOf("files", "file manager"),
        listOf("drive"),
        listOf("music"),
        listOf("app store", "store"),
    )

    fun select(
        candidates: List<StarterWorkspaceCandidate>,
        maxFavorites: Int = 8,
        maxDock: Int = 4,
    ): StarterWorkspaceSelection {
        val usable = candidates
            .filterNot { candidate ->
                candidate.packageName.contains("launcher", ignoreCase = true) ||
                    candidate.label.contains("launcher", ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<StarterWorkspaceCandidate> {
                    it.label.contains("goreecloud", ignoreCase = true)
                }.thenBy { it.label.lowercase() },
            )

        val used = linkedSetOf<String>()

        fun pick(groups: List<List<String>>, limit: Int): List<String> {
            val result = mutableListOf<String>()
            for (group in groups) {
                if (result.size >= limit) break
                val match = usable.firstOrNull { candidate ->
                    candidate.key !in used &&
                        group.any { keyword ->
                            candidate.label.contains(keyword, ignoreCase = true) ||
                                candidate.packageName.contains(keyword, ignoreCase = true)
                        }
                } ?: continue
                result += match.key
                used += match.key
            }
            return result
        }

        val dock = pick(dockPriorityGroups, maxDock).toMutableList()
        if (dock.size < maxDock) {
            usable.asSequence()
                .filter { it.key !in used }
                .take(maxDock - dock.size)
                .forEach {
                    dock += it.key
                    used += it.key
                }
        }

        val favorites = pick(favoritePriorityGroups, maxFavorites).toMutableList()
        if (favorites.size < maxFavorites) {
            usable.asSequence()
                .filter { it.key !in used }
                .take(maxFavorites - favorites.size)
                .forEach {
                    favorites += it.key
                    used += it.key
                }
        }

        return StarterWorkspaceSelection(
            favoriteKeys = favorites,
            dockKeys = dock,
        )
    }
}
