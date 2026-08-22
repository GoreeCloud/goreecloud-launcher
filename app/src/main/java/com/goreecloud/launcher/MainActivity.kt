package com.goreecloud.launcher

import android.app.role.RoleManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goreecloud.launcher.core.launcher.LauncherAppsRepository
import com.goreecloud.launcher.ui.LauncherRoot
import com.goreecloud.launcher.ui.theme.GlazeTheme
import com.goreecloud.launcher.ui.theme.GlazeThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private lateinit var appsRepository: LauncherAppsRepository
    private lateinit var themeRepository: GlazeThemeRepository
    private val defaultHomeState = MutableStateFlow(false)

    private val homeRoleRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshHomeRoleState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appsRepository = LauncherAppsRepository(this)
        themeRepository = GlazeThemeRepository(this)
        refreshHomeRoleState()

        setContent {
            val apps by appsRepository.apps.collectAsStateWithLifecycle(initialValue = emptyList())
            val themeMode by themeRepository.themeMode.collectAsState(initial = themeRepository.defaultMode)
            val isDefaultHome by defaultHomeState.collectAsStateWithLifecycle()
            GlazeTheme(themeMode) {
                LauncherRoot(
                    apps = apps,
                    isDefaultHome = isDefaultHome,
                    onRequestHomeRole = ::requestHomeRole,
                    onLaunchApp = appsRepository::launch,
                    themeMode = themeMode,
                    onCycleTheme = themeRepository::cycleMode,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHomeRoleState()
    }

    private fun refreshHomeRoleState() {
        val manager = getSystemService(RoleManager::class.java)
        defaultHomeState.value =
            manager.isRoleAvailable(RoleManager.ROLE_HOME) && manager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun requestHomeRole() {
        val manager = getSystemService(RoleManager::class.java)
        if (manager.isRoleAvailable(RoleManager.ROLE_HOME) && !manager.isRoleHeld(RoleManager.ROLE_HOME)) {
            homeRoleRequest.launch(manager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        }
    }
}
