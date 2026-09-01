# Saveable Theme Manager settings composition

Status: Development

`LauncherSettingsSurface` now composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## Remaining root wiring

`LauncherBetaRoot` still needs to replace its current direct Appearance theme-cycle behavior with `LauncherSettingsSurface` and use the provided Theme Manager callback. Keeping that final large-root edit separate preserves the existing review boundary and exact-head validation discipline.

This is Development evidence only and does not establish Stable Glaze UI or representative-device acceptance.
