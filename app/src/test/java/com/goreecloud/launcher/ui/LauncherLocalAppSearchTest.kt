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
                packageName = "com.goreecloud.memos.development",
                className = "com.goreecloud.memos.MainActivity",
                query = "   \t\n  ",
            ),
        )
    }

    @Test
    fun matchingIsCaseInsensitiveAcrossLabelPackageAndActivity() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Browser",
                packageName = "io.goreecloud.browser.beta",
                className = "io.goreecloud.browser.BrowserActivityV2",
                query = "BROWSER beta activityv2",
            ),
        )
    }

    @Test
    fun everySearchTermMustMatchLocalMetadata() {
        assertFalse(
            matchesLauncherAppSearch(
                label = "GoreeCloud Browser",
                packageName = "io.goreecloud.browser.beta",
                className = "io.goreecloud.browser.BrowserActivityV2",
                query = "browser memos",
            ),
        )
    }

    @Test
    fun unicodeSeparatorsAndNormalizationRemainSearchable() {
        assertTrue(
            matchesLauncherAppSearch(
                label = "GoreeCloud Café Notes",
                packageName = "com.goreecloud.notes",
                className = "com.goreecloud.notes.MainActivity",
                query = "café\u00A0notes",
            ),
        )
    }
}
