package com.goreecloud.launcher.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GlazeCapabilityV15Test {
    private fun capability(
        id: String,
        state: GlazeCapabilityV15.CapabilityState,
        authority: String = "application",
    ) = GlazeCapabilityV15.Capability(id, state, authority)

    @Test
    fun `launcher targets exact GLAZE UI v1_5_0 Stable authority`() {
        assertThat(GlazeCapabilityV15.targetVersion).isEqualTo("1.5.0")
        assertThat(GlazeCapabilityV15.stableSourceRevision)
            .isEqualTo("b7fa8164bfdeaa1dc0acb21b770e7601120da04e")
        assertThat(GlazeCapabilityV15.reviewedImplementationAnchor)
            .isEqualTo("ee1032a0822ab8e103f8afe48e5c1859fde65cc9")
        assertThat(GlazeCapabilityV15.opticalBaselineVersion).isEqualTo("1.4.1")
        assertThat(GlazeCapabilityV15.rollbackVersion).isEqualTo("1.4.1")
    }

    @Test
    fun `available required capability enables presentation but never automatic execution`() {
        val result = GlazeCapabilityV15.resolveAction(
            GlazeCapabilityV15.ActionRequest(
                id = "launch-app",
                requiredCapabilityIds = setOf("application.launch"),
            ),
            listOf(capability("application.launch", GlazeCapabilityV15.CapabilityState.AVAILABLE)),
        )

        assertThat(result.enabled).isTrue()
        assertThat(result.state).isEqualTo(GlazeCapabilityV15.CapabilityState.AVAILABLE)
        assertThat(result.automaticExecutionAllowed).isFalse()
        assertThat(result.authorityInferred).isFalse()
        assertThat(result.providerPrecedenceInferred).isFalse()
    }

    @Test
    fun `missing capability fails closed without disabling unrelated local capability`() {
        val localLaunch = GlazeCapabilityV15.resolveAction(
            GlazeCapabilityV15.ActionRequest(
                id = "launch-app",
                requiredCapabilityIds = setOf("application.launch"),
            ),
            listOf(capability("application.launch", GlazeCapabilityV15.CapabilityState.AVAILABLE)),
        )
        val indexSearch = GlazeCapabilityV15.resolveAction(
            GlazeCapabilityV15.ActionRequest(
                id = "search-goreecloud",
                requiredCapabilityIds = setOf("service.index-search"),
            ),
            listOf(capability("application.launch", GlazeCapabilityV15.CapabilityState.AVAILABLE)),
        )

        assertThat(localLaunch.enabled).isTrue()
        assertThat(indexSearch.enabled).isFalse()
        assertThat(indexSearch.state).isEqualTo(GlazeCapabilityV15.CapabilityState.UNKNOWN)
        assertThat(indexSearch.reasonCodes).contains("capability-unknown:service.index-search")
    }

    @Test
    fun `duplicate capability ownership fails closed without provider precedence`() {
        val result = GlazeCapabilityV15.resolveAction(
            GlazeCapabilityV15.ActionRequest(
                id = "search-goreecloud",
                requiredCapabilityIds = setOf("service.index-search"),
            ),
            listOf(
                capability("service.index-search", GlazeCapabilityV15.CapabilityState.AVAILABLE, "service"),
                capability("service.index-search", GlazeCapabilityV15.CapabilityState.AVAILABLE, "service"),
            ),
        )

        assertThat(result.enabled).isFalse()
        assertThat(result.state).isEqualTo(GlazeCapabilityV15.CapabilityState.CONFLICT)
        assertThat(result.reasonCodes).contains("capability-conflict:service.index-search")
        assertThat(result.providerPrecedenceInferred).isFalse()
    }

    @Test
    fun `restricted consequential action remains disabled and cannot auto execute`() {
        val result = GlazeCapabilityV15.resolveAction(
            GlazeCapabilityV15.ActionRequest(
                id = "edit-home",
                requiredCapabilityIds = setOf("authorization.workspace-edit"),
                consequential = true,
            ),
            listOf(
                capability(
                    "authorization.workspace-edit",
                    GlazeCapabilityV15.CapabilityState.RESTRICTED,
                ),
            ),
        )

        assertThat(result.enabled).isFalse()
        assertThat(result.state).isEqualTo(GlazeCapabilityV15.CapabilityState.RESTRICTED)
        assertThat(result.reasonCodes).contains("restricted-by-authority:authorization.workspace-edit")
        assertThat(result.automaticExecutionAllowed).isFalse()
        assertThat(result.authorityInferred).isFalse()
    }
}
