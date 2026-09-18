package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherInventoryRefreshScopeTest {
    @Test
    fun packageLifecycleChangesUsePackageScopedRefresh() {
        val packageChanges = listOf(
            LauncherInventoryChange.PACKAGE_ADDED,
            LauncherInventoryChange.PACKAGE_REMOVED,
            LauncherInventoryChange.PACKAGE_CHANGED,
            LauncherInventoryChange.PACKAGE_SUSPENDED,
            LauncherInventoryChange.PACKAGE_UNSUSPENDED,
        )

        packageChanges.forEach { change ->
            assertEquals(
                LauncherInventoryRefreshScope.PACKAGE,
                launcherInventoryRefreshScope(change),
            )
        }
    }

    @Test
    fun availabilityAndProfileTopologyChangesRequireFullRefresh() {
        val fullChanges = listOf(
            LauncherInventoryChange.PACKAGES_AVAILABLE,
            LauncherInventoryChange.PACKAGES_UNAVAILABLE,
            LauncherInventoryChange.PROFILE_TOPOLOGY,
        )

        fullChanges.forEach { change ->
            assertEquals(
                LauncherInventoryRefreshScope.FULL,
                launcherInventoryRefreshScope(change),
            )
        }
    }
}
