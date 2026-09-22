package com.goreecloud.launcher

import android.app.role.RoleManager
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.core.launcher.LauncherGestureAction
import com.goreecloud.launcher.core.launcher.LauncherGestureActionType
import com.goreecloud.launcher.core.launcher.LauncherHomeGesture
import com.goreecloud.launcher.core.launcher.LauncherPreferencesRepository
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspaceAuthoritativeWriteResult
import com.goreecloud.launcher.core.workspace.db.WorkspaceLegacyImportMapper
import com.goreecloud.launcher.core.workspace.db.WorkspacePrimaryHomeSpatialResult
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
                val preferences = LauncherPreferencesRepository(context).preferences.first()
                val write = runtime.toggleFavorite(
                    key = secondKey,
                    homeColumns = preferences.homeColumns,
                    homeRows = preferences.homeRows,
                )
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

    @Test
    fun swipeUpStartingOnWorkspaceAppContentOpensDrawer() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val roleManager = context.getSystemService(RoleManager::class.java)
        val alreadyDefaultHome =
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        val preferencesRepository = LauncherPreferencesRepository(context)
        val previousSwipeUp = preferencesRepository.experiencePreferences.first().swipeUpAction
        val appsAction = LauncherGestureAction.builtIn(LauncherGestureActionType.APPS)

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
            preferencesRepository.setGestureAction(
                LauncherHomeGesture.SWIPE_UP,
                appsAction,
            ).join()

            val apps = withTimeout(10_000) {
                LauncherAppsRepository(context).apps.first { candidates ->
                    candidates.any { it.componentName.packageName != context.packageName }
                }
            }
            val candidate = apps.first { it.componentName.packageName != context.packageName }
            val candidateKey = candidate.workspaceKey()
            val repository = WorkspaceRepository(context)
            repository.ensureDefaults(
                favoriteKeys = listOf(candidateKey),
                dockKeys = emptyList(),
            )

            val scenario = ActivityScenario.launch(MainActivity::class.java)
            try {
                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }

                val runtime = WorkspaceProductionRuntimeCoordinator(
                    authorityRepository = repository,
                    workspaceDaoProvider = {
                        LauncherDatabaseProvider.get(context).workspaceDao()
                    },
                )
                if (candidateKey !in repository.state.first().favoriteKeys) {
                    val preferences = LauncherPreferencesRepository(context).preferences.first()
                    val write = runtime.toggleFavorite(
                        key = candidateKey,
                        homeColumns = preferences.homeColumns,
                        homeRows = preferences.homeRows,
                    )
                    check(write is WorkspaceAuthoritativeWriteResult.Written)
                }

                waitForDisplayedLabel(candidate.label.toString())
                composeRule.waitUntil(timeoutMillis = 15_000) {
                    composeRule
                        .onAllNodesWithTag(
                            "launcher-home-swipe-up-apps",
                            useUnmergedTree = true,
                        )
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }

                composeRule
                    .onNodeWithText(candidate.label.toString(), useUnmergedTree = true)
                    .performTouchInput {
                        swipeUp(
                            startY = bottom - 1f,
                            endY = top - 320f,
                            durationMillis = 400,
                        )
                    }

                composeRule.waitUntil(timeoutMillis = 10_000) {
                    composeRule.onAllNodesWithText("Apps", useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
                composeRule.onNodeWithText("Apps", useUnmergedTree = true).assertIsDisplayed()
                composeRule.onNodeWithText("Search apps", useUnmergedTree = true).assertIsDisplayed()
                Unit
            } finally {
                scenario.close()
            }
        } finally {
            preferencesRepository.setGestureAction(
                LauncherHomeGesture.SWIPE_UP,
                previousSwipeUp,
            ).join()
            if (!alreadyDefaultHome) {
                runShellCommand(
                    "cmd role remove-role-holder ${RoleManager.ROLE_HOME} ${context.packageName}"
                )
            }
        }
    }

    @Test
    fun configuredSwipeDownStartingOnWorkspaceAppContentOpensUniversalSearch() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val roleManager = context.getSystemService(RoleManager::class.java)
        val alreadyDefaultHome =
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        val preferencesRepository = LauncherPreferencesRepository(context)
        val previousSwipeDown = preferencesRepository.experiencePreferences.first().swipeDownAction
        val searchAction =
            LauncherGestureAction.builtIn(LauncherGestureActionType.UNIVERSAL_SEARCH)

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
            preferencesRepository.setGestureAction(
                LauncherHomeGesture.SWIPE_DOWN,
                searchAction,
            ).join()

            val apps = withTimeout(10_000) {
                LauncherAppsRepository(context).apps.first { candidates ->
                    candidates.any { it.componentName.packageName != context.packageName }
                }
            }
            val candidate = apps.first { it.componentName.packageName != context.packageName }
            val candidateKey = candidate.workspaceKey()
            val repository = WorkspaceRepository(context)
            repository.ensureDefaults(
                favoriteKeys = listOf(candidateKey),
                dockKeys = emptyList(),
            )

            val scenario = ActivityScenario.launch(MainActivity::class.java)
            try {
                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }
                waitForDisplayedLabel(candidate.label.toString())

                composeRule
                    .onNodeWithText(candidate.label.toString(), useUnmergedTree = true)
                    .performTouchInput {
                        swipeDown(
                            startY = top + 1f,
                            endY = bottom + 320f,
                            durationMillis = 400,
                        )
                    }

                composeRule.waitUntil(timeoutMillis = 10_000) {
                    composeRule.onAllNodesWithText("Universal Search", useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
                composeRule
                    .onNodeWithText("Universal Search", useUnmergedTree = true)
                    .assertIsDisplayed()
                composeRule
                    .onNodeWithText(
                        "Search apps, settings and actions",
                        useUnmergedTree = true,
                    )
                    .assertIsDisplayed()
                Unit
            } finally {
                scenario.close()
            }
        } finally {
            preferencesRepository.setGestureAction(
                LauncherHomeGesture.SWIPE_DOWN,
                previousSwipeDown,
            ).join()
            if (!alreadyDefaultHome) {
                runShellCommand(
                    "cmd role remove-role-holder ${RoleManager.ROLE_HOME} ${context.packageName}"
                )
            }
        }
    }

    @Test
    fun longPressDragMovesPrimaryHomeAppIntoEmptyCellAndPersists() = runBlocking {
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
                    candidates
                        .filter { it.componentName.packageName != context.packageName }
                        .distinctBy { it.label.toString() }
                        .size >= 2
                }
            }
            val candidates = apps
                .filter { it.componentName.packageName != context.packageName }
                .distinctBy { it.label.toString() }
            val firstApp = candidates[0]
            val secondApp = candidates[1]
            val firstKey = firstApp.workspaceKey()
            val secondKey = secondApp.workspaceKey()
            val repository = WorkspaceRepository(context)
            repository.ensureDefaults(
                favoriteKeys = listOf(firstKey, secondKey),
                dockKeys = emptyList(),
            )

            val scenario = ActivityScenario.launch(MainActivity::class.java)
            try {
                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }

                val dao = LauncherDatabaseProvider.get(context).workspaceDao()
                val baseline = WorkspaceLegacyImportMapper.map(
                    favoriteKeys = listOf(firstKey, secondKey),
                    dockKeys = emptyList(),
                )
                dao.replaceLegacySnapshot(baseline.pages, baseline.items)

                val preferences = LauncherPreferencesRepository(context).preferences.first()
                val runtime = WorkspaceProductionRuntimeCoordinator(
                    authorityRepository = repository,
                    workspaceDaoProvider = {
                        LauncherDatabaseProvider.get(context).workspaceDao()
                    },
                )
                val spatialReady = runtime.ensurePrimaryHomeSpatialGrid(
                    columns = preferences.homeColumns,
                    rows = preferences.homeRows,
                )
                check(spatialReady is WorkspacePrimaryHomeSpatialResult.Ready)

                waitForDisplayedLabel(firstApp.label.toString())
                waitForDisplayedLabel(secondApp.label.toString())

                val occupied = dao
                    .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
                    .mapNotNull { item ->
                        val x = item.cellX
                        val y = item.cellY
                        if (x != null && y != null) x to y else null
                    }
                    .toSet()
                val target = buildList {
                    for (cellY in 0 until preferences.homeRows) {
                        for (cellX in 0 until preferences.homeColumns) {
                            add(cellX to cellY)
                        }
                    }
                }.firstOrNull { it !in occupied }
                checkNotNull(target) { "Primary Home runtime test requires at least one empty cell." }
                val targetX = target.first
                val targetY = target.second
                val targetTag = "launcher-home-cell-$targetX-$targetY"

                composeRule.waitUntil(timeoutMillis = 15_000) {
                    composeRule
                        .onAllNodesWithTag(targetTag, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }

                val firstBounds = composeRule
                    .onNodeWithText(firstApp.label.toString(), useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
                val targetBounds = composeRule
                    .onNodeWithTag(targetTag, useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
                val delta = targetBounds.center - firstBounds.center

                composeRule
                    .onNodeWithText(firstApp.label.toString(), useUnmergedTree = true)
                    .performTouchInput {
                        down(center)
                        advanceEventTime(700)
                        moveTo(center + delta)
                        advanceEventTime(120)
                        up()
                    }

                withTimeout(10_000) {
                    while (true) {
                        val moved = dao
                            .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
                            .singleOrNull { it.appKey == firstKey }
                        if (moved?.cellX == targetX && moved.cellY == targetY) break
                        delay(100)
                    }
                }

                val afterMove = dao
                    .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
                    .sortedBy { it.rank }
                val firstStored = checkNotNull(afterMove.singleOrNull { it.appKey == firstKey })
                val secondStored = checkNotNull(afterMove.singleOrNull { it.appKey == secondKey })
                assertEquals(targetX, firstStored.cellX)
                assertEquals(targetY, firstStored.cellY)
                check(firstStored.cellX != secondStored.cellX || firstStored.cellY != secondStored.cellY)

                scenario.recreate()
                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }
                waitForDisplayedLabel(firstApp.label.toString())

                val afterRecreate = dao
                    .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
                    .single { it.appKey == firstKey }
                assertEquals(targetX, afterRecreate.cellX)
                assertEquals(targetY, afterRecreate.cellY)
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

    @Test
    fun homeButtonFromDrawerReturnsPrimaryHomeSurface() = runBlocking {
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
                    candidates.any { it.componentName.packageName != context.packageName }
                }
            }
            val candidate = apps.first { it.componentName.packageName != context.packageName }
            val candidateKey = candidate.workspaceKey()
            val repository = WorkspaceRepository(context)
            repository.ensureDefaults(
                favoriteKeys = listOf(candidateKey),
                dockKeys = emptyList(),
            )

            val scenario = ActivityScenario.launch(MainActivity::class.java)
            try {
                withTimeout(15_000) {
                    repository.state.first { it.authority == WorkspaceAuthority.ROOM }
                }
                waitForDisplayedLabel(candidate.label.toString())

                composeRule
                    .onNodeWithText(candidate.label.toString(), useUnmergedTree = true)
                    .performTouchInput {
                        swipeUp(
                            startY = bottom - 1f,
                            endY = top - 320f,
                            durationMillis = 400,
                        )
                    }

                composeRule.waitUntil(timeoutMillis = 10_000) {
                    composeRule.onAllNodesWithText("Search apps", useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }

                runShellCommand("input keyevent KEYCODE_HOME")

                composeRule.waitUntil(timeoutMillis = 10_000) {
                    composeRule.onAllNodesWithText("Search apps", useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isEmpty()
                }
                waitForDisplayedLabel(candidate.label.toString())
                Unit
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

    @Test
    fun configuredSwipeUpCanOpenLauncherSettings() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val roleManager = context.getSystemService(RoleManager::class.java)
        val alreadyDefaultHome =
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        val preferencesRepository = LauncherPreferencesRepository(context)
        val previousSwipeUp = preferencesRepository.experiencePreferences.first().swipeUpAction
        val configuredAction =
            LauncherGestureAction.builtIn(LauncherGestureActionType.LAUNCHER_SETTINGS)
        val renderedGestureTag = "launcher-home-swipe-up-launcher_settings"

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
            preferencesRepository.setGestureAction(
                LauncherHomeGesture.SWIPE_UP,
                configuredAction,
            ).join()

            val scenario = ActivityScenario.launch(MainActivity::class.java)
            try {
                composeRule.waitUntil(timeoutMillis = 15_000) {
                    composeRule
                        .onAllNodesWithTag(
                            renderedGestureTag,
                            useUnmergedTree = true,
                        )
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }

                composeRule
                    .onNodeWithTag(
                        renderedGestureTag,
                        useUnmergedTree = true,
                    )
                    .performTouchInput {
                        swipeUp(
                            startY = bottom - 1f,
                            endY = top + 1f,
                            durationMillis = 400,
                        )
                    }

                composeRule.waitUntil(timeoutMillis = 15_000) {
                    composeRule
                        .onAllNodesWithText(
                            "Home, apps, dock, search and Glaze",
                            useUnmergedTree = true,
                        )
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
                composeRule
                    .onNodeWithText(
                        "Home, apps, dock, search and Glaze",
                        useUnmergedTree = true,
                    )
                    .assertIsDisplayed()
                Unit
            } finally {
                scenario.close()
            }
        } finally {
            preferencesRepository.setGestureAction(
                LauncherHomeGesture.SWIPE_UP,
                previousSwipeUp,
            ).join()
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
