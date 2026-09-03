# GLAZE UI V1.0 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.0 (`1.0.0`)**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact V1 source authority: `70909bbdccad378fb7281ae1842e2f5beed64c38`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic mapping  
Automated contract: `scripts/check_glaze_ui.py`

GLAZE UI V1.0 is the official and only current GoreeCloud design-system target. This record defines Launcher’s repository-local post-reset mapping. It does **not** establish production acceptance, complete V1 consumer conformance, release approval, representative-device acceptance, or Stable qualification. No pre-reset Glaze UI acceptance is inherited as V1 evidence.

## Authority boundary

The implementation-facing authority is the exact canonical V1 source revision above, including `VERSION`, `GLAZE_UI_V1_0.md`, `registry/lifecycle.json`, the V1 foundation/component/System Shell contracts, `acceptance/v1.0-stable.md`, and `scripts/validate_glaze_v1.py`.

Launcher maps applicable V1 semantics into first-party Jetpack Compose code. It does not embed a remote UI runtime or create a competing design-system authority.

## Implemented V1 source mapping

The current Development branch maps:

- canonical V1 spacing values 4/8/12/16/20/24/32/48/64 dp where directly consumed;
- V1 foundation radius tiers of 12 dp small, 20 dp standard, 28 dp soft/panel, and pill;
- the 48 dp normal touch-oriented interaction floor;
- the 56 dp Touch Assistance / far-view target where applicable;
- V1 Light/Dark foundation colors, including `#3478F6` / `#8DB5FF` focus identity and the reset canvas/base/text roles;
- the existing local System/Light/Dark Theme Manager flow using the V1 mapping; and
- the existing saveable Settings → Theme Manager composition without broadening Launcher authority.

`GlazeMetrics.kt` pins target version `1.0.0` and exact canonical source revision `70909bbdccad378fb7281ae1842e2f5beed64c38`. `GlazeTheme.kt` maps the current V1 Light/Dark foundations. Existing Launcher property names that remain useful for source compatibility are mapped onto V1 semantic tiers rather than treated as retired canonical tokens.

`space10` remains a Launcher-owned 40 dp layout convenience. It is not claimed as a canonical V1 spacing token.

## V1 System Shell classification

Launcher Home is a **Workspace** presentation surface. Launcher Settings and Theme Manager are **Application** surfaces. These classifications affect presentation and interaction only; they grant no operating-system, window-manager, Control Center, notification, authentication, search-indexing, or other system authority.

GoreeCloud Index remains the universal-search/indexing authority. Theme Manager is not Universal Search, Control Center, a Critical System surface, Signature, or Intelligence merely because V1 defines those concepts.

## Presentation rule

Launcher follows the V1 rule: **Solid where users read or make explicit critical decisions. Glazed where users interact with transient navigation, command, search, control, or feedback chrome.**

Durable settings, explanatory content, and destructive or security-sensitive decisions must remain certainty-first. Glaze presentation must never manufacture authorization, privacy, security, backup, recovery, identity, or trust state owned by another GoreeCloud system or Android.

## Theme Manager boundary

The reachable Theme Manager remains bounded to System, Light, and Dark. Settings navigation is saveable, unknown/stale destination values fail closed to Settings root, and only a different appearance choice may invoke caller-owned persistence.

Deep Dark is a required V1 appearance mode but remains an explicit Launcher implementation/acceptance gap. It is not approximated with a pre-reset palette. Calm/Balanced/Expressive profiles, wallpaper-derived/user-selected palettes, icon-pack discovery/application, icon masking, per-app icon replacement, and broader Theme Engine behavior remain separate work.

## Accessibility and resilience boundary

The V1 source migration does not by itself establish:

- Reduced Motion behavior across all Launcher surfaces;
- Reduced Transparency / solid fallback resolution;
- Increased Contrast and forced-colors/native equivalents;
- 200% text/reflow;
- RTL/localization;
- platform Touch Assistance preference resolution;
- TalkBack, Switch Access, keyboard/D-pad focus and announcement timing;
- phone/tablet/foldable adaptive composition;
- performance/power fallback behavior; or
- Human Visual Excellence acceptance.

The 48/56 dp target mapping is source evidence only. Theme Manager currently uses the 56 dp target conservatively for primary appearance actions; that does not prove platform Touch Assistance detection or complete accessibility acceptance.

## Motion boundary

Historical Glaze Motion evaluation remains test-only Development history. Glaze Motion is a separately governed experimental subsystem unless explicitly incorporated into a future V1.x contract. It is not a production dependency and does not establish V1 conformance.

## Historical evidence boundary

Pre-reset Glaze UI 2.x/earlier adoption commits, pull requests, CI runs, and discussion remain immutable Git/changelog audit history. They may explain implementation ancestry, but they do not define the current target and do not satisfy V1 acceptance.

## Acceptance still required

- Complete V1 component/state mapping applicable to Launcher.
- Complete V1 material-role review across Home, Apps, Settings, Theme Manager, dialogs, and workspace editing.
- Deep Dark plus required accessibility and resilience modes.
- 200% text/reflow, RTL/localization, and Touch Assistance resolution.
- TalkBack, Switch Access, keyboard/D-pad/focus-order, and spoken-announcement acceptance.
- Representative phone/tablet/foldable adaptive composition.
- Representative physical-device Theme Manager navigation/persistence and Home/Apps/Settings acceptance.
- Performance/power fallback evidence and Human Visual Excellence review.
- Required Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, and Index integration acceptance.
- Exact-head CI, production signing/distribution, release approval, and Stable qualification.

Passing source, unit, build, schema, emulator, or registry checks remains Development evidence only.

## Rollback

If the V1 migration causes a regression, revert the exact Launcher V1 migration commit or merge to the prior accepted Launcher revision. Do not weaken the canonical V1 contract, and do not select a retired Glaze product version as the current rollback target.
