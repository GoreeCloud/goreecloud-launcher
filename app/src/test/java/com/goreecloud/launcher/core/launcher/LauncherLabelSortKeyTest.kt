package com.goreecloud.launcher.core.launcher

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherLabelSortKeyTest {
    @Test
    fun sortKeyDoesNotDependOnDeviceDefaultLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))

            assertEquals("i", launcherLabelSortKey("I"))
            assertEquals("calendar", launcherLabelSortKey("CALENDAR"))
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test
    fun canonicallyEquivalentLabelsShareOneSortKey() {
        assertEquals(
            launcherLabelSortKey("Café"),
            launcherLabelSortKey("Cafe\u0301"),
        )
    }

    @Test
    fun ordinaryPackageChangesUsePackageScopedRefreshes() {
        val ordinaryChanges = listOf(
            LauncherInventoryChange.PACKAGE_ADDED,
            LauncherInventoryChange.PACKAGE_REMOVED,
            LauncherInventoryChange.PACKAGE_CHANGED,
            LauncherInventoryChange.PACKAGE_SUSPENDED,
            LauncherInventoryChange.PACKAGE_UNSUSPENDED,
        )

        ordinaryChanges.forEach { change ->
            assertEquals(
                LauncherInventoryRefreshScope.PACKAGE,
                launcherInventoryRefreshScope(change),
            )
        }
    }

    @Test
    fun availabilityAndProfileTopologyChangesRequireFullReconciliation() {
        val topologyChanges = listOf(
            LauncherInventoryChange.PACKAGES_AVAILABLE,
            LauncherInventoryChange.PACKAGES_UNAVAILABLE,
            LauncherInventoryChange.PROFILE_TOPOLOGY,
        )

        topologyChanges.forEach { change ->
            assertEquals(
                LauncherInventoryRefreshScope.FULL,
                launcherInventoryRefreshScope(change),
            )
        }
    }
}
