package com.goreecloud.launcher.core.launcher

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherIconSingleFlightLoaderTest {
    @Test
    fun concurrentWaitersShareOneDecode() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val loader = LauncherIconSingleFlightLoader<String, String>(scope)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val calls = AtomicInteger(0)

        val first = async {
            loader.load("same") {
                calls.incrementAndGet()
                started.complete(Unit)
                release.await()
                "icon"
            }
        }
        started.await()
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            loader.load("same") {
                calls.incrementAndGet()
                "unexpected-second-decode"
            }
        }

        release.complete(Unit)

        assertEquals("icon", first.await())
        assertEquals("icon", second.await())
        assertEquals(1, calls.get())
        scope.cancel()
    }

    @Test
    fun latestPreloadReplacementCancelsSupersededTail() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val runner = LauncherLatestPreloadRunner(scope)
        val firstStarted = CompletableDeferred<Unit>()
        val firstCancelled = CompletableDeferred<Unit>()
        val neverRelease = CompletableDeferred<Unit>()
        val replacementCompleted = CompletableDeferred<Unit>()
        val supersededTailCalls = AtomicInteger(0)

        runner.replace {
            firstStarted.complete(Unit)
            try {
                neverRelease.await()
                supersededTailCalls.incrementAndGet()
            } finally {
                firstCancelled.complete(Unit)
            }
        }
        firstStarted.await()

        runner.replace {
            replacementCompleted.complete(Unit)
        }

        firstCancelled.await()
        replacementCompleted.await()

        assertEquals(0, supersededTailCalls.get())
        scope.cancel()
    }
}
