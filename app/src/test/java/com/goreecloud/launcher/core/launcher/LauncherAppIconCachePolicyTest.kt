package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherAppIconCachePolicyTest {
    @Test
    fun sharedDecodeSizeIsBoundedAndReusableAcrossLauncherSurfaces() {
        assertEquals(144, LAUNCHER_ICON_DECODE_SIZE_PX)
        assertTrue(LAUNCHER_ICON_DECODE_SIZE_PX in 96..192)
    }

    @Test
    fun processIconCacheHasExplicitMemoryCeiling() {
        assertEquals(8 * 1024, LAUNCHER_ICON_CACHE_MAX_KIB)
        assertTrue(LAUNCHER_ICON_CACHE_MAX_KIB <= 16 * 1024)
    }
}
