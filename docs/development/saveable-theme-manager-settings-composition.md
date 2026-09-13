# Saveable Theme Manager settings composition

Status: Development — GLAZE UI V1.4 migration in progress

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local native design authority targets **GLAZE UI V1.4 (`1.4.0`)** at exact Stable merged source revision `84cb3db4884042f0fa25ed6d475a127fb110f596`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## GLAZE UI V1.4 mapping

`GlazeMetrics` records machine version `1.4.0`, the exact Stable merged source revision, inherited spacing/radius values used by Launcher, the 48 dp normal interaction floor, and the 56 dp Touch Assistance / far-view target.

`GlazeTheme` preserves the established Light/Dark/Deep Dark structural palette. `GlazeAtmosphere` records bounded Deep Teal + Soft Amber decorative primitives. `GlazeOpticalV14` adds the V1.4 Optical Intelligence resolver for bounded frost, semantic protection, chromatic depth, warmth, and environmental-memory influence.

The optical resolver is local and deterministic. It performs no telemetry, camera access, wallpaper-pixel sampling, analytics, or remote-context acquisition. Any future context adapter remains separately governed by Launcher privacy/security authority.

Theme Manager is **Application** settings content. It is not Control Center, Universal Search, a Critical System surface, or a source of privacy/security truth. Durable explanatory/settings content stays solid/certainty-first; bounded transient interaction chrome may use Glaze treatment where appropriate.

## Root composition

`LauncherBetaRoot` routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Theme Manager interaction targets and appearances

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark/Deep Dark appearance choices. This remains a conservative accessible target above the 48 dp normal floor. It does **not** claim that Launcher has implemented or detected a platform Touch Assistance preference; broader accessibility-resolution wiring remains separate work.

The already-selected appearance renders as a non-actionable `Selected` status surface rather than another persistence button. Only a different appearance choice can invoke the caller-owned persistence path.

The preview's Soft Amber marker remains deliberately decorative and communicates no application state.

## V1.4 accessibility and optical boundary

Forced Colors and Reduced Transparency must override optical expression. In the native resolver they produce `SOLID_ACCESSIBLE`, disable blur and decorative tint, and maximize semantic protection. Increased Contrast suppresses warmth and memory tint while increasing frost/semantic protection.

The Environmental Color Memory influence accepted by the resolver is capped at 8%. No actual wallpaper or content sampling is implemented by this component.

## Accessibility semantics hardening

Each visual theme preview is exposed as one descriptive semantics node using stable catalog metadata. Decorative preview internals are cleared from the accessibility tree so assistive technology receives one concise description instead of reading decorative fragments as independent content.

The non-actionable selected-state surface exposes an explicit state description and uses a polite live region. A change to the selected appearance can therefore be announced as state rather than being misrepresented as another clickable control.

This is source-level semantic hardening only. It does not prove TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, 200% text/reflow, RTL, representative-device, or adaptive acceptance.

## V1.4.1 human-validation boundary

Human optical review, subjective polish, manual assistive-technology verification, representative physical-device qualification, and real-device performance/thermal/power validation are assigned to the Glaze UI V1.4.1 hardening track. They are not represented as passed V1.4.0 evidence.

Launcher-specific product release requirements remain independent and may be stricter.

## Remaining acceptance

This migration still requires rendered V1.4 component/state/material review, accessibility fallback validation, 200% text/reflow, RTL/localization expansion, representative phone/tablet/foldable behavior, TalkBack/Switch Access, approved optical-context integration where beneficial, performance fallbacks, V1.4.1 human-validation work applicable to Launcher, representative-device Theme Manager navigation/persistence testing across all four modes, production signing/distribution, release approval, and Stable Launcher qualification.

This remains Development evidence only. A green build or correct source mapping does not establish complete GLAZE UI V1.4 consumer conformance or production readiness.
