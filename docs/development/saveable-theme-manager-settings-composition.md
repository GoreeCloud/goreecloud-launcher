# Saveable Theme Manager settings composition

Status: Development — GLAZE UI V1.0 migration in progress

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local native design authority targets **GLAZE UI V1.0 (`1.0.0`)** at exact canonical source revision `70909bbdccad378fb7281ae1842e2f5beed64c38`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## GLAZE UI V1.0 mapping

`GlazeMetrics` records V1 machine version `1.0.0`, the exact canonical post-reset source revision, canonical V1 spacing values consumed by Launcher, mapped 12/20/28 dp foundation radius tiers, the 48 dp normal interaction floor, and the 56 dp Touch Assistance / far-view target.

`GlazeTheme` maps the reset V1 Light/Dark foundation palette. The mapping does not inherit pre-reset acceptance and does not treat old release anchors as current authority.

Theme Manager is **Application** settings content under the V1 System Shell contract. It is not Control Center, Universal Search, or a Critical System surface. Durable explanatory/settings content stays solid/certainty-first; bounded transient interaction chrome may use Glaze treatment where appropriate.

Deep Dark is required by V1 but remains deliberately unimplemented in Launcher rather than being approximated from a pre-reset palette.

## Root composition

`LauncherBetaRoot` routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Theme Manager interaction targets

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark appearance choices. This is a conservative accessible target and exceeds the 48 dp normal V1 floor. It does **not** claim that Launcher has implemented or detected a platform Touch Assistance preference; broader accessibility-resolution wiring remains separate work.

The already-selected appearance renders as a non-actionable `Selected` status surface rather than another persistence button. This prevents a redundant `onSelectThemeMode` callback for the current mode while preserving clear selected-state presentation. Only a different appearance choice can invoke the caller-owned persistence path.

## Accessibility semantics hardening

Each visual theme preview is exposed as one descriptive semantics node using stable catalog metadata such as `Dark appearance preview`. Decorative preview internals are cleared from the accessibility tree for that preview so assistive technology receives one concise description instead of reading decorative fragments as independent content.

The non-actionable selected-state surface exposes an explicit state description such as `Dark appearance selected` and uses a polite live region. A change to the selected appearance can therefore be announced as state rather than being misrepresented as another clickable control.

This is source-level semantic hardening only. It does not prove TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, 200% text/reflow, RTL, representative-device, or adaptive acceptance.

## Pre-reset evidence

Earlier Glaze UI 2.x source mappings and CI runs remain historical Development evidence only. They may support implementation ancestry but do not establish current V1 mapping, consumer acceptance, or production eligibility.

## Remaining acceptance

This migration still requires complete V1 component mapping, state-priority review, Deep Dark, Reduced Motion, Reduced Transparency, Increased Contrast, native accessibility-equivalent/forced-color handling where applicable, 200% text/reflow, RTL/localization expansion, platform Touch Assistance resolution, material-role review across Launcher, representative phone/tablet/foldable behavior, TalkBack/Switch Access, performance fallbacks, Human Visual Excellence, representative-device Theme Manager navigation/persistence testing, production signing/distribution, release approval, and Stable qualification.

This remains Development evidence only. A green build or correct token/semantics mapping does not establish complete GLAZE UI V1.0 conformance or production readiness.
