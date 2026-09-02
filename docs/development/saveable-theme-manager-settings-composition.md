# Saveable Theme Manager settings composition

Status: Development

`LauncherSettingsSurface` now composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## Root composition

`LauncherBetaRoot` routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Theme Manager interaction targets

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` as the minimum height for its Done action and all actionable System/Light/Dark appearance choices. This keeps explicit Settings interactions aligned with the launcher's current Glaze touch-assistance contract instead of relying only on Material component defaults.

The already-selected appearance now renders as a non-actionable `Selected` status surface rather than another persistence button. This prevents a redundant `onSelectThemeMode` callback for the current mode while preserving the same minimum visual target height and clear selected-state presentation. Only a different appearance choice can invoke the caller-owned persistence path.

These interaction changes do not introduce additional theme modes, icon-pack discovery, icon masking, wallpaper-derived palettes, Deep Dark, expression controls, or new system-setting authority.

## Remaining acceptance

The branch requires exact-head Android validation after the latest interaction semantics change, plus representative-device Theme Manager navigation, persistence, accessibility, and Glaze UI acceptance before any Stable claim.

This is Development evidence only and does not establish Stable Glaze UI or representative-device acceptance.
