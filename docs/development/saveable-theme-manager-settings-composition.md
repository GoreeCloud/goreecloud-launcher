# Saveable Theme Manager settings composition

Status: Development — GLAZE UI V1.1 migration in progress

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local native design authority targets **GLAZE UI V1.1 (`1.1.0`)** at exact Stable release source revision `15cc76d2bcd4065552dc31c77145b63f34d9e7b2`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## GLAZE UI V1.1 mapping

`GlazeMetrics` records machine version `1.1.0`, the exact Stable release source revision, inherited V1 spacing/radius values used by Launcher, separate V1.1 8/16/24/32 dp optical geometry references, the 48 dp normal interaction floor, and the 56 dp Touch Assistance / far-view target.

`GlazeTheme` preserves the inherited V1 Light/Dark structural palette and adds the explicit V1.1 Deep Dark structural appearance. `GlazeAtmosphere` separately records the bounded Deep Teal + Soft Amber atmospheric primitives so decorative atmosphere cannot become semantic-state authority.

Theme Manager is **Application** settings content under the inherited V1 System Shell contract. It is not Control Center, Universal Search, or a Critical System surface. Durable explanatory/settings content stays solid/certainty-first; bounded transient interaction chrome may use Glaze treatment where appropriate.

## Root composition

`LauncherBetaRoot` routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Theme Manager interaction targets and appearances

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark/Deep Dark appearance choices. This is a conservative accessible target and exceeds the 48 dp normal V1 floor. It does **not** claim that Launcher has implemented or detected a platform Touch Assistance preference; broader accessibility-resolution wiring remains separate work.

The already-selected appearance renders as a non-actionable `Selected` status surface rather than another persistence button. This prevents a redundant `onSelectThemeMode` callback for the current mode while preserving clear selected-state presentation. Only a different appearance choice can invoke the caller-owned persistence path.

The Deep Dark choice uses the exact V1.1 structural canvas/base/panel/text mapping rather than a pre-reset approximation. The preview's Soft Amber marker is deliberately decorative and sits inside the preview's cleared semantics tree; it communicates no application state.

## Accessibility semantics hardening

Each visual theme preview is exposed as one descriptive semantics node using stable catalog metadata such as `Deep Dark appearance preview`. Decorative preview internals are cleared from the accessibility tree so assistive technology receives one concise description instead of reading decorative fragments as independent content.

The non-actionable selected-state surface exposes an explicit state description such as `Deep Dark appearance selected` and uses a polite live region. A change to the selected appearance can therefore be announced as state rather than being misrepresented as another clickable control.

This is source-level semantic hardening only. It does not prove TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, 200% text/reflow, RTL, representative-device, or adaptive acceptance.

## V1.1 atmosphere and authority boundary

Atmosphere resolves after protected semantics and accessibility. Forced-color/native equivalents, Reduced Motion, Reduced Transparency, Increased Contrast/show-boundaries, large text/Touch Assistance, and material clarity all take precedence. Environmental sampling is not implemented. No color sample is persisted, transmitted, or used to infer meaning.

## Historical evidence

Earlier V1.0 and pre-reset Glaze UI 2.x source mappings and CI runs remain historical Development evidence only. They may support implementation ancestry but do not establish current V1.1 consumer acceptance or production eligibility.

## Remaining acceptance

This migration still requires complete V1.1 component/state/material review, Reduced Motion, Reduced Transparency, Increased Contrast, native accessibility-equivalent/forced-color handling where applicable, 200% text/reflow, RTL/localization expansion, platform Touch Assistance resolution, representative phone/tablet/foldable behavior, TalkBack/Switch Access, performance fallbacks, Human Visual Excellence, representative-device Theme Manager navigation/persistence testing across all four modes, production signing/distribution, release approval, and Stable qualification.

This remains Development evidence only. A green build or correct token/semantics mapping does not establish complete GLAZE UI V1.1 conformance or production readiness.
