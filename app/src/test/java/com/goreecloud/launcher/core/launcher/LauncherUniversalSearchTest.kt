package com.goreecloud.launcher.core.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
