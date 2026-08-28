# Glaze UI Adoption — GoreeCloud Launcher

## Target baseline

- Canonical design system: `GoreeCloud/goreecloud-glaze-ui`
- Target: Glaze UI 1.6 Stable
- Canonical token version reviewed: 1.6.0
- Reviewed canonical revision: `9dcd39dad0ade79fb01dfb1b6b39f6bf2c167471`
- Adoption state: Adoption Candidate
- Production eligible on Glaze UI gate: no
- Adoption mode: native Android semantic mapping
- Automated contract: `scripts/check_glaze_ui.py`

The reviewed revision is the canonical merge commit for PR #97, which promoted Glaze UI 1.6.0 to Stable. Moving the adoption target to 1.6 records current-Stable migration work; it does not certify Launcher for production or imply complete application conformance.

## Current mapped semantics

GoreeCloud Launcher maps the Glaze UI semantics it directly consumes into Jetpack Compose rather than importing a web or third-party UI runtime.

The current mapped subset is:

- spacing steps used by HOME, Dock, app drawer, and management surfaces;
- semantic content/control radii;
- 44 dp minimum and 48 dp comfortable interaction targets;
- native Android buttons and dialogs for accessible placement management;
- Solid/Raised ordinary-content hierarchy rather than decorative glass everywhere;
- local-only presentation with no remote font, icon, analytics, or UI dependency.

The native mapping lives in `ui/theme/GlazeMetrics.kt`. Glaze UI 1.6.0 retains the Stable spacing, radius, and 44/48 dp target foundation consumed by Launcher, so this bounded native mapping can move to the current Stable release without inventing new metric values.

## Automated contract boundary

`scripts/check_glaze_ui.py` is a fail-closed repository contract for the Glaze subset Launcher currently consumes. It verifies the retained Stable spacing/radius/target values in the native Compose metric map and verifies that this evidence file records the exact reviewed Glaze UI 1.6.0 Stable revision and unresolved application-specific acceptance boundaries.

The contract runs in Android CI before Gradle lint, unit tests, and build. It is intentionally not a rendered/native acceptance test and does not convert Adoption Candidate into aligned-current-stable production conformance.

## Glaze UI 1.6 expansion boundary

Glaze UI 1.6 retains the 1.5 Stable adaptive-color, iconography, motion, material/depth, layout/density, and mixed-input foundations while promoting Evidence Presentation and Authority Surfaces plus Adaptive Workspace and Navigation to Stable. Launcher does not claim those systems complete merely because its existing spacing/radius/target subset remains compatible with 1.6.

Application-specific mapping and acceptance are still required wherever Launcher consumes those semantics. Phone and tablet compositions must be evaluated against the 1.6 Adaptive Workspace requirements for semantic/focus order, current destination/action state, input-aware target floors, purpose-built transformation, reduced-transparency/performance fallbacks, and root-overflow resilience. Evidence Presentation is applicable only where Launcher presents producer-authoritative GoreeCloud platform evidence; this adoption record makes no such completion claim.

Existing Compose animation and drag feedback must not be treated as a completed Stable motion mapping until reduced-motion behavior, interruption/cancellation, rendered behavior, and representative-device acceptance are validated.

## Ordering interaction decision

Launcher retains explicit move-earlier/move-later controls as the deterministic non-drag accessibility path for ordering. Direct drag/drop calls the same application-owned ordering domain rather than becoming authoritative for workspace truth. Drag cancellation must leave workspace state unchanged.

A separate test-only Glaze Motion evaluation may map Experimental motion semantics onto these existing domain operations. Experimental Glaze Motion is not a production dependency, does not replace the Stable Glaze UI 1.6 motion contract, and cannot satisfy the Launcher production gate.

## Form-factor boundary

This mapping does not yet constitute phone/tablet Glaze UI acceptance. Mobile and tablet compositions still require representative rendered, accessibility, and real-device review. Tablet must not become a stretched phone composition; workspace/page and settings work should use window/posture-aware layouts where useful.

## Not yet claimed

- Full Glaze UI 1.6 conformance.
- Aligned-current-stable production acceptance.
- Complete adaptive-color or wallpaper-derived palette acceptance.
- Complete iconography/identity acceptance.
- Functional Glass or reduced-transparency acceptance.
- Complete Stable motion-token mapping or reduced-motion acceptance.
- Complete Glaze UI 1.6 Adaptive Workspace phone/tablet acceptance.
- Glaze Motion Candidate, Stable, or production adoption.
- Phone/tablet rendered visual acceptance.
- Physical-device launcher acceptance.

These remain application-specific gates even though Glaze UI 1.6 itself is Stable.
