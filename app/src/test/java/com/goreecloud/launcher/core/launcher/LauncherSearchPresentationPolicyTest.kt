package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherSearchPresentationPolicyTest {
    @Test
    fun applicationMatchesAreSeparatedWithoutChangingRelativeRanking() {
        val rankedResults = listOf(
            result("setting-1", LauncherSearchCategory.SETTING, score = 500),
            result("app-1", LauncherSearchCategory.APPLICATION, score = 450),
            result("action-1", LauncherSearchCategory.ACTION, score = 400),
            result("app-2", LauncherSearchCategory.APPLICATION, score = 350),
            result("setting-2", LauncherSearchCategory.SETTING, score = 300),
        )

        val model = LauncherSearchPresentationPolicy.arrange(
            rawQuery = "settings",
            rankedResults = rankedResults,
            providerControls = controls(),
        )

        assertEquals(listOf("app-1", "app-2"), model.applicationResults.map { it.resultId })
        assertEquals(
            listOf("setting-1", "action-1", "setting-2"),
            model.actionAndSettingResults.map { it.resultId },
        )
        assertTrue(model.hasLocalResults)
    }

    @Test
    fun noLocalResultsReportsEmptyEvenWhenExplicitHandoffExists() {
        val controls = controls(
            LauncherSearchProviderControlOption(
                providerId = "remote",
                displayName = "Remote",
                invocationMode = LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
                defaultEnabled = false,
                privacySummary = "Network required · No query retention",
            ),
            enabledProviderIds = setOf("remote"),
        )

        val model = LauncherSearchPresentationPolicy.arrange(
            rawQuery = "weather",
            rankedResults = emptyList(),
            providerControls = controls,
        )

        assertFalse(model.hasLocalResults)
        assertTrue(model.hasExplicitHandoffProviders)
    }

    @Test
    fun explicitHandoffIncludesOnlyEnabledExplicitProvidersInConfiguredOrder() {
        val controls = controls(
            LauncherSearchProviderControlOption(
                providerId = "local",
                displayName = "Local",
                invocationMode = LauncherSearchProviderInvocationMode.AUTOMATIC_LOCAL,
                defaultEnabled = true,
                privacySummary = "Local only · No query retention",
            ),
            LauncherSearchProviderControlOption(
                providerId = "remote-b",
                displayName = "Remote B",
                invocationMode = LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
                defaultEnabled = false,
                privacySummary = "Network required · Consent required · No query retention",
            ),
            LauncherSearchProviderControlOption(
                providerId = "remote-a",
                displayName = "Remote A",
                invocationMode = LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
                defaultEnabled = false,
                privacySummary = "Network required · No query retention",
            ),
            enabledProviderIds = setOf("local", "remote-b", "remote-a"),
        )

        val providers = LauncherSearchPresentationPolicy.explicitHandoffProviders(
            rawQuery = "maps",
            providerControls = controls,
        )

        assertEquals(listOf("remote-b", "remote-a"), providers.map { it.providerId })
        assertEquals(listOf("Remote B", "Remote A"), providers.map { it.displayName })
    }

    @Test
    fun disabledExplicitProviderIsNotRenderedAsSearchWithOption() {
        val controls = controls(
            LauncherSearchProviderControlOption(
                providerId = "remote",
                displayName = "Remote",
                invocationMode = LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
                defaultEnabled = false,
                privacySummary = "Network required · No query retention",
            ),
            enabledProviderIds = emptySet(),
        )

        assertTrue(
            LauncherSearchPresentationPolicy.explicitHandoffProviders(
                rawQuery = "query",
                providerControls = controls,
            ).isEmpty(),
        )
    }

    @Test
    fun blankQueryNeverExposesExplicitHandoff() {
        val controls = controls(
            LauncherSearchProviderControlOption(
                providerId = "remote",
                displayName = "Remote",
                invocationMode = LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
                defaultEnabled = false,
                privacySummary = "Network required · No query retention",
            ),
            enabledProviderIds = setOf("remote"),
        )

        assertTrue(
            LauncherSearchPresentationPolicy.explicitHandoffProviders(
                rawQuery = "   ",
                providerControls = controls,
            ).isEmpty(),
        )
    }

    private fun controls(
        vararg options: LauncherSearchProviderControlOption,
        enabledProviderIds: Set<String> = emptySet(),
    ): LauncherSearchProviderControlState = LauncherSearchProviderControlState(
        orderedOptions = options.toList(),
        enabledProviderIds = enabledProviderIds,
    )

    private fun result(
        id: String,
        category: LauncherSearchCategory,
        score: Int,
    ): LauncherSearchResult = LauncherSearchResult(
        providerId = "test",
        resultId = id,
        title = id,
        subtitle = null,
        category = category,
        score = score,
        action = null,
    )
}
