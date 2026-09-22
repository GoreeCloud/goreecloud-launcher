package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherSearchProviderUserControlPolicyTest {
    @Test
    fun localRetentionFreeFirstPartyProviderExecutesAutomaticallyByDefault() {
        val registration = registration(
            providerId = "goreecloud.local",
            provenance = LauncherSearchProviderProvenance.GOREECLOUD_FIRST_PARTY,
        )
        val catalog = LauncherSearchProviderContract.evaluate(listOf(registration))

        val state = LauncherSearchProviderUserControlPolicy.normalize(
            catalog = catalog,
            requestedEnabledProviderIds = null,
            requestedProviderOrder = emptyList(),
        )

        assertTrue(state.isEnabled("goreecloud.local"))
        assertEquals(
            LauncherSearchProviderInvocationMode.AUTOMATIC_LOCAL,
            state.orderedOptions.single().invocationMode,
        )
        assertEquals(listOf("goreecloud.local"), automaticProviderIds(catalog, state))
    }

    @Test
    fun thirdPartyProviderNeverReceivesAutomaticQueriesEvenWhenLocallyDescribed() {
        val registration = registration(
            providerId = "third.party",
            provenance = LauncherSearchProviderProvenance.THIRD_PARTY,
        )
        val catalog = LauncherSearchProviderContract.evaluate(listOf(registration))
        val state = LauncherSearchProviderUserControlPolicy.normalize(
            catalog = catalog,
            requestedEnabledProviderIds = setOf("third.party"),
            requestedProviderOrder = emptyList(),
        )

        assertTrue(state.isEnabled("third.party"))
        assertEquals(
            LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
            state.orderedOptions.single().invocationMode,
        )
        assertTrue(LauncherSearchProviderUserControlPolicy.automaticProviders(catalog, state).isEmpty())
    }

    @Test
    fun networkRemoteOrRetainingProviderDefaultsDisabledAndExplicitHandoff() {
        val network = registration(
            providerId = "network",
            offlineBehavior = LauncherSearchOfflineBehavior.NETWORK_REQUIRED,
        )
        val remote = registration(
            providerId = "remote",
            remoteProcessing = LauncherSearchRemoteProcessing.REQUIRED,
        )
        val retaining = registration(
            providerId = "retaining",
            queryRetention = LauncherSearchQueryRetention.SESSION_ONLY,
        )
        val catalog = LauncherSearchProviderContract.evaluate(listOf(network, remote, retaining))

        val state = LauncherSearchProviderUserControlPolicy.normalize(
            catalog = catalog,
            requestedEnabledProviderIds = null,
            requestedProviderOrder = emptyList(),
        )

        assertTrue(state.enabledProviderIds.isEmpty())
        state.orderedOptions.forEach { option ->
            assertFalse(option.defaultEnabled)
            assertEquals(
                LauncherSearchProviderInvocationMode.EXPLICIT_USER_HANDOFF,
                option.invocationMode,
            )
        }
        assertTrue(LauncherSearchProviderUserControlPolicy.automaticProviders(catalog, state).isEmpty())
    }

    @Test
    fun explicitEmptySelectionIsNotReplacedByDefaults() {
        val registration = registration(providerId = "local")
        val catalog = LauncherSearchProviderContract.evaluate(listOf(registration))

        val state = LauncherSearchProviderUserControlPolicy.normalize(
            catalog = catalog,
            requestedEnabledProviderIds = emptySet(),
            requestedProviderOrder = emptyList(),
        )

        assertTrue(state.enabledProviderIds.isEmpty())
    }

    @Test
    fun requestedOrderDropsUnknownAndDuplicatesThenAppendsNewProviders() {
        val first = registration(providerId = "first")
        val second = registration(providerId = "second")
        val third = registration(providerId = "third")
        val catalog = LauncherSearchProviderContract.evaluate(listOf(first, second, third))

        val state = LauncherSearchProviderUserControlPolicy.normalize(
            catalog = catalog,
            requestedEnabledProviderIds = setOf("second", "unknown"),
            requestedProviderOrder = listOf("second", "unknown", "second", "first"),
        )

        assertEquals(
            listOf("second", "first", "third"),
            state.orderedOptions.map { option -> option.providerId },
        )
        assertEquals(setOf("second"), state.enabledProviderIds)
    }

    @Test
    fun builtInNamesAndPrivacySummaryAreUserReadable() {
        val apps = metadata(LauncherInstalledAppsSearchProvider.PROVIDER_ID)
        val actions = metadata(LauncherCoreActionsSearchProvider.PROVIDER_ID)

        assertEquals("Apps", LauncherSearchProviderUserControlPolicy.displayNameFor(apps.providerId))
        assertEquals(
            "Launcher actions",
            LauncherSearchProviderUserControlPolicy.displayNameFor(actions.providerId),
        )
        assertEquals(
            "Local only · No query retention",
            LauncherSearchProviderUserControlPolicy.privacySummaryFor(apps),
        )
    }

    @Test
    fun privacySummarySurfacesNetworkConsentAndRetention() {
        val providerMetadata = metadata(
            providerId = "remote",
            offlineBehavior = LauncherSearchOfflineBehavior.NETWORK_REQUIRED,
            authorizationRequirement = LauncherSearchAuthorizationRequirement.USER_CONSENT,
            remoteProcessing = LauncherSearchRemoteProcessing.REQUIRED,
            queryRetention = LauncherSearchQueryRetention.PERSISTENT,
        )

        assertEquals(
            "Network required · Remote processing · Consent required · Persistent query retention",
            LauncherSearchProviderUserControlPolicy.privacySummaryFor(providerMetadata),
        )
    }

    private fun automaticProviderIds(
        catalog: LauncherSearchProviderCatalog,
        state: LauncherSearchProviderControlState,
    ): List<String> = LauncherSearchProviderUserControlPolicy
        .automaticProviders(catalog, state)
        .map { provider -> provider.id }

    private fun registration(
        providerId: String,
        provenance: LauncherSearchProviderProvenance = LauncherSearchProviderProvenance.GOREECLOUD_FIRST_PARTY,
        offlineBehavior: LauncherSearchOfflineBehavior = LauncherSearchOfflineBehavior.LOCAL_ONLY,
        authorizationRequirement: LauncherSearchAuthorizationRequirement = LauncherSearchAuthorizationRequirement.NONE,
        remoteProcessing: LauncherSearchRemoteProcessing = LauncherSearchRemoteProcessing.NONE,
        queryRetention: LauncherSearchQueryRetention = LauncherSearchQueryRetention.NONE,
    ): LauncherSearchProviderRegistration {
        val provider = object : LauncherSearchProvider {
            override val id: String = providerId
            override fun search(rawQuery: String): List<LauncherSearchResult> = emptyList()
        }
        return LauncherSearchProviderRegistration(
            provider = provider,
            metadata = metadata(
                providerId = providerId,
                provenance = provenance,
                offlineBehavior = offlineBehavior,
                authorizationRequirement = authorizationRequirement,
                remoteProcessing = remoteProcessing,
                queryRetention = queryRetention,
            ),
        )
    }

    private fun metadata(
        providerId: String,
        provenance: LauncherSearchProviderProvenance = LauncherSearchProviderProvenance.LAUNCHER_BUILT_IN,
        offlineBehavior: LauncherSearchOfflineBehavior = LauncherSearchOfflineBehavior.LOCAL_ONLY,
        authorizationRequirement: LauncherSearchAuthorizationRequirement = LauncherSearchAuthorizationRequirement.NONE,
        remoteProcessing: LauncherSearchRemoteProcessing = LauncherSearchRemoteProcessing.NONE,
        queryRetention: LauncherSearchQueryRetention = LauncherSearchQueryRetention.NONE,
    ): LauncherSearchProviderMetadata = LauncherSearchProviderMetadata(
        providerId = providerId,
        contractVersion = LauncherSearchProviderContract.currentVersion,
        provenance = provenance,
        offlineBehavior = offlineBehavior,
        authorizationRequirement = authorizationRequirement,
        remoteProcessing = remoteProcessing,
        queryRetention = queryRetention,
    )
}
