package com.goreecloud.launcher

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniversalSearchFallbackRuntimeTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun homeUniversalSearchFallsBackToLocalAppsWhenIndexIsUnavailable() {
        // Keep this test authoritative for the unavailable-Index boundary even if a future shared
        // emulator image happens to contain either first-party Index package.
        runShellCommand("pm uninstall --user 0 com.goreecloud.index")
        runShellCommand("pm uninstall --user 0 com.goreecloud.index.dev")

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForText("•••")

            composeRule.onRoot(useUnmergedTree = true).performTouchInput {
                swipeDown()
            }

            waitForText("Search apps")
            composeRule.onNodeWithText("Search apps").assertIsDisplayed()
        } finally {
            scenario.close()
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun runShellCommand(command: String) {
        val descriptor: ParcelFileDescriptor =
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        FileInputStream(descriptor.fileDescriptor).use { input ->
            input.readBytes()
        }
        descriptor.close()
    }
}
