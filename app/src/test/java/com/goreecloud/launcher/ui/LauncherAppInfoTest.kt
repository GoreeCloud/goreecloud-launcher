package com.goreecloud.launcher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LauncherAppInfoTest {
    @Test
    fun packageUriTargetsExactTrimmedPackage() {
        assertEquals("package:com.goreecloud.mail", LauncherAppInfo.packageUri("  com.goreecloud.mail  "))
    }

    @Test
    fun packageUriRejectsBlankPackages() {
        assertThrows(IllegalArgumentException::class.java) {
            LauncherAppInfo.packageUri("   ")
        }
    }
}
