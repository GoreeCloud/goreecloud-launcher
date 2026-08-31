package com.goreecloud.launcher.core.workspace.db

data class WorkspaceHomePageContext(
    val appCount: Int,
    val unsupportedItemCount: Int,
) {
    init {
        require(appCount >= 0)
        require(unsupportedItemCount >= 0)
    }

    val compactLabel: String
        get() = when {
            unsupportedItemCount > 0 -> "$appCount app${if (appCount == 1) "" else "s"} · $unsupportedItemCount other"
            else -> "$appCount app${if (appCount == 1) "" else "s"}"
        }

    fun moveTargetLabel(pageNumber: Int): String {
        require(pageNumber >= 1)
        return "Page $pageNumber · $compactLabel"
    }
}

fun WorkspaceRenderedHomePage.context(): WorkspaceHomePageContext = WorkspaceHomePageContext(
    appCount = appKeys.size,
    unsupportedItemCount = unsupportedItemCount,
)
