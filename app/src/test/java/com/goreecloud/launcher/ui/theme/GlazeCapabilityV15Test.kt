package com.goreecloud.launcher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeCapabilityV15Test {
    private fun capability(
        id: String,
        state: GlazeCapabilityV15.CapabilityState,
        authority: String = "application",
    ) = GlazeCapabilityV15.Capability(id, state, authority)

    @Test
    fun `launcher targets exact GLAZE UI v1_5_0 Stable authority`() {
        assertEquals("1.5.0", GlazeCapabilityV15.targetVersion)
        assertEquals(
            "b7fa8164bfdeaa1dc0acb21b770e7601120da04e",
            GlazeCapabilityV15.stableSourceRevision,
        )
        assertEquals(
            "ee1032a0822ab8e103f8afe48e5c1859fde65cc9",
            GlazeCapabilityV15.reviewedImplementationAnchor,
        )
        assertEquals("1.4.1", GlazeCapabilityV15.opticalBaselineVersion)
        assertEquals("1.4.1", GlazeCapabilityV15.rollbackVersion)
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

        assertTrue(result.enabled)
        assertEquals(GlazeCapabilityV15.CapabilityState.AVAILABLE, result.state)
        assertFalse(result.automaticExecutionAllowed)
        assertFalse(result.authorityInferred)
        assertFalse(result.providerPrecedenceInferred)
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

        assertTrue(localLaunch.enabled)
        assertFalse(indexSearch.enabled)
        assertEquals(GlazeCapabilityV15.CapabilityState.UNKNOWN, indexSearch.state)
        assertTrue(indexSearch.reasonCodes.contains("capability-unknown:service.index-search"))
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

        assertFalse(result.enabled)
        assertEquals(GlazeCapabilityV15.CapabilityState.CONFLICT, result.state)
        assertTrue(result.reasonCodes.contains("capability-conflict:service.index-search"))
        assertFalse(result.providerPrecedenceInferred)
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

        assertFalse(result.enabled)
        assertEquals(GlazeCapabilityV15.CapabilityState.RESTRICTED, result.state)
        assertTrue(result.reasonCodes.contains("restricted-by-authority:authorization.workspace-edit"))
        assertFalse(result.automaticExecutionAllowed)
        assertFalse(result.authorityInferred)
    }
}
