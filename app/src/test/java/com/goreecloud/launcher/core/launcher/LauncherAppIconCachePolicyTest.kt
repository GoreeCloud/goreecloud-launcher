package com.goreecloud.launcher.core.launcher

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    @Test
    fun cancellingFirstWaiterDoesNotCancelSharedSingleFlightLoad() = runBlocking {
        val sharedScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val loader = LauncherIconSingleFlightLoader<String, Int>(sharedScope)
        val loadStarted = CompletableDeferred<Unit>()
        val allowLoadToFinish = CompletableDeferred<Unit>()
        val decodeCount = AtomicInteger(0)

        try {
            val firstWaiter = launch {
                loader.load("same-icon") {
                    decodeCount.incrementAndGet()
                    loadStarted.complete(Unit)
                    allowLoadToFinish.await()
                    42
                }
            }

            loadStarted.await()

            val secondWaiter = async {
                loader.load("same-icon") {
                    error("same-generation follower must not start a duplicate decode")
                }
            }

            firstWaiter.cancelAndJoin()
            allowLoadToFinish.complete(Unit)

            assertEquals(42, secondWaiter.await())
            assertEquals(1, decodeCount.get())
        } finally {
            sharedScope.cancel()
        }
    }
}
