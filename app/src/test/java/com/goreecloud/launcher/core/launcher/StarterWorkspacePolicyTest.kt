package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StarterWorkspacePolicyTest {
    private fun candidate(key: String, label: String, pkg: String = "example.$key") =
        StarterWorkspaceCandidate(key = key, label = label, packageName = pkg)

    @Test
    fun selectsIntentionalDockAndFavoritesWithoutLauncherPackages() {
        val selection = StarterWorkspacePolicy.select(
            listOf(
                candidate("launcher", "GoreeCloud Launcher", "com.goreecloud.launcher.dev"),
                candidate("phone", "Phone"),
                candidate("messages", "GoreeCloud Messenger"),
                candidate("browser", "GoreeCloud Browser"),
                candidate("camera", "Camera"),
                candidate("calendar", "Calendar"),
                candidate("clock", "Clock"),
                candidate("contacts", "Contacts"),
                candidate("gallery", "GoreeCloud Gallery"),
                candidate("memos", "GoreeCloud Memos"),
                candidate("files", "Files"),
                candidate("drive", "Drive"),
                candidate("music", "Music"),
            ),
        )

        assertEquals(listOf("phone", "messages", "browser", "camera"), selection.dockKeys)
        assertEquals(
            listOf("calendar", "clock", "contacts", "gallery", "memos", "files", "drive", "music"),
            selection.favoriteKeys,
        )
        assertFalse(selection.dockKeys.contains("launcher"))
        assertFalse(selection.favoriteKeys.contains("launcher"))
    }

    @Test
    fun fillsSparseDevicesDeterministicallyWithoutDuplicates() {
        val selection = StarterWorkspacePolicy.select(
            listOf(
                candidate("a", "Alpha"),
                candidate("b", "Beta"),
                candidate("c", "Camera"),
                candidate("d", "Delta"),
                candidate("e", "Echo"),
            ),
            maxFavorites = 3,
            maxDock = 3,
        )

        assertEquals(3, selection.dockKeys.size)
        assertEquals(2, selection.favoriteKeys.size)
        assertEquals(5, (selection.dockKeys + selection.favoriteKeys).distinct().size)
    }
}
