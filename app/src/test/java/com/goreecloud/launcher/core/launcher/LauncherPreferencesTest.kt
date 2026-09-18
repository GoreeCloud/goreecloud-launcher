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

    @Test
    fun drawerLayoutModeStorageDecodingFailsSafeToGrid() {
        assertEquals(
            LauncherDrawerLayoutMode.COMPACT,
            LauncherDrawerLayoutMode.fromStorage("compact"),
        )
        assertEquals(
            LauncherDrawerLayoutMode.LIST,
            LauncherDrawerLayoutMode.fromStorage("list"),
        )
        assertEquals(
            LauncherDrawerLayoutMode.GRID,
            LauncherDrawerLayoutMode.fromStorage("unknown"),
        )
        assertEquals(
            LauncherDrawerLayoutMode.GRID,
            LauncherDrawerLayoutMode.fromStorage(null),
        )
    }

    @Test
    fun drawerSortModeStorageDecodingFailsSafeToNameAscending() {
        assertEquals(
            LauncherDrawerSortMode.NAME_DESC,
            LauncherDrawerSortMode.fromStorage("name_desc"),
        )
        assertEquals(
            LauncherDrawerSortMode.NAME_ASC,
            LauncherDrawerSortMode.fromStorage("unknown"),
        )
        assertEquals(
            LauncherDrawerSortMode.NAME_ASC,
            LauncherDrawerSortMode.fromStorage(null),
        )
    }

    @Test
    fun drawerSearchPositionStorageDecodingFailsSafeToTop() {
        assertEquals(
            LauncherDrawerSearchPosition.BOTTOM,
            LauncherDrawerSearchPosition.fromStorage("bottom"),
        )
        assertEquals(
            LauncherDrawerSearchPosition.TOP,
            LauncherDrawerSearchPosition.fromStorage("unknown"),
        )
        assertEquals(
            LauncherDrawerSearchPosition.TOP,
            LauncherDrawerSearchPosition.fromStorage(null),
        )
    }
}
