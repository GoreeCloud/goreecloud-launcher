# Saveable Theme Manager settings composition

Status: Development — GLAZE UI V1.5.1 migration in progress

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local active presentation authority targets **GLAZE UI V1.5 (`1.5.1`)** at exact Stable merged revision `98da57064ede0f334627b632bc16801f580331af` and reviewed implementation anchor `ee1032a0822ab8e103f8afe48e5c1859fde65cc9`. The inherited optical/material implementation remains V1.4.1 at `4fab9da0fad2e5c974e0e66ec88632c61745751c`; the immediate shared Stable rollback release is V1.5.0 at `b7fa8164bfdeaa1dc0acb21b770e7601120da04e`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and fail closed to Settings root. Root content receives one bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, authorization, or system-setting authority.

## GLAZE UI V1.5 presentation mapping

`GlazeCapabilityV15` is the Launcher-native projection of the V1.5 Context + Capability Awareness presentation contract. It consumes already-authoritative capability state and fails closed for missing, temporarily unavailable, restricted, unknown, or conflicting required capabilities. It cannot infer authorization, choose provider precedence, request permission, navigate, execute a consequential action, or execute a fallback automatically.

The Theme Manager itself does not require cross-service capability inference for ordinary local appearance selection. Its settings action remains Launcher-owned application behavior, while any future capability-dependent controls must use producer-owned state rather than deriving authority from Glaze presentation.

## Inherited V1.4.1 optical mapping

`GlazeMetrics` and `GlazeOpticalV14` remain the explicit inherited V1.4.1 optical/material layer. They preserve inherited spacing/radius values, the 48 dp normal interaction floor, the 56 dp Touch Assistance / far-view target, and the bounded Optical Intelligence behavior used by Launcher.

`GlazeTheme` preserves Light/Dark/Deep Dark structural palettes. `GlazeAtmosphere` retains bounded Deep Teal + Soft Amber decorative primitives. `GlazeOpticalV14` retains the deterministic resolver for frost, semantic protection, chromatic depth, warmth, and environmental-memory influence.

The optical resolver remains local and deterministic. It performs no telemetry, camera access, wallpaper-pixel sampling, analytics, or remote-context acquisition. Any future context adapter remains separately governed by Launcher privacy/security authority.

Theme Manager is **Application** settings content. It is not Control Center, Universal Search, a Critical System surface, or a source of privacy/security/identity truth. Durable explanatory/settings content stays solid/certainty-first; bounded transient interaction chrome may use Glaze treatment where appropriate.

## Theme Manager interaction and accessibility boundary

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark/Deep Dark choices. The already-selected appearance renders as a non-actionable `Selected` status surface. Only a different appearance choice can invoke caller-owned persistence.

Forced Colors and Reduced Transparency override optical expression and resolve to `SOLID_ACCESSIBLE`; Increased Contrast suppresses warmth and memory tint while increasing optical protection. Environmental Color Memory influence accepted by the inherited resolver remains capped at 8%.

Each visual theme preview remains one descriptive semantics node using stable catalog metadata, with decorative preview internals excluded from the accessibility tree. The selected-state surface exposes an explicit state description and polite live region.

These are source-level semantics and mapping controls only. Shared V1.5.1 qualification does not establish Launcher-local TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, 200% text/reflow, RTL, representative-device, adaptive, performance, or Human Visual Excellence acceptance.

## Remaining acceptance

This migration still requires rendered component/state/material review, capability-state explanation review where applicable, accessibility fallback validation, 200% text/reflow, RTL/localization, representative phone/tablet/foldable behavior, TalkBack/Switch Access, approved optical-context integration where beneficial, performance fallbacks, representative-device Theme Manager navigation/persistence testing across all four modes, production signing/distribution, release approval, and Stable Launcher qualification.

Shared V1.5.1 now contains accepted Glaze-wide `performance-representative-budget` and `platform-posture-continuity` qualification evidence. Those results remain Glaze-level evidence tied to their reviewed revisions and environments; they do not remove Launcher-specific performance, power, supported-form-factor, posture, Android-runtime, or device acceptance obligations.

A green build or correct source mapping does not establish complete GLAZE UI V1.5 consumer conformance or production readiness.
