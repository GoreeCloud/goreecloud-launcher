package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalSearchRouterTest {
    @Test
    fun usesIndexWhenAvailableWithoutOpeningLocalFallback() {
        var localOpened = false
        val router = UniversalSearchRouter(
            openIndexSearch = { true },
            openLocalAppSearch = { localOpened = true },
        )

        assertEquals(UniversalSearchRouter.Destination.INDEX, router.open())
        assertFalse(localOpened)
    }

    @Test
    fun opensLocalAppSearchWhenIndexCannotOpen() {
        var localOpened = false
        val router = UniversalSearchRouter(
            openIndexSearch = { false },
            openLocalAppSearch = { localOpened = true },
        )

        assertEquals(UniversalSearchRouter.Destination.LOCAL_APPS, router.open())
        assertTrue(localOpened)
    }

    @Test
    fun attemptsIndexOnlyOnceBeforeFallingBack() {
        var indexAttempts = 0
        var localOpens = 0
        val router = UniversalSearchRouter(
            openIndexSearch = {
                indexAttempts += 1
                false
            },
            openLocalAppSearch = { localOpens += 1 },
        )

        router.open()

        assertEquals(1, indexAttempts)
        assertEquals(1, localOpens)
    }
}
