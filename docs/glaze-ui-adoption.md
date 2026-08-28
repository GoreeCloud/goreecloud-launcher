# Glaze UI Adoption — GoreeCloud Launcher

## Target baseline

- Canonical design system: `GoreeCloud/goreecloud-glaze-ui`
- Target: Glaze UI 2.0 Stable
- Canonical token version reviewed: 2.0.0
- Reviewed canonical revision: `ff3fff4306bd53ea9c0715a7c0d64265bb038617`
- Adoption state: Adoption Candidate
- Production eligible on Glaze UI gate: no
- Adoption mode: native Android semantic mapping
- Automated contract: `scripts/check_glaze_ui.py`

The reviewed revision is the canonical merge commit that promoted Glaze UI 2.0.0 to Stable. This adoption record establishes real current-Stable migration work in Launcher; it does not certify the application for production or imply complete Glaze UI 2.0 conformance.

## Implemented 2.0 mapping

GoreeCloud Launcher maps the Glaze UI semantics it consumes into Jetpack Compose rather than importing a web or third-party UI runtime.

The current 2.0 Adoption Candidate mapping includes:

- the promoted 4/8/12/16/24/32/48 spacing subset used by Home, Dock, app drawer, and placement surfaces;
- 8/12/16/24/32 utility geometry plus the 999 dp capsule control radius;
- the 48 dp general interaction floor for mapped controls;
- Light and Dark foundation/identity colors from the promoted Stable token map;
- Canvas/Surface-oriented content presentation through native Material 3 surfaces;
- native Android buttons, fields, dialogs, and explicit move controls for deterministic accessibility paths;
- local-only presentation with no remote font, icon, analytics, or UI runtime dependency.

The native metric mapping lives in `app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt`. The Light/Dark color mapping lives in `app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeTheme.kt`.

Existing metric property names are retained temporarily as compatibility aliases for Launcher call sites, but their values now follow the promoted 2.0 geometry/spacing/target contract. New Launcher UI work should use the 2.0 semantic intent rather than treat the old property names as an independent design vocabulary.

## 2.0 interaction boundary

Glaze UI 2.0 requires content to default to Canvas/Surface while interaction uses Soft Glaze/Glaze/Deep Glaze/Live Glaze according to role. Launcher has migrated its baseline geometry, target floor, and Light/Dark palette, but this Adoption Candidate does not yet claim complete material-role mapping across every Home, Dock, drawer, dialog, drag, and search surface.

Connected Transformation, Navigation Capsule semantics where applicable, Live Glaze behavior, Calm/Balanced/Expressive expression modes, haptic semantics, and the complete 2.0 motion family remain application-specific implementation and acceptance work. Existing Compose animation and drag feedback must not be treated as complete 2.0 motion conformance until reduced-motion behavior, interruption/cancellation, rendered behavior, and representative-device acceptance are validated.

## Ordering interaction decision

Launcher retains explicit move-earlier/move-later controls as the deterministic non-drag accessibility path for ordering. Direct drag/drop calls the same application-owned ordering domain rather than becoming authoritative for workspace truth. Drag cancellation must leave workspace state unchanged.

A separate test-only Glaze Motion evaluation may map Experimental motion semantics onto these existing domain operations. Glaze Motion remains Experimental and is not a production dependency, does not replace the Stable Glaze UI 2.0 motion contract, and cannot satisfy the Launcher production gate.

## Appearance boundary

Light and Dark now use the promoted Glaze UI 2.0 Stable token values consumed by Launcher. Deep Dark is not approximated with an invented palette and remains an explicit application acceptance gap until a reviewed native mapping is implemented.

System appearance continues to follow the Android system Light/Dark state. Launcher personalization does not yet claim complete 2.0 Appearance, Accent Seed, or Expression preference support.

## Form-factor and accessibility boundary

This mapping does not yet constitute phone/tablet Glaze UI 2.0 acceptance. Mobile and tablet compositions still require representative rendered/native accessibility and real-device review. Tablet must not become a stretched phone composition; workspace/page and settings work should use window/posture-aware layouts where useful.

The current mapping also does not establish complete reduced-transparency, increased-contrast, large-text/reflow, TalkBack, switch-access, touch-assistance, haptic, or reduced-motion acceptance. These requirements remain mandatory before Launcher can be considered aligned-current-stable and production eligible on the Glaze UI gate.

## Historical migration evidence

The prior Glaze UI 1.6 Adoption Candidate remains historical migration evidence only. It does not satisfy the current 2.0 Stable requirement and must not be used as a production conformance claim.

## Not yet claimed

- Full Glaze UI 2.0 conformance.
- Aligned-current-stable production acceptance.
- Complete Soft Glaze/Glaze/Deep Glaze/Live Glaze role mapping.
- Deep Dark appearance acceptance.
- Accent Seed and expression-mode personalization.
- Complete Connected Transformation or Navigation Capsule mapping.
- Complete 2.0 motion-token mapping or reduced-motion acceptance.
- Complete increased-contrast or reduced-transparency acceptance.
- Complete phone/tablet adaptive-layout acceptance.
- TalkBack or switch-access acceptance.
- Phone/tablet rendered/native visual acceptance.
- Representative physical-device launcher acceptance.
- Glaze Motion Candidate, Stable, or production adoption.

These remain application-specific gates even though Glaze UI 2.0 itself is Stable.
