package com.goreecloud.launcher

import android.app.role.RoleManager
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativeWriteResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceProductionRuntimeCoordinator
import com.goreecloud.launcher.core.workspace.workspaceKey
import java.io.FileInputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivatedHomeLifecycleRuntimeTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun recreatedMainActivityRecollectsRoomPlacementAndRemainsReactive() = runBlocking {
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
            val apps = withTimeout(10_000) {
                LauncherAppsRepository(context).apps.first { candidates ->
                    candidates.count { it.componentName.packageName != context.packageName } >= 2
                }
            }
            val candidates = apps
                .filter { it.componentName.packageName != context.packageName }
                .distinctBy { it.label.toString() }
            check(candidates.size >= 2) { "API 36 lifecycle test requires two distinct launchable app labels." }

            val firstApp = candidates[0]
            val secondApp = candidates[1]
            val firstKey = firstApp.workspaceKey()
            val secondKey = secondApp.workspaceKey()
            val repository = WorkspaceRepository(context)
            repository.ensureDefaults(
                favoriteKeys = listOf(firstKey),
                dockKeys = emptyList(),
            )

            val scenario = ActivityScenario.launch(MainActivity::class.java)
            try {
                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }
                waitForDisplayedLabel(firstApp.label.toString())

                scenario.recreate()

                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }
                waitForDisplayedLabel(firstApp.label.toString())

                val runtime = WorkspaceProductionRuntimeCoordinator(
                    authorityRepository = repository,
                    workspaceDaoProvider = {
                        LauncherDatabaseProvider.get(context).workspaceDao()
                    },
                )
                val write = runtime.toggleFavorite(secondKey)
                check(write is WorkspaceAuthoritativeWriteResult.Written)
                assertEquals(WorkspaceAuthority.ROOM, repository.state.first().authority)

                waitForDisplayedLabel(secondApp.label.toString())
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

    private fun waitForDisplayedLabel(label: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(label, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText(label, useUnmergedTree = true).assertIsDisplayed()
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
