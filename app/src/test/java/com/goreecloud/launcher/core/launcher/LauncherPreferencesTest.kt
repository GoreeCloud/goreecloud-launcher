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
    fun defaultsKeepHomeVisuallyQuietAndLayoutUnlocked() {
        val defaults = LauncherPreferences()

        assertFalse(defaults.layoutLocked)
        assertEquals(GoreeCloudIndexHomeMode.SWIPE_DOWN_ONLY, defaults.indexHomeMode)
    }

    @Test
    fun experienceDefaultsFavorLauncherLikeHomeWithoutDashboardActions() {
        val defaults = LauncherExperiencePreferences()

        assertFalse(defaults.showHomeQuickActions)
        assertEquals(true, defaults.showHomePageIndicator)
        assertFalse(defaults.starterLayoutApplied)
        assertEquals(LauncherDockStyle.GLASS, defaults.dockStyle)
        assertEquals(LauncherWallpaperShade.SOFT, defaults.wallpaperShade)
        assertEquals(LauncherDrawerSearchPlacement.BOTTOM, defaults.drawerSearchPlacement)
        assertEquals(LauncherDrawerNavigation.PAGES, defaults.drawerNavigation)
        assertEquals(LauncherDrawerEntryMode.BROWSE, defaults.drawerEntryMode)
        assertEquals(LauncherDrawerSpacing.STANDARD, defaults.drawerSpacing)
        assertEquals(5, defaults.drawerPageRows)
        assertEquals(LauncherHomeGlanceAlignment.LEFT, defaults.homeGlanceAlignment)
        assertEquals(LauncherHomeSearchPlacement.BOTTOM, defaults.homeSearchPlacement)
        assertEquals(LauncherHomeSearchStyle.GLASS, defaults.homeSearchStyle)
        assertEquals(LauncherHomeSpacing.BALANCED, defaults.homeSpacing)
    }

    @Test
    fun visualPreferenceStorageDecodingFailsSafe() {
        assertEquals(LauncherDockStyle.CLEAR, LauncherDockStyle.fromStorage("clear"))
        assertEquals(LauncherDockStyle.EDGE, LauncherDockStyle.fromStorage("edge"))
        assertEquals(LauncherDockStyle.GLASS, LauncherDockStyle.fromStorage("unknown"))
        assertEquals(LauncherWallpaperShade.STRONG, LauncherWallpaperShade.fromStorage("strong"))
        assertEquals(LauncherWallpaperShade.SOFT, LauncherWallpaperShade.fromStorage(null))
        assertEquals(
            LauncherDrawerSearchPlacement.TOP,
            LauncherDrawerSearchPlacement.fromStorage("top"),
        )
        assertEquals(
            LauncherDrawerSearchPlacement.BOTTOM,
            LauncherDrawerSearchPlacement.fromStorage("unknown"),
        )
        assertEquals(LauncherDrawerNavigation.SCROLL, LauncherDrawerNavigation.fromStorage("scroll"))
        assertEquals(LauncherDrawerNavigation.PAGES, LauncherDrawerNavigation.fromStorage("unknown"))
        assertEquals(
            LauncherDrawerEntryMode.SEARCH_FIRST,
            LauncherDrawerEntryMode.fromStorage("search_first"),
        )
        assertEquals(
            LauncherDrawerEntryMode.BROWSE,
            LauncherDrawerEntryMode.fromStorage("unknown"),
        )
        assertEquals(LauncherDrawerSpacing.TIGHT, LauncherDrawerSpacing.fromStorage("tight"))
        assertEquals(LauncherDrawerSpacing.RELAXED, LauncherDrawerSpacing.fromStorage("relaxed"))
        assertEquals(LauncherDrawerSpacing.STANDARD, LauncherDrawerSpacing.fromStorage("unknown"))
        assertEquals(
            LauncherHomeGlanceAlignment.CENTER,
            LauncherHomeGlanceAlignment.fromStorage("center"),
        )
        assertEquals(
            LauncherHomeGlanceAlignment.LEFT,
            LauncherHomeGlanceAlignment.fromStorage("unknown"),
        )
        assertEquals(
            LauncherHomeSearchPlacement.TOP,
            LauncherHomeSearchPlacement.fromStorage("top"),
        )
        assertEquals(
            LauncherHomeSearchPlacement.BOTTOM,
            LauncherHomeSearchPlacement.fromStorage("unknown"),
        )
        assertEquals(
            LauncherHomeSearchStyle.CLEAR,
            LauncherHomeSearchStyle.fromStorage("clear"),
        )
        assertEquals(
            LauncherHomeSearchStyle.SOLID,
            LauncherHomeSearchStyle.fromStorage("solid"),
        )
        assertEquals(
            LauncherHomeSearchStyle.GLASS,
            LauncherHomeSearchStyle.fromStorage("unknown"),
        )
        assertEquals(LauncherHomeSpacing.COMPACT, LauncherHomeSpacing.fromStorage("compact"))
        assertEquals(LauncherHomeSpacing.AIRY, LauncherHomeSpacing.fromStorage("airy"))
        assertEquals(LauncherHomeSpacing.BALANCED, LauncherHomeSpacing.fromStorage("unknown"))
    }

    @Test
    fun drawerLayoutModeStorageDecodingFailsSafeToGrid() {
        assertEquals(LauncherDrawerLayoutMode.COMPACT, LauncherDrawerLayoutMode.fromStorage("compact"))
        assertEquals(LauncherDrawerLayoutMode.LIST, LauncherDrawerLayoutMode.fromStorage("list"))
        assertEquals(LauncherDrawerLayoutMode.CATEGORY, LauncherDrawerLayoutMode.fromStorage("category"))
        assertEquals(LauncherDrawerLayoutMode.GRID, LauncherDrawerLayoutMode.fromStorage("unknown"))
        assertEquals(LauncherDrawerLayoutMode.GRID, LauncherDrawerLayoutMode.fromStorage(null))
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
