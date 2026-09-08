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
            summary = "Follow the current Android light or dark appearance with GLAZE UI V1.2 Frosted Neutral material.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.LIGHT,
            title = "Light",
            summary = "Use the GLAZE UI V1.2 Frosted Neutral light appearance regardless of system appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.DARK,
            title = "Dark",
            summary = "Use the GLAZE UI V1.2 Frosted Neutral dark appearance regardless of system appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.DEEP_DARK,
            title = "Deep Dark",
            summary = "Use the GLAZE UI V1.2 Deep Dark appearance with neutral frosted material and bounded accent.",
        ),
    )

    fun choiceFor(mode: GlazeThemeMode): GlazeThemeChoice =
        choices.first { it.mode == mode }
}
