# GLAZE UI V1.4.1 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.4.1 (`1.4.1`)**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact Stable merged source authority: `4fab9da0fad2e5c974e0e66ec88632c61745751c`  
Immediate rollback baseline: **V1.4.0 (`1.4.0`)**  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic/structural mapping with bounded local Optical Intelligence  
Automated Stable contract: `scripts/check_glaze_ui.py`  
V1.5 Development compatibility contract: `contracts/glaze-ui/launcher-glaze-v1.5-development.json`

GLAZE UI V1.4.1 is the current GoreeCloud design-system adoption target for Launcher. This record defines repository-local Development implementation only. Shared Glaze V1.4.1 qualification does **not** establish complete Launcher consumer conformance, rendered/accessibility/device acceptance, product production approval, release approval, or Stable Launcher qualification.

## Authority boundary

Launcher consumes the current Stable V1.4.1 contract as a native Jetpack Compose mapping. It does not embed a remote UI runtime and does not create a competing Glaze authority. The native source pins V1.4.1 through `GlazeMetrics.targetVersion`, `GlazeOpticalV14.targetVersion`, and exact Stable revision `4fab9da0fad2e5c974e0e66ec88632c61745751c`.

GLAZE UI remains presentation/interaction authority only. Optical atmosphere must never manufacture authorization, privacy, security, backup, recovery, identity, synchronization, search, package, workspace, or trust state owned by another GoreeCloud system or Android.

## Implemented source mapping

The current Development implementation:

- pins machine version `1.4.1` and exact Stable authority `4fab9da0fad2e5c974e0e66ec88632c61745751c`;
- preserves V1.4.0 as the immediate rollback baseline;
- preserves inherited spacing, structural radii, optical geometry, the 48 dp ordinary target, and 56 dp Touch Assistance / far-view target;
- preserves System, Light, Dark, and Deep Dark through the existing device-local Theme Manager path;
- retains Deep Teal + Soft Amber atmosphere as non-semantic;
- retains the deterministic native Content-Aware Frost / Semantic Blur Protection resolver;
- keeps Environmental Color Memory influence capped at 8%;
- keeps Forced Colors and Reduced Transparency fail-closed to solid-accessible presentation;
- keeps Increased Contrast decoration-free; and
- requires no telemetry, camera access, wallpaper-pixel collection, analytics, or remote context.

`space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.

## Accessibility and application-acceptance boundary

The shared V1.4.1 release has its own governed qualification record, but Launcher still requires repository-local evidence for its concrete surfaces and runtime composition. Passing source, unit, build, schema, emulator, registry, or V1.5 Development compatibility checks remains Development evidence only.

Launcher-specific acceptance still requires rendered Home/Apps/Settings/Theme Manager/dialog/workspace review, Reduced Motion/Transparency/Increased Contrast/forced-color behavior, 200% text/reflow, RTL/localization, Touch Assistance resolution, TalkBack/Switch Access/keyboard/D-pad/focus-order acceptance, representative phone/tablet/foldable composition, Theme Manager persistence, performance/power fallbacks, product-specific Human Visual Excellence, rollback, protected signing/provenance, release approval, and production verification.

## Platform Contract 0.3 boundary

The active Launcher stack uses GoreeCloud Platform Contract `0.3` with the seven authoritative Integral Platform Systems: Manager, Privacy Shield, Wardveil Security, Everkeep, GLAZE UI, Mesh, and Identity. GoreeCloud Sync is a separately governed application/service capability and is **not** an eighth Integral Platform System.

Launcher remains Development/nonconformant. Local portable snapshots, transactional restore, and recovery journals remain recovery/portability mechanisms. They are not GoreeCloud Sync implementation, replication, conflict-resolution, or cross-device synchronization evidence. Sync and Everkeep remain distinct authorities.

GoreeCloud Index remains the universal-search/indexing authority.

## V1.5 Development compatibility

Launcher carries a bounded repository-local compatibility exercise for **GLAZE UI V1.5 / `1.5.0-dev.1`** at exact Glaze Development revision `e7c397837908e4644d6230f17d0f73e84e3d1558`.

V1.5 is explicitly Development and non-consumer-eligible. This compatibility slice therefore does **not** replace Launcher’s current Stable V1.4.1 target, does not alter `goreecloud.platform.yaml`, and does not add the V1.5 JavaScript resolver to the shipped Android runtime.

The repository-local record is `contracts/glaze-ui/launcher-glaze-v1.5-development.json`. `scripts/validate_glaze_v1_5_development.mjs` consumes an exact detached checkout of the governed upstream revision and exercises four Launcher-specific machine scenarios:

- the canonical handheld/offline Launcher profile, where local application launch remains available and GoreeCloud Index retains service authority for universal search;
- a temporarily unavailable Index search capability, which disables only the search action while local app launch remains available;
- duplicate Index capability ownership, which fails closed rather than allowing Glaze to invent provider precedence; and
- a Launcher-owned restricted workspace-edit authorization state, which remains visible/disabled and cannot be executed automatically by Glaze.

The dedicated workflow `Launcher Glaze V1.5 Development Compatibility` first reruns `scripts/check_glaze_ui.py` to prove the current Stable V1.4.1 Launcher source boundary still holds. It then fetches the exact V1.5 Development revision by commit SHA and runs the repository-local compatibility validator.

Passing this gate establishes only source-level Development compatibility with the stated resolver semantics. It does **not** establish rendered V1.5 adoption, native/device V1.5 integration, TalkBack/Switch Access or other assistive-technology acceptance, representative default-HOME acceptance, Privacy Shield or Wardveil acceptance, performance/power acceptance, consumer conformance, Release Candidate qualification, Stable qualification, signing, release publication, deployment, or production acceptance.

## Rollback

V1.4.0 remains the immediate known-good shared Glaze rollback baseline for this V1.4.1 patch migration. A Launcher rollback must be explicit and verified; it does not authorize relabeling an older Glaze release as current.
