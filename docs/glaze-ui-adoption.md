# Glaze UI Adoption — GoreeCloud Launcher

## Target baseline

- Canonical design system: `GoreeCloud/goreecloud-glaze-ui`
- Target: Glaze UI 1.5 Stable
- Canonical token version reviewed: 1.5.0
- Reviewed canonical revision: `2e1618397f6ebcdd254a76bfdd7e98846f2c5aa3`
- Adoption state: Adoption Candidate
- Production eligible on Glaze UI gate: no
- Adoption mode: native Android semantic mapping
- Automated contract: `scripts/check_glaze_ui.py`

The reviewed revision is the canonical squash-merge commit for PR #81, which promoted Glaze UI 1.5.0 to Stable. Moving the adoption target to 1.5 does not certify Launcher for production or imply complete application conformance.

## Current mapped semantics

GoreeCloud Launcher maps the Glaze UI semantics it directly consumes into Jetpack Compose rather than importing a web or third-party UI runtime.

The current mapped subset is:

- spacing steps used by HOME, Dock, app drawer, and management surfaces;
- semantic content/control radii;
- 44 dp minimum and 48 dp comfortable interaction targets;
- native Android buttons and dialogs for accessible placement management;
- Solid/Raised ordinary-content hierarchy rather than decorative glass everywhere;
- local-only presentation with no remote font, icon, analytics, or UI dependency.

The native mapping lives in `ui/theme/GlazeMetrics.kt`. The spacing, radius, and 44/48 dp target values consumed by Launcher are unchanged in the exact Glaze UI 1.5 Stable token map, so the repository can migrate this bounded subset without inventing new values.

## Automated contract boundary

`scripts/check_glaze_ui.py` is a fail-closed repository contract for the Glaze subset Launcher currently consumes. It verifies the canonical 1.5 spacing/radius/target values in the native Compose metric map and verifies that this evidence file records the exact reviewed Stable revision and unresolved application-specific acceptance boundaries.

The contract runs in Android CI before Gradle lint, unit tests, and build. It is intentionally not a rendered/native acceptance test and does not convert Adoption Candidate into aligned-current-stable production conformance.

## Glaze UI 1.5 expansion boundary

Glaze UI 1.5 adds or strengthens adaptive color, iconography and identity, purpose-driven motion, material/depth, semantic layout/density, and mixed-input state behavior. Launcher does not claim those systems complete merely because its existing spacing/radius/target subset is compatible with 1.5.

Application-specific mapping and acceptance are still required wherever Launcher consumes those semantics. In particular, existing Compose animation and drag feedback must not be treated as a completed Glaze UI 1.5 motion mapping until reduced-motion behavior, interruption/cancellation, rendered behavior, and representative-device acceptance are validated.

## Ordering interaction decision

Launcher retains explicit move-earlier/move-later controls as the deterministic non-drag accessibility path for ordering. Direct drag/drop calls the same application-owned ordering domain rather than becoming authoritative for workspace truth. Drag cancellation must leave workspace state unchanged.

A separate test-only Glaze Motion evaluation may map Experimental motion semantics onto these existing domain operations. Experimental Glaze Motion is not a production dependency, does not replace the Stable Glaze UI 1.5 motion contract, and cannot satisfy the Launcher production gate.

## Form-factor boundary

This mapping does not yet constitute phone/tablet Glaze UI acceptance. Mobile and tablet compositions still require representative rendered, accessibility, and real-device review. Tablet must not become a stretched phone composition; workspace/page and settings work should use window/posture-aware layouts where useful.

## Not yet claimed

- Full Glaze UI 1.5 conformance.
- Aligned-current-stable production acceptance.
- Complete adaptive-color or wallpaper-derived palette acceptance.
- Complete iconography/identity acceptance.
- Functional Glass or reduced-transparency acceptance.
- Complete Stable motion-token mapping or reduced-motion acceptance.
- Glaze Motion Candidate, Stable, or production adoption.
- Phone/tablet rendered visual acceptance.
- Physical-device launcher acceptance.

These remain application-specific gates even though Glaze UI 1.5 itself is Stable.
