# GLAZE UI V1.5.1 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.5 (`1.5.1`)**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact Stable merged source authority: `98da57064ede0f334627b632bc16801f580331af`  
Reviewed V1.5 implementation anchor: `ee1032a0822ab8e103f8afe48e5c1859fde65cc9`  
Inherited optical/material baseline: **V1.4.1 (`1.4.1`)** at `4fab9da0fad2e5c974e0e66ec88632c61745751c`  
Immediate shared Stable rollback baseline: **V1.5.0 (`1.5.0`)** at `b7fa8164bfdeaa1dc0acb21b770e7601120da04e`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android presentation mapping with inherited bounded local V1.4.1 Optical Intelligence  
Automated source contract: `scripts/check_glaze_ui.py`

GLAZE UI V1.5 / `1.5.1` is the current GoreeCloud design-system adoption target for Launcher. This record describes repository-local Development implementation only. Shared Glaze V1.5.1 Stable qualification does **not** establish complete Launcher consumer conformance, rendered/accessibility/device acceptance, product production approval, release approval, or Stable Launcher qualification.

## Authority boundary

Launcher consumes V1.5 as a native Jetpack Compose presentation contract. `GlazeCapabilityV15` maps the Stable Context + Capability Awareness semantics without embedding the Glaze JavaScript runtime or creating a competing authority. The V1.5 layer consumes already-owned capability truth and cannot grant consent, permission, authorization, provider precedence, navigation, fallback execution, or consequential execution.

GLAZE UI remains presentation/interaction authority only. Room remains Launcher workspace authority, Android LauncherApps remains application-inventory authority, GoreeCloud Index remains universal-search/indexing authority, Privacy Shield remains privacy/data-use authorization authority, Wardveil Security remains security authority, GoreeCloud Identity remains identity/authorization authority, GoreeCloud Mesh remains coordination authority, and Everkeep remains continuity authority.

Missing, unavailable, restricted, unknown, or conflicting required capability state fails closed. Duplicate provider ownership is represented as conflict rather than silently selecting a winner.

## V1.5.1 presentation mapping and inherited V1.4.1 optics

The current Development implementation:

- pins the active presentation contract to machine version `1.5.1`, Stable authority `98da57064ede0f334627b632bc16801f580331af`, and reviewed implementation anchor `ee1032a0822ab8e103f8afe48e5c1859fde65cc9` through `GlazeCapabilityV15`;
- records V1.5.0 / `b7fa8164bfdeaa1dc0acb21b770e7601120da04e` as the immediate shared Stable rollback release;
- preserves V1.4.1 / `4fab9da0fad2e5c974e0e66ec88632c61745751c` as the inherited optical/material implementation rather than relabeling `GlazeOpticalV14` as V1.5 source;
- preserves inherited spacing, structural radii, optical geometry, the 48 dp ordinary target, and 56 dp Touch Assistance / far-view target;
- preserves System, Light, Dark, and Deep Dark through the existing device-local Theme Manager path;
- retains Deep Teal + Soft Amber atmosphere as non-semantic;
- retains the deterministic native Content-Aware Frost / Semantic Blur Protection resolver;
- keeps Environmental Color Memory influence capped at 8%;
- keeps Forced Colors and Reduced Transparency fail-closed to solid-accessible presentation;
- keeps Increased Contrast decoration-free;
- requires no telemetry, camera access, wallpaper-pixel collection, analytics, or remote context; and
- keeps contextual capability presentation free of query text, credentials, private identifiers, raw authorization payloads, or other unnecessary user content.

`space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.

## Historical V1.5 Development compatibility evidence

The repository still retains `contracts/glaze-ui/launcher-glaze-v1.5-development.json` and `scripts/validate_glaze_v1_5_development.mjs` as historical Development compatibility evidence from before V1.5 Stable promotion. That record is useful provenance for the Launcher-specific resolver scenarios, but it is no longer the current lifecycle authority and must not override the current Stable V1.5.1 source mapping.

Those scenarios remain relevant regression expectations: local application launch stays independent of Index availability, unavailable Index search disables only the affected presentation, duplicate Index capability ownership fails closed, and restricted workspace-edit state cannot become automatic execution authority.

## Accessibility and application-acceptance boundary

The shared V1.5.1 release has its own governed qualification record with 18 accepted obligations, including the independently reviewed `performance-representative-budget` and `platform-posture-continuity` expansions. Those shared qualification results remain bound to their actual reviewed Glaze revisions and environments. They do **not** establish Launcher-local performance, power, phone/tablet/foldable posture, Android runtime, rendered, accessibility, or production acceptance.

Launcher-specific acceptance still requires rendered Home/Apps/Settings/Theme Manager/dialog/workspace review, capability-state explanations, Reduced Motion/Transparency/Increased Contrast/forced-color behavior, 200% text/reflow, RTL/localization, Touch Assistance resolution, TalkBack/Switch Access/keyboard/D-pad/focus-order acceptance, representative phone/tablet/foldable composition, Theme Manager persistence, performance/power fallbacks, product-specific Human Visual Excellence, rollback, protected signing/provenance, release approval, and production verification.

## Platform Contract 0.2 boundary

The active Launcher migration uses the current authoritative GoreeCloud Platform Contract `0.2` with exactly seven Integral Platform Systems: Manager, Privacy Shield, Wardveil Security, Everkeep, GLAZE UI, Mesh, and Identity. GoreeCloud Sync is a separately governed application/service capability and is **not** an eighth Integral Platform System. The accepted central Platform Contract authority requiring current Stable GLAZE UI `1.5.1` is exact revision `981c5807f249955e9f2c6c5d9136c9ed9c8017e9`.

Launcher remains Development/nonconformant. Local portable snapshots, transactional restore, and recovery journals remain recovery/portability mechanisms. They are not GoreeCloud Sync implementation, replication, conflict-resolution, or cross-device synchronization evidence. Sync and Everkeep remain distinct authorities.

## Rollback

V1.5.0 at `b7fa8164bfdeaa1dc0acb21b770e7601120da04e` is the immediate shared Glaze rollback baseline for this V1.5.1 migration. V1.4.1 remains the inherited Launcher optical/material implementation baseline. A Launcher rollback must be explicit and verified; it does not authorize relabeling an older Glaze release as current.
