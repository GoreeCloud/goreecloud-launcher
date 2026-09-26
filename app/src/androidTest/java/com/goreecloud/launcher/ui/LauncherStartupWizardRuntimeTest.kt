package com.goreecloud.launcher.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goreecloud.launcher.core.launcher.LauncherHomeAppMode
import com.goreecloud.launcher.core.launcher.LauncherUniversalSearchHomeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherStartupWizardRuntimeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun wizardOffersHomeModesAndReturnsSelectedConfiguration() {
        var completed: LauncherStartupConfiguration? = null

        composeRule.setContent {
            MaterialTheme {
                LauncherStartupWizard(
                    isDefaultHome = false,
                    initialHomeAppMode = LauncherHomeAppMode.NONE,
                    initialHomeColumns = 5,
                    initialHomeRows = 6,
                    initialShowHomeLabels = true,
                    initialUniversalSearchHomeMode =
                        LauncherUniversalSearchHomeMode.SWIPE_DOWN_ONLY,
                    initialAddNewAppsToHome = false,
                    initialShowHints = true,
                    onRequestHomeRole = {},
                    onFinish = { completed = it },
                )
            }
        }

        composeRule.onNodeWithText("Welcome to GoreeCloud Launcher").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("No automatic apps").assertIsDisplayed()
        composeRule.onNodeWithText("10 most recent apps").assertIsDisplayed()
        composeRule.onNodeWithText("10 most used apps").assertIsDisplayed()
        composeRule.onNodeWithText("10 most used apps").performClick()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("Search, gestures, and hints").assertIsDisplayed()
        composeRule.onNodeWithText("Finish setup").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { completed != null }
        val result = completed
        assertNotNull(result)
        assertEquals(LauncherHomeAppMode.MOST_USED, result?.homeAppMode)
        assertEquals(5, result?.homeColumns)
        assertEquals(6, result?.homeRows)
        assertEquals(LauncherUniversalSearchHomeMode.SWIPE_DOWN_ONLY, result?.universalSearchHomeMode)
    }
}
