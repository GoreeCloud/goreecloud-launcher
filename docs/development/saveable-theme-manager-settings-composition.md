# Saveable Theme Manager settings composition

Status: Development — GLAZE UI V1.4.1 migration in progress

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local native design authority targets **GLAZE UI V1.4.1 (`1.4.1`)** at exact Stable merged source revision `4fab9da0fad2e5c974e0e66ec88632c61745751c`. V1.4.0 remains the immediate rollback baseline.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and fail closed to Settings root. Root content receives one bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## GLAZE UI V1.4.1 mapping

`GlazeMetrics` records machine version `1.4.1`, exact Stable merged source revision `4fab9da0fad2e5c974e0e66ec88632c61745751c`, inherited spacing/radius values used by Launcher, the 48 dp normal interaction floor, and the 56 dp Touch Assistance / far-view target.

`GlazeTheme` preserves Light/Dark/Deep Dark structural palettes. `GlazeAtmosphere` retains bounded Deep Teal + Soft Amber decorative primitives. `GlazeOpticalV14` retains the bounded Optical Intelligence resolver for frost, semantic protection, chromatic depth, warmth, and environmental-memory influence.

The optical resolver remains local and deterministic. It performs no telemetry, camera access, wallpaper-pixel sampling, analytics, or remote-context acquisition. Any future context adapter remains separately governed by Launcher privacy/security authority.

Theme Manager is **Application** settings content. It is not Control Center, Universal Search, a Critical System surface, or a source of privacy/security truth. Durable explanatory/settings content stays solid/certainty-first; bounded transient interaction chrome may use Glaze treatment where appropriate.

## Theme Manager interaction and accessibility boundary

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark/Deep Dark choices. The already-selected appearance renders as a non-actionable `Selected` status surface. Only a different appearance choice can invoke caller-owned persistence.

Forced Colors and Reduced Transparency override optical expression and resolve to `SOLID_ACCESSIBLE`; Increased Contrast suppresses warmth and memory tint while increasing optical protection. Environmental Color Memory influence accepted by the resolver remains capped at 8%.

Each visual theme preview remains one descriptive semantics node using stable catalog metadata, with decorative preview internals excluded from the accessibility tree. The selected-state surface exposes an explicit state description and polite live region.

These are source-level semantics and mapping controls only. Shared V1.4.1 qualification does not establish Launcher-local TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, 200% text/reflow, RTL, representative-device, adaptive, performance, or Human Visual Excellence acceptance.

## Remaining acceptance

This migration still requires rendered component/state/material review, accessibility fallback validation, 200% text/reflow, RTL/localization, representative phone/tablet/foldable behavior, TalkBack/Switch Access, approved optical-context integration where beneficial, performance fallbacks, representative-device Theme Manager navigation/persistence testing across all four modes, production signing/distribution, release approval, and Stable Launcher qualification.

A green build or correct source mapping does not establish complete GLAZE UI V1.4.1 consumer conformance or production readiness.
