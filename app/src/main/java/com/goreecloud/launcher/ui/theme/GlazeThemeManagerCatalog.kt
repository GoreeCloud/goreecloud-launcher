package com.goreecloud.launcher.ui.theme

data class GlazeThemeChoice(
    val mode: GlazeThemeMode,
    val title: String,
    val summary: String,
) {
    val previewAccessibilityLabel: String
        get() = "$title appearance preview"

    val selectedAccessibilityState: String
        get() = "$title appearance selected"
}

object GlazeThemeManagerCatalog {
    val choices: List<GlazeThemeChoice> = listOf(
        GlazeThemeChoice(
            mode = GlazeThemeMode.SYSTEM,
            title = "System",
            summary = "Follow the current Android light or dark appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.LIGHT,
            title = "Light",
            summary = "Use the GLAZE UI V1.1 light foundation regardless of system appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.DARK,
            title = "Dark",
            summary = "Use the GLAZE UI V1.1 dark foundation regardless of system appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.DEEP_DARK,
            title = "Deep Dark",
            summary = "Use the explicit GLAZE UI V1.1 Deep Dark structural appearance.",
        ),
    )

    fun choiceFor(mode: GlazeThemeMode): GlazeThemeChoice =
        choices.first { it.mode == mode }
}
