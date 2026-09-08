# Saveable Theme Manager settings composition

Status: Development — GLAZE UI V1.2 migration in progress

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local native design authority targets **GLAZE UI V1.2 (`1.2.0`)** at exact Stable release source revision `f285b9145e27e6e7027b075c37299d101945c272`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## GLAZE UI V1.2 mapping

`GlazeMetrics` records machine version `1.2.0`, the exact Stable release source revision, inherited spacing/radius values used by Launcher, 8/16/24/32 dp optical geometry references, the 48 dp normal interaction floor, and the 56 dp Touch Assistance / far-view target.

`GlazeTheme` keeps System/Light/Dark/Deep Dark structural appearance support while moving active material surfaces to V1.2 Frosted Neutral. `GlazeAtmosphere` contains only neutral frosted material references, neutral degraded-effect outlines, and bounded accent references. The prior Deep Teal + Soft Amber aura substrate is retired from active Launcher presentation.

Theme Manager is **Application** settings content. It is not Control Center, Universal Search, a Critical System surface, or an authorization boundary. Durable explanatory/settings content stays certainty-first; bounded transient interaction chrome may use neutral Glaze treatment where appropriate.

## Root composition

`LauncherBetaRoot` routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Theme Manager interaction targets and appearances

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark/Deep Dark appearance choices. This exceeds the 48 dp normal interaction floor. It does **not** claim that Launcher has implemented or detected a platform Touch Assistance preference; broader accessibility-resolution wiring remains separate work.

The already-selected appearance renders as a non-actionable `Selected` status surface rather than another persistence button. Only a different appearance choice can invoke the caller-owned persistence path.

The current preview uses Frosted Neutral structure with a single bounded Ice Blue accent marker. The accent sits inside the preview's cleared semantics tree and communicates no application state.

## Accessibility semantics hardening

Each visual theme preview is exposed as one descriptive semantics node using stable catalog metadata such as `Deep Dark appearance preview`. Decorative preview internals are cleared from the accessibility tree so assistive technology receives one concise description instead of reading decorative fragments as independent content.

The non-actionable selected-state surface exposes an explicit state description such as `Deep Dark appearance selected` and uses a polite live region. A change to the selected appearance can therefore be announced as state rather than being misrepresented as another clickable control.

This is source-level semantic hardening only. It does not prove TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, 200% text/reflow, RTL, representative-device, or adaptive acceptance.

## V1.2 Frosted Neutral and authority boundary

**Neutral glass is the material; color is an accent.** Protected semantics and accessibility resolve before decorative material. Forced-color/native equivalents, Reduced Motion, Reduced Transparency, Increased Contrast/show-boundaries, large text/Touch Assistance, and platform capability all take precedence.

Environmental sampling is not implemented. No color sample is persisted, transmitted, or used to infer meaning. Removing transparency or decorative accent must leave meaning, actions, focus, state, and hierarchy intact.

## Historical evidence

Earlier V1.1/V1.0 and pre-reset Glaze UI 2.x source mappings and CI runs remain historical Development evidence only. They may support implementation ancestry but do not establish current V1.2 consumer acceptance or production eligibility.

## Remaining acceptance

This migration still requires complete V1.2 component/state/material review, Reduced Motion, Reduced Transparency, Increased Contrast, native accessibility-equivalent/forced-color handling where applicable, 200% text/reflow, RTL/localization expansion, platform Touch Assistance resolution, representative phone/tablet/foldable behavior, TalkBack/Switch Access, performance fallbacks, Human Visual Excellence, representative-device Theme Manager navigation/persistence testing across all four modes, production signing/distribution, release approval, and Release Candidate qualification.

This remains Development evidence only. A green build or correct token/semantics mapping does not establish complete GLAZE UI V1.2 conformance or production readiness.
