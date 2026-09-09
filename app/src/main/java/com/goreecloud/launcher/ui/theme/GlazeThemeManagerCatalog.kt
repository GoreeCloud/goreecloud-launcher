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
            summary = "Follow Android light or dark appearance with GLAZE UI V1.3 Adaptive Resonance and inherited Frosted Neutral material.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.LIGHT,
            title = "Light",
            summary = "Use the GLAZE UI V1.3 light appearance with inherited Frosted Neutral material regardless of system appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.DARK,
            title = "Dark",
            summary = "Use the GLAZE UI V1.3 dark appearance with inherited Frosted Neutral material regardless of system appearance.",
        ),
        GlazeThemeChoice(
            mode = GlazeThemeMode.DEEP_DARK,
            title = "Deep Dark",
            summary = "Use the GLAZE UI V1.3 Deep Dark appearance with inherited neutral frosted material and bounded accent.",
        ),
    )

    fun choiceFor(mode: GlazeThemeMode): GlazeThemeChoice =
        choices.first { it.mode == mode }
}
