package com.goreecloud.launcher.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherLocalAppSearchTest {
    @Test
    fun blankQueryKeepsInstalledAppVisible() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "Memos",
                packageName = "com.goreecloud.memos.native.dev",
                className = "com.goreecloud.memos.MainActivity",
                query = "   ",
            )
        )
    }

    @Test
    fun labelMatchingIsTrimmedAndCaseInsensitive() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Memos",
                packageName = "com.goreecloud.memos.native.dev",
                className = "com.goreecloud.memos.MainActivity",
                query = "  MEMOS  ",
            )
        )
    }

    @Test
    fun packageAndActivityNamesAreSearchableLocally() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "Memos",
                packageName = "com.goreecloud.memos.native.dev",
                className = "com.goreecloud.memos.MainActivity",
                query = "native mainactivity",
            )
        )
    }

    @Test
    fun multipleTermsMustAllMatchInstalledAppMetadata() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Browser",
                packageName = "io.goreecloud.browser.beta",
                className = "io.goreecloud.browser.BrowserActivityV2",
                query = "browser beta",
            )
        )
        assertFalse(
            matchesLauncherAppSearch(
                label = "GoreeCloud Browser",
                packageName = "io.goreecloud.browser.beta",
                className = "io.goreecloud.browser.BrowserActivityV2",
                query = "browser memos",
            )
        )
    }
}
