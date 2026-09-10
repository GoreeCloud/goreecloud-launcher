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
                query = "   \t\n  ",
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

    @Test
    fun unicodeSeparatorsRemainLocalSearchTermBoundaries() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Café Notes",
                packageName = "com.goreecloud.notes",
                className = "com.goreecloud.notes.MainActivity",
                query = "café\u00A0notes",
            )
        )
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Browser",
                packageName = "io.goreecloud.browser.beta",
                className = "io.goreecloud.browser.BrowserActivityV2",
                query = "browser\u2003beta",
            )
        )
    }

    @Test
    fun composedAndDecomposedUnicodeMatchTheSameLabel() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Résumé",
                packageName = "com.goreecloud.resume",
                className = "com.goreecloud.resume.MainActivity",
                query = "résumé",
            )
        )
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Café",
                packageName = "com.goreecloud.cafe",
                className = "com.goreecloud.cafe.MainActivity",
                query = "café",
            )
        )
    }

    @Test
    fun repeatedTermsDoNotChangeMatchSemantics() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Browser",
                packageName = "io.goreecloud.browser.beta",
                className = "io.goreecloud.browser.BrowserActivityV2",
                query = "browser browser beta browser",
            )
        )
    }
}
