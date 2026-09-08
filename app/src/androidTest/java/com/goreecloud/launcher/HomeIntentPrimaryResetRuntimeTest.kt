package com.goreecloud.launcher

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeIntentPrimaryResetRuntimeTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun homeIntentIsDistinguishedFromOrdinaryLauncherIntent() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val launcher = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        assertTrue(home.isHomeReturnIntent())
        assertFalse(launcher.isHomeReturnIntent())
        assertFalse(Intent(Intent.ACTION_VIEW).isHomeReturnIntent())
    }

    @Test
    fun homeIntentReturnsExistingSingleTaskInstanceFromSettingsToPrimaryHome() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForText("•••")
            composeRule.onNodeWithText("•••", useUnmergedTree = true).performClick()
            waitForText("Home screen")

            scenario.onActivity { activity ->
                activity.onNewIntent(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                    }
                )
            }

            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodesWithText("Home screen", useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
            composeRule.onNodeWithText("•••", useUnmergedTree = true).assertIsDisplayed()
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
        composeRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
    }
}
