package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherBuiltInWallpapersTest {
    @Test
    fun catalogContainsFourDistinctGoreeCloudWallpapers() {
        val wallpapers = LauncherBuiltInWallpapers.all

        assertTrue(wallpapers.size >= 4)
        assertEquals(wallpapers.size, wallpapers.map { it.id }.distinct().size)
        assertEquals(wallpapers.size, wallpapers.map { it.name }.distinct().size)
        assertTrue(wallpapers.all { it.name.startsWith("Glaze ") })
        assertTrue(wallpapers.all { it.description.isNotBlank() })
    }
}
