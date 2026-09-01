# Saveable Theme Manager settings composition

Status: Development

`LauncherSettingsSurface` now composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## Root composition

`LauncherBetaRoot` now routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Remaining acceptance

The branch still requires fresh exact-head Android validation after this documentation reconciliation, plus representative-device Theme Manager navigation, persistence, accessibility, and Glaze UI acceptance before any Stable claim.

This is Development evidence only and does not establish Stable Glaze UI or representative-device acceptance.
