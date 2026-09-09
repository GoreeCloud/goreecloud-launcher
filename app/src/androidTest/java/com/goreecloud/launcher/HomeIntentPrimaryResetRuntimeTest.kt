package com.goreecloud.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
    fun homeButtonReturnsExistingSingleTaskInstanceFromSettingsToPrimaryHome() {
        runBlocking {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val context = instrumentation.targetContext
            val roleManager = context.getSystemService(RoleManager::class.java)
            val alreadyDefaultHome =
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && roleManager.isRoleHeld(RoleManager.ROLE_HOME)

            if (!alreadyDefaultHome) {
                runShellCommand(
                    "cmd role add-role-holder ${RoleManager.ROLE_HOME} ${context.packageName}"
                )
                withTimeout(10_000) {
                    while (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        delay(100)
                    }
                }
            }

            try {
                val scenario = ActivityScenario.launch(MainActivity::class.java)
                try {
                    waitForText("•••")
                    composeRule.onNodeWithText("•••").performClick()
                    waitForText("Home screen")

                    // Exercise Android's real HOME dispatch while GoreeCloud is the HOME role holder.
                    // This must return to the existing singleTask instance and drive onNewIntent,
                    // rather than starting a test-owned explicit activity that ActivityScenario tracks
                    // as a separate lifecycle transition.
                    runShellCommand("input keyevent KEYCODE_HOME")

                    // HOME can briefly leave no active Compose hierarchy while Android moves the
                    // existing singleTask instance back to the foreground. Treat that transition as
                    // "not ready yet" rather than failing the poll before the Activity resumes.
                    waitForText("•••")
                    assertFalse(hasText("Home screen"))
                } finally {
                    scenario.close()
                }
            } finally {
                if (!alreadyDefaultHome) {
                    runShellCommand(
                        "cmd role remove-role-holder ${RoleManager.ROLE_HOME} ${context.packageName}"
                    )
                }
            }
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            hasText(text)
        }
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }

    private fun hasText(text: String): Boolean =
        runCatching {
            composeRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }.getOrDefault(false)

    private fun runShellCommand(command: String) {
        val descriptor: ParcelFileDescriptor =
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        FileInputStream(descriptor.fileDescriptor).use { input ->
            input.readBytes()
        }
        descriptor.close()
    }
}
