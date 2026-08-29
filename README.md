# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application. It is not a fork or visual clone of another launcher.

## Status

**Development — not a signed production/Stable release.**

The current repository contains a usable native daily-launcher foundation plus staged post-cutover workspace persistence contracts. Passing CI/emulator tests does not establish production Room cutover, complete physical-device interaction acceptance, signed release, or Stable qualification.

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
- All apps with local label/package search and app launching; and
- persisted System / Light / Dark appearance selection.

## Glaze UI 2.0 boundary

Launcher now targets the current **Glaze UI 2.0.0 Stable** design-system baseline through its repository Adoption Candidate mapping. Historical Glaze UI 1.x references are not the current design authority.

Source governance and mapped native tokens do not by themselves establish complete rendered/native/accessibility acceptance. Representative phone/tablet/foldable behavior, physical touch interaction, TalkBack/switch-access behavior, contrast, reduced-transparency/reduced-motion behavior where applicable, and other production design gates remain separate evidence requirements.

## Workspace persistence and multi-page foundation

Launcher has progressed beyond the earlier DataStore-only relational rehearsal. The repository now contains:

- Preferences DataStore compatibility/workspace state used by the current user-facing Home path;
- AndroidX Room relational workspace pages/items and schema history;
- mirror, verification, dual-read, startup reconciliation, and durable authority-state contracts;
- guarded terminal `ROOM` authority primitives;
- deterministic framework-independent grid and multi-page placement validation/mutation contracts;
- Room-backed multi-page HOME page-order persistence for terminal Room authority; and
- Room-backed cross-page HOME item placement persistence that validates the complete workspace model and rechecks the observed page/item snapshot inside the transaction before writing, preventing a validated move from overwriting concurrent workspace drift.

These merged source capabilities are **persistence/domain foundations**, not a claim that the current Home UI exposes a complete production multi-page editor. Production Room cutover/routing, rendered page creation/navigation, live cell/span editing, broader drag/drop UX, and recovery acceptance remain separate milestones.

## Current Room item/page safety behavior

Post-cutover Room mutation APIs refuse access unless durable terminal Room authority is already established. Multi-page item moves fail closed on missing pages/items, identity substitution, invalid/null placement coordinates, collisions, out-of-bounds state, or a workspace snapshot that changes between validation and the transactional write.

Source/emulator acceptance of these paths does not equal production cutover or physical-device Home acceptance.

## Build baseline

Current Android source uses Kotlin, Jetpack Compose, AndroidX Room/SQLite, DataStore, and Android SDK 36-era build tooling. See repository Gradle files and CI for the exact pinned versions used by the current source tree rather than relying on copied version text in external documentation.

Run the repository validation commands documented by CI, including privacy/manifest/Glaze guards, lint, JVM tests, debug assembly, Room schema checks, and the complete Android instrumentation suite on the configured emulator target.

## Current limitations

Still incomplete or separately gated:

- production Room-authority cutover/routing and post-cutover recovery;
- rendered multi-page workspace creation/navigation/editing;
- folders, shortcuts, widgets/AppWidgetHost, and richer placement UI;
- complete icon/label customization and broader gesture bindings;
- complete first-party Glaze Theme Engine behavior;
- versioned backup/restore and Everkeep acceptance;
- Android OS process-death acceptance;
- representative physical-device default-HOME and drag interaction acceptance;
- signed release packaging/distribution; and
- production/Stable qualification.

## Documentation

- [USER-MANUAL.md](USER-MANUAL.md)
- [SPECIFICATIONS.md](SPECIFICATIONS.md)
- [FEATURES.md](FEATURES.md)
- [BENEFITS.md](BENEFITS.md)
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md)
- `docs/` for architecture, persistence, Glaze UI adoption, validation, and related implementation records.

## License

GPL-3.0. See `LICENSE`.
