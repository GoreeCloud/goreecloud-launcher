# Glaze UI Adoption — GoreeCloud Launcher

## Target baseline

- Canonical design system: `GoreeCloud/glaze-ui`
- Target: Glaze UI 1.4 Stable
- Canonical token version reviewed: 1.4.0
- Adoption mode: native Android semantic mapping

## Current mapped semantics

GoreeCloud Launcher currently maps the Glaze UI semantics it directly consumes into Jetpack Compose rather than importing a third-party UI runtime.

Mapped in the current Milestone 1 slice:

- spacing steps used by HOME, Dock, app drawer, and management surfaces;
- semantic content/control radii;
- 44 dp minimum and 48 dp comfortable interaction targets;
- native Android buttons and dialogs for accessible placement management;
- Solid/Raised ordinary-content hierarchy rather than decorative glass everywhere;
- local-only presentation with no remote font, icon, analytics, or UI dependency.

The native mapping lives in `ui/theme/GlazeMetrics.kt` and intentionally contains only the subset currently consumed by Launcher.

## Form-factor boundary

This mapping does not yet constitute phone/tablet Glaze UI acceptance. Mobile and tablet compositions still require representative rendered and real-device review. Tablet must not become a stretched phone composition; future workspace/page and settings work must use window/posture-aware layouts where useful.

## Ordering interaction decision

Milestone 1 introduces native move-earlier/move-later controls before direct drag/drop. The explicit controls provide deterministic ordering, a non-drag accessibility path, and a stable domain operation that later drag/drop can call. Drag/drop must not replace the accessible ordering path.

## Not yet claimed

- Full Glaze UI 1.4 conformance.
- Functional Glass rendering acceptance.
- Reduced-transparency acceptance.
- Complete motion-token mapping.
- Wallpaper-derived palette acceptance.
- Phone/tablet visual acceptance.
- Physical-device launcher acceptance.

These remain application-specific gates even though Glaze UI 1.4 itself is Stable.
