package com.goreecloud.launcher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeVerticalGestureTest {
    @Test
    fun upwardVerticalSwipeOpensDrawer() {
        assertEquals(
            HomeVerticalGesture.OPEN_DRAWER,
            classifyHomeVerticalGesture(deltaX = 8f, deltaY = -80f, threshold = 64f),
        )
    }

    @Test
    fun downwardVerticalSwipeOpensSearch() {
        assertEquals(
            HomeVerticalGesture.OPEN_SEARCH,
            classifyHomeVerticalGesture(deltaX = -4f, deltaY = 72f, threshold = 64f),
        )
    }

    @Test
    fun movementBelowThresholdDoesNothing() {
        assertEquals(
            HomeVerticalGesture.NONE,
            classifyHomeVerticalGesture(deltaX = 2f, deltaY = -63f, threshold = 64f),
        )
    }

    @Test
    fun horizontalDominantMovementDoesNothing() {
        assertEquals(
            HomeVerticalGesture.NONE,
            classifyHomeVerticalGesture(deltaX = 96f, deltaY = -80f, threshold = 64f),
        )
    }

    @Test
    fun invalidThresholdFailsClosed() {
        assertEquals(
            HomeVerticalGesture.NONE,
            classifyHomeVerticalGesture(deltaX = 0f, deltaY = -100f, threshold = 0f),
        )
    }
}
