package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherUniversalSearchTest {
    @Test
    fun textRankingPrioritizesExactPrefixContainsAndSubtitle() {
        assertEquals(400, LauncherSearchTextRanking.score("Camera", "com.goreecloud.camera", "camera"))
        assertEquals(300, LauncherSearchTextRanking.score("Camera Pro", "com.goreecloud.camera", "cam"))
        assertEquals(200, LauncherSearchTextRanking.score("GoreeCloud Camera", "com.goreecloud.camera", "camera"))
        assertEquals(100, LauncherSearchTextRanking.score("Photos", "com.goreecloud.camera", "camera"))
        assertNull(LauncherSearchTextRanking.score("Photos", "com.goreecloud.gallery", "camera"))
    }

    @Test
    fun blankQueryPreservesBrowseEligibility() {
        assertEquals(0, LauncherSearchTextRanking.score("Camera", "com.goreecloud.camera", "   "))
    }

    @Test
    fun providerFailureDoesNotDisableOtherResults() {
        val failing = object : LauncherSearchProvider {
            override val id = "failing"

            override fun search(rawQuery: String): List<LauncherSearchResult> {
                error("provider failed")
            }
        }
        val working = object : LauncherSearchProvider {
            override val id = "working"

            override fun search(rawQuery: String): List<LauncherSearchResult> =
                listOf(
                    LauncherSearchResult(
                        providerId = id,
                        resultId = "2",
                        title = "Beta",
                        subtitle = null,
                        category = LauncherSearchCategory.APPLICATION,
                        score = 100,
                    ),
                    LauncherSearchResult(
                        providerId = id,
                        resultId = "1",
                        title = "Alpha",
                        subtitle = null,
                        category = LauncherSearchCategory.APPLICATION,
                        score = 200,
                    ),
                )
        }

        val results = LauncherUniversalSearch.search("a", listOf(failing, working))

        assertEquals(listOf("Alpha", "Beta"), results.map { it.title })
    }

    @Test
    fun duplicateProviderResultsAreCollapsedDeterministically() {
        val provider = object : LauncherSearchProvider {
            override val id = "working"

            override fun search(rawQuery: String): List<LauncherSearchResult> {
                val result = LauncherSearchResult(
                    providerId = id,
                    resultId = "same",
                    title = "Camera",
                    subtitle = null,
                    category = LauncherSearchCategory.APPLICATION,
                    score = 400,
                )
                return listOf(result, result)
            }
        }

        assertEquals(1, LauncherUniversalSearch.search("camera", listOf(provider)).size)
    }

    @Test
    fun coreActionsProviderReturnsTypedSettingsDestination() {
        val result = LauncherCoreActionsSearchProvider()
            .search("settings")
            .single { it.title == "Launcher settings" }

        assertEquals("Launcher settings", result.title)
        assertEquals(LauncherSearchCategory.SETTING, result.category)
        assertEquals(
            LauncherSearchDestination.SETTINGS,
            (result.action as LauncherNavigateSearchAction).destination,
        )
    }

    @Test
    fun coreActionsProviderSearchesActionMetadataLocally() {
        val results = LauncherCoreActionsSearchProvider().search("drawer")

        assertEquals(listOf("Apps"), results.map { it.title })
        assertTrue(results.all { it.providerId == LauncherCoreActionsSearchProvider.PROVIDER_ID })
        assertEquals(
            LauncherSearchDestination.APPS,
            (results.single().action as LauncherNavigateSearchAction).destination,
        )
    }

    @Test
    fun coreActionsProviderExposesTrustedHomeAppearanceActions() {
        val provider = LauncherCoreActionsSearchProvider()

        val editHome = provider.search("edit home")
            .single { it.title == "Edit Home" }
        assertEquals(
            LauncherSearchDestination.HOME_EDITOR,
            (editHome.action as LauncherNavigateSearchAction).destination,
        )

        val wallpaper = provider.search("wallpaper")
            .single { it.title == "Wallpaper" }
        assertEquals("Wallpaper", wallpaper.title)
        assertEquals(LauncherSearchCategory.SETTING, wallpaper.category)
        assertEquals(
            LauncherSearchDestination.WALLPAPER,
            (wallpaper.action as LauncherNavigateSearchAction).destination,
        )

        val themeManager = provider.search("theme")
            .single { it.title == "Theme Manager" }
        assertEquals("Theme Manager", themeManager.title)
        assertEquals(LauncherSearchCategory.SETTING, themeManager.category)
        assertEquals(
            LauncherSearchDestination.THEME_MANAGER,
            (themeManager.action as LauncherNavigateSearchAction).destination,
        )
    }

}
