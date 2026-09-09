# GLAZE UI V1.3 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.3 (`1.3.0`) — Adaptive Resonance**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact Stable integration source authority: `fc7cc91d2eace8da2371371c2855c24cbcb326a1`  
Stable aggregate entrypoints: `css/glaze-v1.3.0.css`, `js/glaze-v1.3.0.mjs`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic/structural mapping using Adaptive Resonance with the inherited V1.2 Frosted Neutral material foundation  
Automated contract: `scripts/check_glaze_ui.py`

GLAZE UI V1.3 / `1.3.0` is the current GoreeCloud design-system target. This record defines Launcher's repository-local V1.3 Development mapping. It does **not** establish complete V1.3 consumer conformance, rendered/accessibility/device acceptance, production approval, release approval, Release Candidate entry, or Stable qualification. V1.2 (`1.2.0`) is the rollback baseline; earlier V1.1/V1.0 and pre-reset evidence remains historical only.

## Authority boundary

The implementation-facing source anchor is the exact V1.3 Stable integration revision above. Canonical lifecycle authority remains `VERSION`, `registry/lifecycle.json`, `GLAZE_UI_V1_3.md`, `MIGRATION_V1_2_TO_V1_3.md`, and `acceptance/v1.3-stable.md` in `GoreeCloud/goreecloud-glaze-ui`.

Launcher maps applicable V1.3 contracts into first-party Jetpack Compose code. It does not embed a remote UI runtime or create a competing design-system authority. The V1.3 migration inherits the validated V1.2 Frosted Neutral material and structural geometry where the current Glaze contract carries them forward, while recording the Adaptive Resonance contract and Stable aggregate entrypoints explicitly.

## Implemented V1.3 source mapping

The current Development branch:

- pins machine version `1.3.0`, release identity `Adaptive Resonance`, and exact Stable integration revision `fc7cc91d2eace8da2371371c2855c24cbcb326a1`;
- records `contracts/v1.3/adaptive-resonance.plan.json` and the Stable V1.3 aggregate entrypoints;
- preserves inherited spacing values 4/8/12/16/20/24/32/48/64 dp where directly consumed;
- preserves the structural radius tiers currently consumed by Launcher;
- preserves inherited 8/16/24/32 dp optical geometry references plus capsule geometry;
- preserves the 48 dp normal touch-oriented interaction floor and 56 dp Touch Assistance / far-view target;
- retains System, Light, Dark, and Deep Dark through the existing device-local Theme Manager persistence path;
- retains the neutral Frosted Neutral material substrate inherited from V1.2;
- uses neutral separator lines for degraded/effects-free presentation;
- limits the current Theme Manager preview to one bounded Ice Blue decorative accent; and
- explicitly forbids wallpaper, application content, privacy/security state, or other producer truth from becoming an implicit adaptive-color authority.

The inherited material rule remains: **Neutral glass is the material; color is an accent.** Decorative color can never establish protection, privacy, identity, recovery, availability, focus, selection, or other authoritative state.

`space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.

## System Shell classification

Launcher Home is a **Workspace** presentation surface. Launcher Settings and Theme Manager are **Application** surfaces. These classifications affect presentation and interaction only; they grant no operating-system, Control Center, notification, authentication, search-indexing, or other system authority.

GoreeCloud Index remains the universal-search/indexing authority. Theme Manager is not Universal Search, Control Center, a Critical System surface, or an authorization boundary merely because Glaze defines presentation concepts for those surfaces.

## Adaptive Resonance and inherited Frosted Neutral boundary

Durable reading and explicit decisions remain certainty-first. Transient navigation, control, search, and feedback chrome may use neutral glazed treatment where appropriate. V1.3 adaptive expression remains bounded to presentation inputs that are explicitly implemented and accepted; this Launcher candidate does not sample wallpaper/application/editor content, persist Environmental Color Memory, or infer product truth from decorative color.

Reduced Transparency, Increased Contrast, forced-color/native equivalents, accessibility state, and producer-owned semantic state take precedence over material effects. If a frosted effect degrades or is removed, meaning, actions, focus, state, and hierarchy must remain intact.

## Theme Manager boundary

The reachable Theme Manager supports System, Light, Dark, and Deep Dark. Settings navigation remains saveable, unknown/stale destination values fail closed to Settings root, and only a different appearance choice may invoke caller-owned persistence.

The preview uses inherited neutral Frosted material plus one Ice Blue accent marker. It does not sample wallpaper/content, persist Environmental Color Memory, animate atmospheric fields, infer semantic state from color, or create remote color dependencies.

## Accessibility and resilience boundary

The V1.3 source migration does not by itself establish:

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

## Historical and rollback boundary

V1.2 (`1.2.0`) is the immediate rollback baseline for this migration. V1.2, V1.1, V1.0, and pre-reset Glaze UI 2.x adoption commits, pull requests, CI runs, and discussion remain immutable development history. Historical evidence may explain implementation ancestry but does not satisfy current V1.3 application acceptance.

Rollback must restore a recorded known-good Launcher integration; it must not rewrite Glaze release history or relabel a historical release as the current target.

## Acceptance still required

- Complete V1.3 component/state/material/adaptive mapping applicable to Launcher.
- Rendered review of Light, Dark, and Deep Dark across Home, Apps, Settings, Theme Manager, dialogs, and workspace editing.
- Reduced Motion, Reduced Transparency, Increased Contrast, forced-color/native-equivalent behavior, and degradation-order validation.
- 200% text/reflow, RTL/localization, and Touch Assistance resolution.
- TalkBack, Switch Access, keyboard/D-pad/focus-order, and spoken-announcement acceptance.
- Representative phone/tablet/foldable adaptive composition and reachability.
- Representative physical-device Theme Manager navigation/persistence and Home/Apps/Settings acceptance.
- Performance/power fallback evidence and Human Visual Excellence review.
- Verified V1.2 rollback on the Launcher integration path.
- Required Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, Manager, and Index integration acceptance.
- Exact-head CI, production signing/distribution, release approval, and Release Candidate qualification.

Passing source, unit, build, schema, emulator, or registry checks remains Development evidence only.
