# GLAZE UI V1.1 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.1 (`1.1.0`)**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact Stable release source authority: `15cc76d2bcd4065552dc31c77145b63f34d9e7b2`  
Stable release tag: `v1.1.0`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic/structural mapping with bounded optical refinement  
Automated contract: `scripts/check_glaze_ui.py`

GLAZE UI V1.1 is the sole current GoreeCloud design-system adoption target. This record defines Launcher’s repository-local V1.1 Development mapping. It does **not** establish complete V1.1 consumer conformance, rendered/accessibility/device acceptance, production approval, release approval, or Stable qualification. No V1.0 or pre-reset acceptance is inherited as V1.1 acceptance.

## Authority boundary

The implementation-facing authority is the exact Stable V1.1 release above, including `GLAZE_UI_V1_1.md`, `contracts/v1.1/optical-refinement.json`, `tokens/glaze-v1.1-atmosphere.json`, `css/glaze-v1.1-appearance.css`, `acceptance/v1.1-stable.md`, and the current V1 validator.

Launcher maps applicable V1.1 contracts into first-party Jetpack Compose code. It does not embed a remote UI runtime or create a competing design-system authority.

## Implemented V1.1 source mapping

The current Development branch:

- pins machine version `1.1.0` and exact Stable release revision `15cc76d2bcd4065552dc31c77145b63f34d9e7b2`;
- preserves inherited V1 spacing values 4/8/12/16/20/24/32/48/64 dp where directly consumed;
- preserves inherited V1 structural radius tiers of 12 dp small, 20 dp standard, 28 dp soft/panel, and pill;
- records V1.1 optical geometry references of 8/16/24/32 dp plus capsule separately from structural radii;
- preserves the 48 dp normal touch-oriented interaction floor and 56 dp Touch Assistance / far-view target;
- preserves the inherited Light/Dark structural appearance values;
- adds the explicit V1.1 Deep Dark structural appearance using canvas `#05070A`, base `#0D1015`, panel-equivalent `rgba(18,22,29,0.90)`, primary text `#F5F7FA`, and secondary text `#ABB4C2`;
- exposes System, Light, Dark, and Deep Dark through the existing device-local Theme Manager persistence path; and
- records the V1.1 Deep Teal + Soft Amber atmospheric primitives separately from Material semantic colors.

`GlazeAtmosphere.kt` is deliberately non-semantic. Teal/amber atmosphere cannot establish protected, privacy, identity, recovery, availability, focus, selection, or other authoritative state. The current Theme Manager preview uses only a small static Soft Amber decorative counter-light. It does not implement content sampling, persistent Environmental Color Memory, animated atmosphere, or remote color derivation.

`space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.

## V1.1 authority resolution

Launcher preserves the V1.1 priority order: producer-authoritative protected semantics, forced-color/native accessibility resolution, Reduced Motion, Reduced Transparency, Increased Contrast/show-boundaries, large text/Touch Assistance, material clarity/platform capability, and only then atmospheric/application expression.

Atmosphere is therefore removable. Meaning, actions, focus, state, and hierarchy must remain intact without it.

## System Shell classification

Launcher Home is a **Workspace** presentation surface. Launcher Settings and Theme Manager are **Application** surfaces. These classifications affect presentation and interaction only; they grant no operating-system, window-manager, Control Center, notification, authentication, search-indexing, or other system authority.

GoreeCloud Index remains the universal-search/indexing authority. Theme Manager is not Universal Search, Control Center, a Critical System surface, Signature, or Intelligence merely because Glaze defines those concepts.

## Presentation rule

Launcher follows the inherited V1 rule: **Solid where users read or make explicit critical decisions. Glazed where users interact with transient navigation, command, search, control, or feedback chrome.**

Durable settings, explanatory content, and destructive or security-sensitive decisions remain certainty-first. V1.1 atmosphere must never manufacture authorization, privacy, security, backup, recovery, identity, or trust state owned by another GoreeCloud system or Android.

## Theme Manager boundary

The reachable Theme Manager now supports System, Light, Dark, and Deep Dark. Settings navigation remains saveable, unknown/stale destination values fail closed to Settings root, and only a different appearance choice may invoke caller-owned persistence.

Deep Dark is now implemented as a source-level structural appearance instead of being approximated from a retired palette. This does not prove Deep Dark rendered contrast/accessibility acceptance. Calm/Balanced/Expressive profiles, wallpaper-derived/user-selected palettes, icon-pack discovery/application, icon masking, per-app icon replacement, Environmental Color Memory, and broader Theme Engine behavior remain separate work.

## Accessibility and resilience boundary

The V1.1 source migration does not by itself establish:

- Reduced Motion behavior across all Launcher surfaces;
- Reduced Transparency / solid fallback resolution;
- Increased Contrast and forced-colors/native equivalents;
- 200% text/reflow;
- RTL/localization;
- platform Touch Assistance preference resolution;
- TalkBack, Switch Access, keyboard/D-pad focus and announcement timing;
- phone/tablet/foldable adaptive composition;
- performance/power fallback behavior;
- complete V1.1 atmospheric degradation behavior; or
- Human Visual Excellence acceptance.

The 48/56 dp target mapping is source evidence only. Theme Manager uses the 56 dp target conservatively for primary appearance actions; that does not prove platform Touch Assistance detection or complete accessibility acceptance.

## Motion boundary

Glaze Motion remains separately Experimental. This migration introduces no Glaze Motion production dependency and no animated atmospheric requirement.

## Historical evidence boundary

V1.0 and pre-reset Glaze UI 2.x adoption commits, pull requests, CI runs, and discussion remain immutable development history. They may explain implementation ancestry, but they do not define the current target and do not satisfy V1.1 application acceptance.

## Acceptance still required

- Complete V1.1 component/state/material mapping applicable to Launcher.
- Rendered review of Light, Dark, and Deep Dark across Home, Apps, Settings, Theme Manager, dialogs, and workspace editing.
- Reduced Motion, Reduced Transparency, Increased Contrast, forced-color/native-equivalent behavior, and degradation-order validation.
- 200% text/reflow, RTL/localization, and Touch Assistance resolution.
- TalkBack, Switch Access, keyboard/D-pad/focus-order, and spoken-announcement acceptance.
- Representative phone/tablet/foldable adaptive composition.
- Representative physical-device Theme Manager navigation/persistence and Home/Apps/Settings acceptance.
- Performance/power fallback evidence and Human Visual Excellence review.
- Required Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, Manager, and Index integration acceptance.
- Exact-head CI, production signing/distribution, release approval, and Stable qualification.

Passing source, unit, build, schema, emulator, or registry checks remains Development evidence only.

## Rollback

If this migration causes a regression, revert the exact Launcher V1.1 migration candidate to the prior validated V1.0 Development head. That is a source rollback boundary, not authority to relabel V1.0 as the current design-system target. Do not weaken the canonical V1.1 contract to preserve a local implementation.
