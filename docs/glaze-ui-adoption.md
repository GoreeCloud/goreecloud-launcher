# GLAZE UI V1.2 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.2 (`1.2.0`)**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact Stable release source authority: `f285b9145e27e6e7027b075c37299d101945c272`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic/structural mapping with Frosted Neutral material  
Automated contract: `scripts/check_glaze_ui.py`

GLAZE UI V1.2 is the current GoreeCloud design-system target. This record defines Launcher’s repository-local V1.2 Development mapping. It does **not** establish complete V1.2 consumer conformance, rendered/accessibility/device acceptance, production approval, release approval, Release Candidate entry, or Stable qualification. Earlier V1.1/V1.0 evidence remains historical only.

## Authority boundary

The implementation-facing authority is the exact V1.2 Stable promotion revision above, including the V1.2 Frosted Neutral, optical, interaction, responsive, accessibility, and lifecycle contracts in `GoreeCloud/goreecloud-glaze-ui`.

Launcher maps applicable V1.2 contracts into first-party Jetpack Compose code. It does not embed a remote UI runtime or create a competing design-system authority.

## Implemented V1.2 source mapping

The current Development branch:

- pins machine version `1.2.0` and exact Stable release revision `f285b9145e27e6e7027b075c37299d101945c272`;
- preserves inherited spacing values 4/8/12/16/20/24/32/48/64 dp where directly consumed;
- preserves the structural radius tiers currently consumed by Launcher;
- preserves 8/16/24/32 dp optical geometry references plus capsule geometry;
- preserves the 48 dp normal touch-oriented interaction floor and 56 dp Touch Assistance / far-view target;
- retains System, Light, Dark, and Deep Dark through the existing device-local Theme Manager persistence path;
- replaces the earlier Deep Teal + Soft Amber aura substrate with neutral frosted material references;
- maps light material to translucent Frost White and dark/deep-dark material to translucent neutral graphite;
- uses neutral separator lines for degraded/effects-free presentation; and
- limits the current Theme Manager preview to one bounded Ice Blue decorative accent.

The governing V1.2 material rule is: **Neutral glass is the material; color is an accent.** Decorative color must never establish protection, privacy, identity, recovery, availability, focus, selection, or other authoritative state.

`space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.

## System Shell classification

Launcher Home is a **Workspace** presentation surface. Launcher Settings and Theme Manager are **Application** surfaces. These classifications affect presentation and interaction only; they grant no operating-system, Control Center, notification, authentication, search-indexing, or other system authority.

GoreeCloud Index remains the universal-search/indexing authority. Theme Manager is not Universal Search, Control Center, a Critical System surface, or an authorization boundary merely because Glaze defines presentation concepts for those surfaces.

## Frosted Neutral presentation rule

Durable reading and explicit decisions remain certainty-first. Transient navigation, control, search, and feedback chrome may use neutral glazed treatment where appropriate. Color is reserved for bounded identity/accent use rather than becoming the material substrate.

Reduced Transparency, Increased Contrast, forced-color/native equivalents, accessibility state, and producer-owned semantic state take precedence over material effects. If a frosted effect degrades or is removed, meaning, actions, focus, state, and hierarchy must remain intact.

## Theme Manager boundary

The reachable Theme Manager supports System, Light, Dark, and Deep Dark. Settings navigation remains saveable, unknown/stale destination values fail closed to Settings root, and only a different appearance choice may invoke caller-owned persistence.

The preview uses neutral material plus one Ice Blue accent marker. It does not sample wallpaper/content, persist Environmental Color Memory, animate atmospheric fields, infer semantic state from color, or create remote color dependencies.

## Accessibility and resilience boundary

The V1.2 source migration does not by itself establish:

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

The 48/56 dp target mapping is source evidence only. Theme Manager uses the 56 dp target conservatively for primary appearance actions; that does not prove platform Touch Assistance detection or complete accessibility acceptance.

## Platform-system boundary

Glaze UI governs presentation and interaction only. This migration does not manufacture GoreeCloud Manager, Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Mesh, GoreeCloud Identity, or GoreeCloud Index integration. Each remains independently evidence-gated.

## Historical evidence boundary

V1.1, V1.0, and pre-reset Glaze UI 2.x adoption commits, pull requests, CI runs, and discussion remain immutable development history. They may explain implementation ancestry, but they do not define the current target and do not satisfy V1.2 application acceptance.

## Acceptance still required

- Complete V1.2 component/state/material mapping applicable to Launcher.
- Rendered review of Light, Dark, and Deep Dark across Home, Apps, Settings, Theme Manager, dialogs, and workspace editing.
- Reduced Motion, Reduced Transparency, Increased Contrast, forced-color/native-equivalent behavior, and degradation-order validation.
- 200% text/reflow, RTL/localization, and Touch Assistance resolution.
- TalkBack, Switch Access, keyboard/D-pad/focus-order, and spoken-announcement acceptance.
- Representative phone/tablet/foldable adaptive composition.
- Representative physical-device Theme Manager navigation/persistence and Home/Apps/Settings acceptance.
- Performance/power fallback evidence and Human Visual Excellence review.
- Required Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, Manager, and Index integration acceptance.
- Exact-head CI, production signing/distribution, release approval, and Release Candidate qualification.

Passing source, unit, build, schema, emulator, or registry checks remains Development evidence only.

## Rollback

If this migration causes a regression, revert the exact Launcher V1.2 migration candidate to the prior validated V1.1 Development revision. That is a source rollback boundary, not authority to relabel V1.1 as the current design-system target. Do not weaken the canonical V1.2 contract to preserve a local implementation.
