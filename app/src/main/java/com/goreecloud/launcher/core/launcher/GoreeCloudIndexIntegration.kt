package com.goreecloud.launcher.core.launcher

import android.content.Context
import android.content.Intent

/**
 * Bounded Launcher handoff into the first-party GoreeCloud Index search surface.
 *
 * Launcher owns invocation UX and Launcher-specific context. GoreeCloud Index owns the
 * universal query/provider/result pipeline. This integration intentionally does not duplicate
 * Index provider or ranking logic inside Launcher.
 */
class GoreeCloudIndexIntegration(
    private val context: Context,
) {
    fun isAvailable(): Boolean = candidateIntents(query = null).any(::canResolve)

    fun openSearch(query: String? = null): Boolean {
        val intent = candidateIntents(query)
            .firstOrNull(::canResolve)
            ?: return false

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun canResolve(intent: Intent): Boolean =
        intent.resolveActivity(context.packageManager) != null

    private fun candidateIntents(query: String?): Sequence<Intent> =
        CANDIDATE_PACKAGES.asSequence().map { packageName ->
            Intent(ACTION_SEARCH)
                .setPackage(packageName)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .apply {
                    query?.trim()?.takeIf { it.isNotEmpty() }?.let {
                        putExtra(EXTRA_QUERY, it)
                    }
                }
        }

    companion object {
        const val ACTION_SEARCH = "com.goreecloud.index.action.SEARCH"
        const val EXTRA_QUERY = "com.goreecloud.index.extra.QUERY"

        val CANDIDATE_PACKAGES: List<String> = listOf(
            "com.goreecloud.index",
            "com.goreecloud.index.dev",
        )
    }
}
