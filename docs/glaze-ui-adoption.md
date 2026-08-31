# Glaze UI Adoption — GoreeCloud Launcher

## Target baseline

- Canonical design system: `GoreeCloud/goreecloud-glaze-ui`
- Target: Glaze UI 2.1 Stable
- Canonical token version reviewed: 2.1.0
- Reviewed canonical revision: `c49113eb8b93c267613fdf1bbca1f814495acad7`
- Adoption state: Adoption Candidate
- Production eligible on Glaze UI gate: no
- Adoption mode: native Android semantic mapping
- Automated contract: `scripts/check_glaze_ui.py`

The reviewed revision is the canonical Stable release anchor for Glaze UI 2.1.0. This adoption record establishes current-Stable migration work in Launcher; it does not certify the application for production or imply complete Glaze UI 2.1 conformance.

## Implemented 2.1 mapping

GoreeCloud Launcher maps the Glaze UI semantics it consumes into Jetpack Compose rather than importing a web or third-party UI runtime.

The current 2.1 Adoption Candidate mapping includes:

- the canonical spacing token keys `space.1`, `space.2`, `space.3`, `space.4`, `space.5`, `space.6`, `space.8`, `space.10`, `space.12`, and `space.16`, mapped respectively to 4/8/12/16/20/24/32/40/48/64 dp;
- `radius.sm`, `radius.md`, `radius.control`, `radius.lg`, `radius.xl`, `radius.2xl`, and `radius.pill`, mapped to 10/14/16/22/28/32/999 dp;
- the 48 dp current-contract general interaction floor from `currentContract.touchMinimum`;
- a mapped 56 dp touch-assistance target from `currentContract.touchAssistanceMinimum` for later accessibility-resolution integration;
- Light and Dark foundation/identity colors retained by the promoted 2.1 token map;
- Canvas/Surface-oriented content presentation through native Material 3 surfaces;
- native Android buttons, fields, dialogs, and explicit move controls for deterministic accessibility paths;
- direct persisted System/Light/Dark theme selection support in the native theme repository; and
- a native Theme Manager surface/catalog foundation with bounded previews for the currently implemented appearance modes.

The canonical token map also retains the historical compatibility token `target.minimum=44`. Launcher does not use that compatibility value to lower the current 2.1 mobile interaction contract: the active mapped floor remains 48 dp because the 2.1 `currentContract.touchMinimum` is authoritative for migrated current-contract interaction.

The native metric mapping lives in `app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt`. The Light/Dark color mapping lives in `app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeTheme.kt`. The native Theme Manager foundation lives in `GlazeThemeManagerCatalog.kt` and `ThemeManagerSurface.kt`.

The Theme Manager surface is source foundation in this slice and is not yet wired into Launcher Settings navigation. Existing Settings continues to expose its current appearance control until a separately reviewed wiring slice is accepted.

## 2.1 material and accessibility boundary

Glaze UI 2.1 keeps the material hierarchy Canvas → Surface → Soft Glaze → Glaze → Deep Glaze → Live Glaze and adds stronger Material Clarity, density, performance-profile, material-budget, and deterministic Accessibility Resolution Matrix requirements.

Launcher has migrated its bounded spacing, shape, target, and Light/Dark foundation mapping, but this Adoption Candidate does not yet claim complete material-role coverage, clarity-mode behavior, material budgeting, density adaptation, performance fallback, or accessibility-resolution integration across every Home, Dock, Apps, settings, dialog, gesture, and search surface.

Reduced transparency must resolve to Solid where required by the 2.1 contract. Touch-assistance behavior must apply the 56 dp resolution target where applicable. Large text/reflow, forced colors/system semantic authority, increased contrast, reduced motion, and assistive-technology behavior remain application-specific acceptance work rather than being inferred from token presence.

## Theme Manager foundation boundary

The native Theme Manager foundation currently represents only the persisted System, Light, and Dark modes that Launcher already implements. It adds stable user-facing metadata, bounded native previews, and direct `setMode` persistence so future Settings wiring does not depend on cycling through unrelated modes.

This does not implement or claim Deep Dark, wallpaper-derived/user-selected palettes, expression controls, third-party icon-pack discovery, icon-pack application, icon masking, per-app icon replacement, optical normalization beyond the existing bounded icon-size preference, or complete Glaze Theme Engine behavior.

## Ordering interaction decision

Launcher retains explicit move-earlier/move-later controls as the deterministic non-drag accessibility path for ordering. Direct workspace manipulation must call the same application-owned ordering/mutation authority rather than becoming authoritative for workspace truth. Cancellation must leave workspace state unchanged.

A separate test-only Glaze Motion evaluation may map Experimental motion semantics onto existing domain operations. Glaze Motion remains Experimental and is not a production dependency, does not replace the Stable Glaze UI 2.1 contract, and cannot satisfy the Launcher production gate.

## Appearance boundary

Light and Dark use the promoted Glaze UI 2.1 Stable token values consumed by Launcher. System follows Android system Light/Dark state. Deep Dark is not approximated with an invented palette and remains an explicit application acceptance gap until a reviewed native mapping is implemented.

Launcher personalization does not yet claim complete Appearance, Accent Seed, Material Clarity, Expression, or density preference support.

## Form-factor and accessibility boundary

This mapping does not constitute phone/tablet/foldable Glaze UI 2.1 acceptance. Representative rendered/native accessibility and real-device review remain required. Tablet and large-screen composition must not become a stretched phone surface; workspace, Theme Manager, Index, and settings surfaces require appropriate window/posture-aware composition where applicable.

The current mapping also does not establish complete reduced-transparency, increased-contrast, large-text/reflow, forced-colors, TalkBack, switch-access, touch-assistance, haptic, or reduced-motion acceptance. These requirements remain mandatory before Launcher can be considered aligned-current-stable and production eligible on the Glaze UI gate.

## Historical migration evidence

The prior Glaze UI 2.0 Adoption Candidate and earlier 1.x migrations remain historical implementation/migration evidence only. They do not satisfy the current 2.1 Stable requirement and must not be used as a current production-conformance claim.

## Not yet claimed

- Full Glaze UI 2.1 conformance.
- Aligned-current-stable production acceptance.
- Complete Soft Glaze/Glaze/Deep Glaze/Live Glaze role mapping.
- Complete Material Clarity or material-budget behavior.
- Complete density/performance profile adaptation.
- Deep Dark appearance acceptance.
- Accent Seed and expression-mode personalization.
- Theme Manager navigation integration into current Launcher Settings.
- Third-party icon-pack discovery/application or icon masking.
- Complete 2.1 motion-token mapping or reduced-motion acceptance.
- Complete Accessibility Resolution Matrix integration.
- Complete increased-contrast or reduced-transparency acceptance.
- Large-text/reflow or forced-colors acceptance.
- Touch-assistance runtime acceptance despite the mapped 56 dp target token.
- Complete phone/tablet/foldable adaptive-layout acceptance.
- TalkBack or switch-access acceptance.
- Phone/tablet rendered/native visual acceptance.
- Representative physical-device launcher acceptance.
- Glaze Motion Candidate, Stable, or production adoption.

These remain application-specific gates even though Glaze UI 2.1 itself is Stable.
