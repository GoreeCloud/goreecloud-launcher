package com.goreecloud.launcher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
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
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativePlacementState
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativeWriteResult
import com.goreecloud.launcher.core.workspace.db.WorkspacePlacementSource
import com.goreecloud.launcher.core.workspace.db.WorkspaceProductionRuntimeCoordinator
import com.goreecloud.launcher.core.workspace.workspaceKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivatedHomeProcessDeathRuntimeTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun seedTerminalRoomPlacement() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val candidates = launchableCandidates()
        val firstApp = candidates[0]
        val firstKey = firstApp.workspaceKey()
        val repository = WorkspaceRepository(context)

        repository.ensureDefaults(
            favoriteKeys = listOf(firstKey),
            dockKeys = emptyList(),
        )

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            withTimeout(15_000) {
                repository.state.first { state -> state.authority == WorkspaceAuthority.ROOM }
            }

            val runtime = productionRuntime(repository)
            val placement = withTimeout(15_000) {
                runtime.observePlacement().first { state ->
                    state is WorkspaceAuthoritativePlacementState.Ready &&
                        state.snapshot.source == WorkspacePlacementSource.ROOM
                }
            } as WorkspaceAuthoritativePlacementState.Ready

            assertEquals(listOf(firstKey), placement.snapshot.favoriteKeys)
            assertEquals(emptyList<String>(), placement.snapshot.dockKeys)
            waitForDisplayedLabel(firstApp.label.toString())
        } finally {
            scenario.close()
        }
    }

    @Test
    fun verifyColdStartRoomPlacementAndReactivity() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val candidates = launchableCandidates()
        val firstApp = candidates[0]
        val secondApp = candidates[1]
        val firstKey = firstApp.workspaceKey()
        val secondKey = secondApp.workspaceKey()
        val repository = WorkspaceRepository(context)

        val persistedAuthority = withTimeout(15_000) {
            repository.state.first { state -> state.authority == WorkspaceAuthority.ROOM }
        }
        assertEquals(WorkspaceAuthority.ROOM, persistedAuthority.authority)

        val runtime = productionRuntime(repository)
        val persistedPlacement = withTimeout(15_000) {
            runtime.observePlacement().first { state ->
                state is WorkspaceAuthoritativePlacementState.Ready &&
                    state.snapshot.source == WorkspacePlacementSource.ROOM
            }
        } as WorkspaceAuthoritativePlacementState.Ready
        assertEquals(listOf(firstKey), persistedPlacement.snapshot.favoriteKeys)
        assertEquals(emptyList<String>(), persistedPlacement.snapshot.dockKeys)

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForDisplayedLabel(firstApp.label.toString())
            composeRule.onNodeWithText(secondApp.label.toString(), useUnmergedTree = true)
                .assertDoesNotExist()

            val write = runtime.toggleFavorite(secondKey)
            check(write is WorkspaceAuthoritativeWriteResult.Written)
            assertEquals(WorkspaceAuthority.ROOM, repository.state.first().authority)

            val updatedPlacement = withTimeout(15_000) {
                runtime.observePlacement().first { state ->
                    state is WorkspaceAuthoritativePlacementState.Ready &&
                        state.snapshot.source == WorkspacePlacementSource.ROOM &&
                        state.snapshot.favoriteKeys == listOf(firstKey, secondKey)
                }
            } as WorkspaceAuthoritativePlacementState.Ready
            assertEquals(listOf(firstKey, secondKey), updatedPlacement.snapshot.favoriteKeys)
            waitForDisplayedLabel(secondApp.label.toString())
        } finally {
            scenario.close()
        }
    }

    private suspend fun launchableCandidates() =
        withTimeout(10_000) {
            LauncherAppsRepository(InstrumentationRegistry.getInstrumentation().targetContext)
                .apps
                .first { candidates ->
                    candidates.count {
                        it.componentName.packageName !=
                            InstrumentationRegistry.getInstrumentation().targetContext.packageName
                    } >= 2
                }
        }
            .filter {
                it.componentName.packageName !=
                    InstrumentationRegistry.getInstrumentation().targetContext.packageName
            }
            .distinctBy { it.label.toString() }
            .also { candidates ->
                check(candidates.size >= 2) {
                    "API 36 process-death acceptance requires two distinct launchable app labels."
                }
            }

    private fun productionRuntime(repository: WorkspaceRepository): WorkspaceProductionRuntimeCoordinator {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return WorkspaceProductionRuntimeCoordinator(
            authorityRepository = repository,
            workspaceDaoProvider = {
                LauncherDatabaseProvider.get(context).workspaceDao()
            },
        )
    }

    private fun waitForDisplayedLabel(label: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(label, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText(label, useUnmergedTree = true).assertIsDisplayed()
    }
}
