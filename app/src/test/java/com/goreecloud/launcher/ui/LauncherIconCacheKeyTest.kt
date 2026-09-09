package com.goreecloud.launcher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherIconCacheKeyTest {
    @Test
    fun sameInventoryAndWorkspaceReuseOneCacheKey() {
        val inventory = Any()

        assertEquals(
            LauncherIconCacheKey("10:com.example/.Main", inventory),
            LauncherIconCacheKey("10:com.example/.Main", inventory),
        )
    }

    @Test
    fun newInventorySnapshotInvalidatesSameWorkspaceIdentity() {
        val firstInventory = Any()
        val nextInventory = Any()

        assertNotEquals(
            LauncherIconCacheKey("10:com.example/.Main", firstInventory),
            LauncherIconCacheKey("10:com.example/.Main", nextInventory),
        )
    }

    @Test
    fun differentProfilesOrComponentsRemainDistinct() {
        val inventory = Any()

        assertNotEquals(
            LauncherIconCacheKey("10:com.example/.Main", inventory),
            LauncherIconCacheKey("11:com.example/.Main", inventory),
        )
        assertNotEquals(
            LauncherIconCacheKey("10:com.example/.Main", inventory),
            LauncherIconCacheKey("10:com.example/.Alternate", inventory),
        )
    }

    @Test
    fun cacheAndRasterBoundsStayExplicit() {
        assertEquals(144, LauncherIconCache.ICON_RASTER_SIZE_PX)
        assertTrue(LauncherIconCache.MAX_CACHE_KILOBYTES <= 8 * 1024)
    }
}
