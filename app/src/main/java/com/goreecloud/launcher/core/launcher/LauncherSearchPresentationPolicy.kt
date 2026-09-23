package com.goreecloud.launcher.core.launcher

/**
 * UI-facing structure for Launcher Universal Search results.
 *
 * The presentation model intentionally keeps ranking authority in [LauncherUniversalSearch]. It
 * only separates already-ranked local results into stable visual groups so direct application
 * matches can be visually prominent without changing provider scores or inventing a second ranking
 * algorithm in Compose.
 */
data class LauncherSearchPresentationModel(
    val applicationResults: List<LauncherSearchResult>,
    val actionAndSettingResults: List<LauncherSearchResult>,
    val explicitHandoffProviders: List<LauncherSearchExplicitHandoffProvider>,
) {
    val hasLocalResults: Boolean
        get() = applicationResults.isNotEmpty() || actionAndSettingResults.isNotEmpty()

    val hasExplicitHandoffProviders: Boolean
        get() = explicitHandoffProviders.isNotEmpty()
}

/**
 * Descriptive entry for a future explicit `Search with` surface.
 *
 * This model contains no query and performs no invocation. It is safe to render before external
 * provider execution exists because selecting a provider must remain a separate explicit action.
 */
data class LauncherSearchExplicitHandoffProvider(
    val providerId: String,
    val displayName: String,
    val privacySummary: String,
)

object LauncherSearchPresentationPolicy {
    /**
     * Groups an already-ranked result stream for rendering.
     *
     * Relative order inside each group is preserved exactly. Applications are surfaced separately;
     * settings and Launcher actions share a structured secondary group. No result is re-scored.
     */
    fun arrange(
        rawQuery: String,
        rankedResults: List<LauncherSearchResult>,
        providerControls: LauncherSearchProviderControlState,
    ): LauncherSearchPresentationModel {
        val applications = mutableListOf<LauncherSearchResult>()
        val actionsAndSettings = mutableListOf<LauncherSearchResult>()

        rankedResults.forEach { result ->
            when (result.category) {
                LauncherSearchCategory.APPLICATION -> applications += result
                LauncherSearchCategory.SHORTCUT,
                LauncherSearchCategory.CONTACT,
                LauncherSearchCategory.CALL_HISTORY,
                LauncherSearchCategory.MESSAGE,
                LauncherSearchCategory.FILE,
                LauncherSearchCategory.CONNECTED_SOURCE,
                LauncherSearchCategory.SETTING,
                LauncherSearchCategory.ACTION,
                -> actionsAndSettings += result
            }
        }

        return LauncherSearchPresentationModel(
            applicationResults = applications,
            actionAndSettingResults = actionsAndSettings,
            explicitHandoffProviders = explicitHandoffProviders(
                rawQuery = rawQuery,
                providerControls = providerControls,
            ),
        )
    }

    /**
     * Returns only user-enabled providers that are intentionally excluded from automatic query
     * fan-out by [LauncherSearchProviderUserControlPolicy].
     *
     * A blank query never exposes a handoff action. This method does not carry or dispatch the query;
     * the future invocation layer must require an explicit user action and re-check provider trust.
     */
    fun explicitHandoffProviders(
        rawQuery: String,
        providerControls: LauncherSearchProviderControlState,
    ): List<LauncherSearchExplicitHandoffProvider> {
        if (rawQuery.isBlank()) return emptyList()

        return providerControls.orderedOptions.mapNotNull { option ->
            if (
                !providerControls.isEnabled(option.providerId) ||
                option.invocationMode != LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF
            ) {
                return@mapNotNull null
            }

            LauncherSearchExplicitHandoffProvider(
                providerId = option.providerId,
                displayName = option.displayName,
                privacySummary = option.privacySummary,
            )
        }
    }
}
