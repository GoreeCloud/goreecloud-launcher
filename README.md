# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application. It is not a fork or visual clone of another launcher.

## Status

**Development — not a signed production/Stable release.**

The current repository contains a usable native daily-launcher foundation plus guarded Room-authority workspace foundations and an expanding rendered multi-page navigation/editing path. Passing CI/emulator tests does not establish production Room cutover, complete physical-device interaction acceptance, signed release, or Stable qualification.

## Product rules

- No ads, sponsorships, promoted apps, or affiliate placement.
- No behavioral advertising or mandatory analytics.
- Core Home operation is offline-capable.
- No required GoreeCloud server or account for core launcher use.
- Minimal, documented permissions; current source has no Android `INTERNET` permission.
- Glaze UI is the Design Center authority for applicable interface behavior.
- Privacy Shield governs privacy/user-control surfaces.
- Wardveil Security governs applicable security/trust surfaces.

## Current user-facing foundation

Current source includes:

- Android HOME activity and user-controlled `ROLE_HOME` onboarding;
- lifecycle-aware default-HOME status;
- `LauncherApps` discovery across available profiles and package lifecycle callbacks;
- ordered locally persisted Favorites and a five-item Dock;
- first-run Favorites/Dock seeding from installed launchable apps;
- long-press placement management from supported surfaces;
- explicit move-earlier/move-later controls;
- Reorder-mode Favorites/Dock drag/drop using the current workspace persistence path;
- terminal-Room multi-page HOME observation with page selection, page reordering, empty-page creation/deletion, and rendering of secondary application pages;
- bounded secondary-page application moves to another existing Home page through the guarded Room placement path;
- All apps with local label/package search and app launching; and
- persisted System / Light / Dark appearance selection.

## Glaze UI 2.0 boundary

Launcher targets the current **Glaze UI 2.0.0 Stable** design-system baseline through its repository Adoption Candidate mapping. Historical Glaze UI 1.x references are not the current design authority.

Source governance and mapped native tokens do not by themselves establish complete rendered/native/accessibility acceptance. Representative phone/tablet/foldable behavior, physical touch interaction, TalkBack/switch-access behavior, contrast, reduced-transparency/reduced-motion behavior where applicable, and other production design gates remain separate evidence requirements.

## Workspace persistence and multi-page foundation

Launcher has progressed beyond the earlier DataStore-only relational rehearsal. The repository now contains:

- Preferences DataStore compatibility/workspace state used by the primary user-facing Home path;
- AndroidX Room relational workspace pages/items and schema history;
- mirror, verification, dual-read, startup reconciliation, and durable authority-state contracts;
- guarded terminal `ROOM` authority primitives;
- deterministic framework-independent grid and multi-page placement validation/mutation contracts;
- Room-backed multi-page HOME page-order persistence for terminal Room authority;
- Room-backed cross-page HOME item placement persistence that validates the complete workspace model and rechecks the observed page/item snapshot inside the transaction before writing; and
- a terminal-Room paged HOME observer that maps authoritative Room pages/items into rendered application-page state and drives page selection in the Development Home UI.

### Current rendered multi-page boundary

When terminal Room authority is active, Launcher can expose page selection, create empty pages, delete only revalidated empty non-primary pages, reorder pages, render application items from secondary Room pages, and request that an application on a secondary page move to another existing Home page.

For the bounded move path, the UI identifies the source page and application key. Room-authoritative state is read to identify exactly one source item and derive a deterministic free target cell from the current coordinate envelope. The actual write is then delegated to the existing guarded `moveHomeItem` transaction, which re-reads and validates the complete HOME page/item snapshot. Concurrent changes, malformed/null placement state, collisions, missing pages/items, or ambiguous source identity fail closed instead of overwriting newer state.

The primary Home page keeps the existing Favorites/Dock management path. Direct cell/span editing, arbitrary drag across pages, and moving primary-page Favorites through the secondary-page menu remain separate interaction milestones.

Unsupported Room item types are counted and surfaced rather than silently treated as applications. Folders, shortcuts, widgets, populated-page deletion, and destructive recovery/undo remain separate milestones.

This is a Development rendering/editing bridge, not a claim of production Room cutover or a complete multi-page editor.

## Current Room item/page safety behavior

Post-cutover Room mutation APIs refuse access unless durable terminal Room authority is already established. Multi-page item moves fail closed on missing pages/items, identity substitution, invalid/null placement coordinates, collisions, out-of-bounds state, or a workspace snapshot that changes between validation and the transactional write.

Page creation validates the complete current page set before appending an empty page. Page deletion is limited to empty non-primary pages and repeats page/item snapshot and emptiness checks inside the transaction before the foreign-key cascade can run.

The rendered page observer is likewise Room-authority gated. If authoritative paged Room state is unavailable, the application does not fabricate secondary pages and remains on the accepted primary Home path.

Source/emulator acceptance of these paths does not equal production cutover or physical-device Home acceptance.

## Build baseline

Current Android source uses Kotlin, Jetpack Compose, AndroidX Room/SQLite, DataStore, and Android SDK 36-era build tooling. See repository Gradle files and CI for the exact pinned versions used by the current source tree rather than relying on copied version text in external documentation.

Run the repository validation commands documented by CI, including privacy/manifest/Glaze guards, lint, JVM tests, debug assembly, Room schema checks, and the complete Android instrumentation suite on the configured emulator target.

## Current limitations

Still incomplete or separately gated:

- production Room-authority cutover/routing and post-cutover recovery acceptance;
- direct cell/span placement editing and cross-page drag/drop UI;
- primary-page-to-secondary-page item movement through the rendered editor;
- populated-page deletion plus confirmation/recovery/undo semantics;
- folders, shortcuts, widgets/AppWidgetHost, and richer placement UI;
- complete icon/label customization and broader gesture bindings;
- complete first-party Glaze Theme Engine behavior;
- versioned backup/restore and Everkeep acceptance;
- Android OS process-death acceptance;
- representative physical-device default-HOME and drag interaction acceptance;
- signed release packaging/distribution; and
- production/Stable qualification.

## Documentation

- [USER-MANUAL.md](USER-MANUAL.md) — current Development user guidance.
- [Rendered HOME page navigation](docs/rendered-home-page-navigation.md) — current terminal-Room page rendering and bounded page-editing boundary.
- `docs/` — architecture, persistence, Glaze UI adoption, validation, and implementation records.
- Canonical application/service project specifications are maintained under `GoreeCloud/Projects` in the authorized GoreeCloud project documentation scope.

## License

GPL-3.0. See `LICENSE`.
