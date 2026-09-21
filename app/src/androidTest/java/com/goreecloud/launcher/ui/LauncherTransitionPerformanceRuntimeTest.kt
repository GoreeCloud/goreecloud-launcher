package com.goreecloud.launcher.ui

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.view.FrameMetrics
import android.view.InputDevice
import android.view.MotionEvent
import android.view.Window
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.launcher.MainActivity
import kotlin.math.roundToInt
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherTransitionPerformanceRuntimeTest {
    @Test
    fun homeDrawerRoundTripsReportFrameTiming() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        var collector: FrameMetricsCollector? = null

        try {
            waitForLauncherReady(scenario)
            scenario.onActivity { activity ->
                collector = FrameMetricsCollector(activity.window)
            }
            val metrics = checkNotNull(collector)

            repeat(WARMUP_ROUND_TRIPS) {
                swipeAndWait(scenario, LauncherSurfaceMode.DRAWER)
                swipeAndWait(scenario, LauncherSurfaceMode.HOME)
            }

            val homeToDrawerSamples = mutableListOf<Long>()
            val drawerToHomeSamples = mutableListOf<Long>()

            repeat(MEASURED_ROUND_TRIPS) {
                metrics.start()
                swipeAndWait(scenario, LauncherSurfaceMode.DRAWER)
                settleTransition(instrumentation)
                val opening = metrics.stop()
                assertTrue("Home -> Apps must emit FrameMetrics samples", opening.isNotEmpty())
                homeToDrawerSamples += opening

                metrics.start()
                swipeAndWait(scenario, LauncherSurfaceMode.HOME)
                settleTransition(instrumentation)
                val closing = metrics.stop()
                assertTrue("Apps -> Home must emit FrameMetrics samples", closing.isNotEmpty())
                drawerToHomeSamples += closing
            }

            val openingStats = FrameTimingStats.from(homeToDrawerSamples)
            val closingStats = FrameTimingStats.from(drawerToHomeSamples)
            val report = buildString {
                append("home_to_apps=")
                append(openingStats)
                append("; apps_to_home=")
                append(closingStats)
                append("; note=diagnostic emulator evidence only; no release threshold or representative-device acceptance")
            }

            Log.i(TAG, report)
            instrumentation.sendStatus(
                0,
                Bundle().apply {
                    putString("launcher.transition.frame_metrics", report)
                },
            )
        } finally {
            collector?.let { activeCollector ->
                scenario.onActivity {
                    activeCollector.close()
                }
            }
            scenario.close()
        }
    }

    private fun waitForLauncherReady(scenario: ActivityScenario<MainActivity>) {
        val deadline = SystemClock.elapsedRealtime() + READY_TIMEOUT_MS
        while (SystemClock.elapsedRealtime() < deadline) {
            var windowReady = false
            scenario.onActivity { activity ->
                val decor = activity.window.decorView
                windowReady = decor.isLaidOut && decor.hasWindowFocus()
            }
            if (
                windowReady &&
                LauncherTransitionDiagnostics.observationCount > 0L &&
                LauncherTransitionDiagnostics.currentSurfaceMode == LauncherSurfaceMode.HOME
            ) {
                SystemClock.sleep(SETTLE_MS)
                return
            }
            SystemClock.sleep(POLL_MS)
        }
        throw AssertionError(
            "Launcher did not reach an observable focused Home surface within ${READY_TIMEOUT_MS}ms",
        )
    }

    private fun swipeAndWait(
        scenario: ActivityScenario<MainActivity>,
        target: LauncherSurfaceMode,
    ) {
        injectVerticalSwipe(
            scenario = scenario,
            upward = target == LauncherSurfaceMode.DRAWER,
        )
        val deadline = SystemClock.elapsedRealtime() + MODE_TIMEOUT_MS
        while (SystemClock.elapsedRealtime() < deadline) {
            if (LauncherTransitionDiagnostics.currentSurfaceMode == target) {
                return
            }
            SystemClock.sleep(POLL_MS)
        }
        throw AssertionError(
            "Launcher did not reach $target after the expected vertical gesture",
        )
    }

    private fun injectVerticalSwipe(
        scenario: ActivityScenario<MainActivity>,
        upward: Boolean,
    ) {
        var bounds: GestureBounds? = null
        scenario.onActivity { activity ->
            val decor = activity.window.decorView
            val location = IntArray(2)
            decor.getLocationOnScreen(location)
            bounds = GestureBounds(
                x = location[0] + (decor.width * 0.5f),
                top = location[1].toFloat(),
                height = decor.height.toFloat(),
            )
        }

        val target = checkNotNull(bounds)
        val startY = target.top + target.height * if (upward) 0.78f else 0.28f
        val endY = target.top + target.height * if (upward) 0.28f else 0.78f
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val downTime = SystemClock.uptimeMillis()

        injectTouch(automation, downTime, downTime, MotionEvent.ACTION_DOWN, target.x, startY)
        repeat(GESTURE_STEPS) { index ->
            val fraction = (index + 1).toFloat() / GESTURE_STEPS
            val eventTime = SystemClock.uptimeMillis()
            val y = startY + ((endY - startY) * fraction)
            injectTouch(automation, downTime, eventTime, MotionEvent.ACTION_MOVE, target.x, y)
            SystemClock.sleep(GESTURE_STEP_MS)
        }
        injectTouch(
            automation,
            downTime,
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_UP,
            target.x,
            endY,
        )
    }

    private fun injectTouch(
        automation: android.app.UiAutomation,
        downTime: Long,
        eventTime: Long,
        action: Int,
        x: Float,
        y: Float,
    ) {
        MotionEvent.obtain(downTime, eventTime, action, x, y, 0).also { event ->
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            try {
                check(automation.injectInputEvent(event, true)) {
                    "Failed to inject Launcher transition gesture event"
                }
            } finally {
                event.recycle()
            }
        }
    }

    private fun settleTransition(instrumentation: android.app.Instrumentation) {
        SystemClock.sleep(TRANSITION_SETTLE_MS)
        instrumentation.waitForIdleSync()
        SystemClock.sleep(FRAME_CALLBACK_DRAIN_MS)
    }

    private data class GestureBounds(
        val x: Float,
        val top: Float,
        val height: Float,
    )

    private data class FrameTimingStats(
        val frames: Int,
        val medianMs: Double,
        val p95Ms: Double,
        val maxMs: Double,
        val over16_67Ms: Int,
        val over33_34Ms: Int,
    ) {
        override fun toString(): String =
            "frames=$frames,medianMs=${medianMs.round(2)},p95Ms=${p95Ms.round(2)}," +
                "maxMs=${maxMs.round(2)},over16.67ms=$over16_67Ms,over33.34ms=$over33_34Ms"

        companion object {
            fun from(samplesNs: List<Long>): FrameTimingStats {
                require(samplesNs.isNotEmpty())
                val sorted = samplesNs.sorted()
                val p95Index = ((sorted.size - 1) * 0.95).roundToInt()
                return FrameTimingStats(
                    frames = sorted.size,
                    medianMs = sorted[sorted.size / 2] / NANOS_PER_MS,
                    p95Ms = sorted[p95Index] / NANOS_PER_MS,
                    maxMs = sorted.last() / NANOS_PER_MS,
                    over16_67Ms = sorted.count { it > FRAME_60HZ_NS },
                    over33_34Ms = sorted.count { it > FRAME_30HZ_NS },
                )
            }

            private fun Double.round(decimals: Int): Double {
                val scale = Math.pow(10.0, decimals.toDouble())
                return kotlin.math.round(this * scale) / scale
            }
        }
    }

    private class FrameMetricsCollector(
        private val window: Window,
    ) : AutoCloseable {
        private val callbackThread = HandlerThread("launcher-frame-metrics").apply { start() }
        private val callbackHandler = Handler(callbackThread.looper)
        private val samples = mutableListOf<Long>()

        @Volatile
        private var recording = false

        private val listener = Window.OnFrameMetricsAvailableListener { _, frameMetrics, _ ->
            if (!recording) return@OnFrameMetricsAvailableListener
            val duration = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
            if (duration > 0L) {
                synchronized(samples) {
                    samples += duration
                }
            }
        }

        init {
            window.addOnFrameMetricsAvailableListener(listener, callbackHandler)
        }

        fun start() {
            synchronized(samples) {
                samples.clear()
            }
            recording = true
        }

        fun stop(): List<Long> {
            recording = false
            return synchronized(samples) {
                samples.toList()
            }
        }

        override fun close() {
            recording = false
            window.removeOnFrameMetricsAvailableListener(listener)
            callbackThread.quitSafely()
        }
    }

    private companion object {
        const val TAG = "LauncherTransitionPerf"
        const val WARMUP_ROUND_TRIPS = 2
        const val MEASURED_ROUND_TRIPS = 6
        const val READY_TIMEOUT_MS = 30_000L
        const val MODE_TIMEOUT_MS = 5_000L
        const val POLL_MS = 16L
        const val SETTLE_MS = 300L
        const val TRANSITION_SETTLE_MS = 220L
        const val FRAME_CALLBACK_DRAIN_MS = 60L
        const val GESTURE_STEPS = 10
        const val GESTURE_STEP_MS = 10L
        const val FRAME_60HZ_NS = 16_666_667L
        const val FRAME_30HZ_NS = 33_333_334L
        const val NANOS_PER_MS = 1_000_000.0
    }
}
