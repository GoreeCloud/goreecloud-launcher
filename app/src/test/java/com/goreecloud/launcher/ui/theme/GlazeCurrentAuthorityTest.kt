package com.goreecloud.launcher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeCurrentAuthorityTest {
    @Test
    fun `current shared Glaze authority is exact V1_6 Stable release`() {
        assertEquals("1.6.0", GlazeCurrentAuthority.currentRequiredVersion)
        assertEquals(
            "a7180679ea851389e0f3004515f9a25f420e716d",
            GlazeCurrentAuthority.currentStableSourceRevision,
        )
    }

    @Test
    fun `retained V1_1 implementation remains truthfully migration required`() {
        assertEquals("1.1.0", GlazeCurrentAuthority.implementedBaselineVersion)
        assertEquals(
            "15cc76d2bcd4065552dc31c77145b63f34d9e7b2",
            GlazeCurrentAuthority.implementedBaselineSourceRevision,
        )
        assertFalse(GlazeCurrentAuthority.currentConsumerConformanceEstablished)
        assertTrue(GlazeCurrentAuthority.migrationRequired())
    }
}
