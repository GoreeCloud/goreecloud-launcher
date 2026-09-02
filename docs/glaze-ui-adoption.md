# Glaze UI 2.2 Adoption Candidate — GoreeCloud Launcher

Status: **Adoption Candidate**  
Required Stable baseline: **Glaze UI 2.2.0**  
Reviewed exact Stable promotion head: `fb5ecde4a8258503789ffde08ac46a2e524ef71e`  
Reviewed Stable release merge: `6731098b28dd0393faa878c70d989a221d714a20`  
Reviewed Stable tag: `v2.2.0`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic mapping  
Automated contract: `scripts/check_glaze_ui.py`

Glaze UI 2.2.0 Stable is the production design-system authority. This record establishes repository-local current-Stable Adoption Candidate evidence only; it does not certify GoreeCloud Launcher for production, current-Stable alignment, or complete Glaze UI 2.2 conformance.

## Implemented 2.2 mapping

GoreeCloud Launcher maps the Stable semantics it consumes into its first-party Jetpack Compose implementation rather than embedding a web runtime or third-party launcher/theme engine.

The current mapping includes:

- canonical spacing keys `space.1`, `space.2`, `space.3`, `space.4`, `space.5`, `space.6`, `space.8`, `space.10`, `space.12`, and `space.16`, mapped to 4/8/12/16/20/24/32/40/48/64 dp;
- `radius.sm`, `radius.md`, `radius.control`, `radius.lg`, `radius.xl`, `radius.2xl`, and `radius.pill`, mapped to 10/14/16/22/28/32/999 dp;
- the 48 dp `currentContract.touchMinimum` interaction floor and 56 dp `currentContract.touchAssistanceMinimum` target;
- the ordinary System Glaze budget of one dominant panel, at most three small floating controls, and no nested backdrop blur;
- canonical Light and Dark foundation/identity colors retained by Glaze UI 2.2 Stable;
- direct persisted System/Light/Dark selection through the existing local theme repository;
- the native Theme Manager catalog, previews, selected-state semantics, and Settings navigation composition already implemented on the active Development branch; and
- deterministic explicit non-drag ordering controls alongside the existing application-owned direct-manipulation ordering authority.

The compatibility token `target.minimum=44` remains historical compatibility semantics only. Launcher does not lower the active mobile interaction floor below 48 dp.

`GlazeMetrics.kt` records the exact 2.2 Stable version, promotion head, release revision, compatible spacing/radius values, interaction floors, and System Glaze budget. `GlazeTheme.kt` carries the bounded Light/Dark mapping. `ThemeManagerSurface.kt`, `GlazeThemeManagerCatalog.kt`, and `LauncherSettingsSurface.kt` provide the current first-party Theme Manager presentation and Settings composition.

## 2.2 System Shell classification boundary

Launcher Home is treated as a **Workspace** presentation surface under the Glaze UI 2.2 System Shell hierarchy: an application-owned user environment whose durable readable content remains certainty-first rather than an excuse for pervasive translucency. Launcher Settings and Theme Manager are **Application** surfaces. These presentation classifications do not grant operating-system, window-manager, Control Center, notification, authentication, or other system authority.

Theme Manager is not Universal Search, Control Center, a Critical System surface, a Signature component, or an Intelligence component merely because Glaze UI 2.2 defines those concepts. GoreeCloud Index remains the universal-search/indexing authority and Launcher remains an invocation/presentation consumer of that authority.

The current mapping records the ordinary System Glaze composition budget but does not claim complete budget acceptance across every Launcher surface. Nested backdrop blur remains prohibited by the mapped current contract.

## Theme Manager boundary

The reachable Theme Manager remains bounded to the already implemented System, Light, and Dark appearance modes. Settings navigation is saveable and unknown/stale destination values fail closed to Settings root. The selected appearance is represented as non-actionable state; only a different appearance choice may invoke caller-owned persistence.

Theme preview semantics are derived from the same bounded catalog as visible mode metadata. Current Theme Manager actions use the mapped 56 dp Touch Assistance target as a conservative minimum, but Launcher does not claim platform Touch Assistance preference detection or complete Accessibility Resolution Matrix integration.

Deep Dark is not approximated with an invented palette. Wallpaper-derived/user-selected palettes, expression controls, third-party icon-pack discovery/application, icon masking, per-app icon replacement, and complete Glaze Theme Engine behavior remain separate implementation and acceptance work.

## Material, accessibility, and adaptive boundary

Glaze UI 2.2 retains the Canvas → Surface → Soft Glaze → Glaze → Deep Glaze → Live Glaze hierarchy and adds current System Shell, component-tier, Optical Reachability, adaptive, accessibility-resolution, material-budget, and performance requirements.

This Adoption Candidate does not establish complete material-role/component coverage; Material Clarity or performance fallback behavior; Deep Dark; Reduced Motion; Reduced Transparency/Solid resolution; Increased Contrast; forced-colors/native semantic equivalents; 200% text/reflow; RTL/localization; platform Touch Assistance resolution; TalkBack/Switch Access; keyboard/D-pad focus acceptance; phone/tablet/foldable composition; representative physical-device behavior; or Human Visual Excellence acceptance.

Tablet and large-screen Launcher composition must not become a stretched phone surface. Home, Apps, Settings, Theme Manager, Index invocation, dialogs, and workspace editing require appropriate window/posture-aware behavior where applicable before production qualification.

## Motion quarantine

The historical Glaze Motion 0.4 evaluation remains test-only development evidence. Glaze Motion remains Experimental and is not a production dependency. It does not replace Glaze UI 2.2 Stable, cannot establish current-Stable alignment, and cannot make Launcher production eligible.

## Privacy and authority boundary

The 2.2 migration changes presentation/governance evidence only. It introduces no Android network permission, analytics, telemetry, advertising, sponsorship, remote UI runtime, account dependency, workspace-placement authority, wallpaper mutation authority, icon-pack authority, Control Center authority, or system-setting authority. Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, GoreeCloud Mesh, GoreeCloud Index, and Android platform authorities retain their existing responsibilities.

## Historical migration evidence

Glaze UI 2.1.0, 2.0.0, and earlier 1.x adoption records remain historical migration/rollback evidence only. They do not satisfy the current Stable 2.2.0 production design-system requirement.

## Acceptance still required

- Complete Glaze UI 2.2 component/state mapping applicable to Launcher.
- Complete System Glaze/material-budget review across Launcher surfaces.
- Reduced Motion, Reduced Transparency, Increased Contrast, and forced-colors/native accessibility-equivalent behavior.
- 200% text/reflow, RTL/localization, and platform Touch Assistance resolution.
- TalkBack, Switch Access, keyboard/D-pad/focus-order, and spoken-announcement timing acceptance.
- Representative phone/tablet/foldable adaptive composition.
- Representative physical-device Theme Manager navigation/persistence and Home/Apps/Settings acceptance.
- Performance/power fallback evidence and Human Visual Excellence review.
- Required Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, and Index integration acceptance.
- Production signing/distribution, release approval, and Stable qualification.

Passing source, unit, build, schema, emulator, or central registry checks remains Development evidence only.