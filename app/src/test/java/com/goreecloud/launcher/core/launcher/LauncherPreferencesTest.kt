package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LauncherPreferencesTest {
    @Test
    fun sanitizedClampsGridDrawerAndIconScale() {
        val result = LauncherPreferences(
            homeColumns = 2,
            homeRows = 99,
            drawerColumns = 9,
            iconScale = 3f,
        ).sanitized()

        assertEquals(4, result.homeColumns)
        assertEquals(7, result.homeRows)
        assertEquals(6, result.drawerColumns)
        assertEquals(1.15f, result.iconScale, 0f)
    }

    @Test
    fun homeCapacityReflectsConfiguredGrid() {
        assertEquals(
            30,
            LauncherPreferences(homeColumns = 5, homeRows = 6).homeCapacity,
        )
    }

    @Test
    fun defaultsPreserveExistingVisibleIndexEntryAndUnlockedLayout() {
        val defaults = LauncherPreferences()

        assertFalse(defaults.layoutLocked)
        assertEquals(GoreeCloudIndexHomeMode.PERMANENT, defaults.indexHomeMode)
    }

    @Test
    fun indexHomeModeStorageDecodingFailsSafeToPermanent() {
        assertEquals(
            GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY,
            GoreeCloudIndexHomeMode.fromStorage("swipe_down_only"),
        )
        assertEquals(
            GoreeCloudIndexHomeMode.PERMANENT,
            GoreeCloudIndexHomeMode.fromStorage("unknown"),
        )
        assertEquals(
            GoreeCloudIndexHomeMode.PERMANENT,
            GoreeCloudIndexHomeMode.fromStorage(null),
        )
    }
}
