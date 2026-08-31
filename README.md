# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application and the intended native home, application-navigation, search, personalization, and contextual-access experience for GoreeCloud devices. It is not a fork or visual clone of another launcher.

## Status

**Development — not a signed production/Stable release.**

The current repository contains a usable native daily-launcher foundation plus guarded Room-authority workspace foundations and an expanding rendered multi-page navigation/editing path. Passing CI/emulator tests does not establish complete physical-device interaction acceptance, signed release, full platform integration, or Stable qualification.

The complete approved target capability inventory is maintained in [FEATURES.md](FEATURES.md). Target capabilities are not implementation claims unless they are also identified as current Development behavior and supported by repository evidence.

## Product role

Launcher is intended to become the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together within one adaptive interface.

Its approved product scope includes rich Home and application-drawer organization, GoreeCloud Search integration, visual/icon personalization, gestures, contextual experiences, information feeds/cards, notifications, folders, widgets, adaptive dock/layout behavior, motion, application management, backup/sync/configuration, and applicable cross-platform GoreeCloud integrations.

Launcher intelligence and personalization should remain transparent and user-controlled.

## Product rules

- No ads, sponsorships, promoted apps, or affiliate placement.
- No behavioral advertising or mandatory analytics.
- Core Home operation is offline-capable.
- No required GoreeCloud server or account for core launcher use.
- Minimal, documented permissions; current source has no Android `INTERNET` permission.
- Glaze UI is the Design Center authority for applicable interface behavior.
- Privacy Shield governs applicable privacy, consent, data-minimization, and user-control surfaces.
- Wardveil Security governs applicable security, trust, protection, verification, and response surfaces.
- Everkeep governs applicable continuity, backup, recovery, preservation, and portability behavior.
- GoreeCloud Identity governs applicable identity/authentication/authorization behavior.
- GoreeCloud Mesh governs applicable cross-service/device coordination and capability integration.
- A platform-system integration is not considered implemented merely because it is named in product documentation or displayed in the interface.

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
- a lazy horizontal Home page selector that shows authoritative app/unsupported-item context and automatically scrolls the selected page into view after selection, reorder, or page-count changes;
- bounded secondary-page application moves to another existing Home page through the guarded Room placement path;
- guarded nearest-free-cell and exact one-cell movement for supported secondary-page app items;
- All apps with local label/package search and app launching; and
- persisted System / Light / Dark appearance selection.

## Approved target capability areas

The approved product scope extends substantially beyond the current Development surface. Major target areas include:

- deeply customizable multi-page Home layouts, grids, icons, labels, margins, folders, widgets, dock behavior, and device-adaptive layouts;
- a richer application drawer with custom grids, folders/tabs, categorization, smart groups, suggestions, recency/frequency surfaces, and privacy-aware organization;
- GoreeCloud Search across installed apps, application content, contacts, settings, files/documents/screenshots, actions, the web, application store, GoreeCloud services, and compatible connected devices;
- icon packs, GoreeCloud-native/adaptive themed icons, custom icon shapes, wallpaper-derived palettes, transparent surfaces, and adaptive Glaze UI visual themes;
- configurable gestures and direct contextual actions;
- optional contextual recommendations and cards for calendar, weather, travel, package delivery, navigation, media, files, devices, privacy/security state, and GoreeCloud services;
- notification badges/previews with privacy-aware visibility;
- smart folders and complete widget workflows;
- adaptive dock, per-page/per-device/per-display layout configuration, foldable/tablet/desktop-style modes, and accessibility-aware motion;
- application-management shortcuts into Android/GoreeCloud permission, storage, notification, privacy, and security controls;
- launcher-layout backup/restore, configuration history, migration, Sync continuity, Everkeep preservation, and safe device-replacement recovery; and
- applicable integration with Drive, Sync, Backups, Everkeep, Identity, Privacy Shield, Wardveil Security, Mesh, Location, Mail, Messenger, Maps, Calendar, Glaze UI, and other compatible GoreeCloud services.

See [FEATURES.md](FEATURES.md) for the detailed approved scope and implementation-state boundaries.

## Glaze UI 2.0 boundary

Launcher targets the current **Glaze UI 2.0.0 Stable** design-system baseline through its repository Adoption Candidate mapping. Historical Glaze UI 1.x references are not the current design authority.

Source governance and mapped native tokens do not by themselves establish complete rendered/native/accessibility acceptance. Representative phone/tablet/foldable behavior, physical touch interaction, TalkBack/switch-access behavior, contrast, reduced-transparency/reduced-motion behavior where applicable, and other production design gates remain separate evidence requirements.

## Workspace persistence and multi-page foundation

Launcher contains:

- Preferences DataStore compatibility/workspace state;
- AndroidX Room relational workspace pages/items and schema history;
- mirror, verification, dual-read, startup reconciliation, and durable authority-state contracts;
- guarded terminal `ROOM` authority primitives;
- deterministic framework-independent grid and multi-page placement validation/mutation contracts;
- Room-backed multi-page HOME page-order persistence for terminal Room authority;
- Room-backed cross-page HOME item placement persistence that validates the complete workspace model and rechecks the observed page/item snapshot inside the transaction before writing; and
- a terminal-Room paged HOME observer that maps authoritative Room pages/items into rendered application-page state and drives page selection in the Development Home UI.

### Current rendered multi-page boundary

When terminal Room authority is active, Launcher can expose page selection, create empty pages, delete only revalidated empty non-primary pages, reorder pages, render application items from secondary Room pages, and request that an application on a secondary page move to another existing Home page.

The page selector is lazy rather than eagerly rendering an unbounded Row. It presents the authoritative app count and any unsupported-item count for each page, and the current selected page is automatically scrolled into view after selection/reorder/page-count changes. This presentation behavior does not own or mutate workspace placement.

For bounded app movement, the UI identifies the source page and application key. Room-authoritative state is read to identify exactly one source item and derive the permitted target from the current coordinate envelope. Actual writes are delegated to existing guarded Room transactions, which re-read and validate the complete HOME page/item snapshot. Concurrent changes, malformed/null placement state, collisions, missing pages/items, ambiguous source identity, occupied exact-cell targets, or out-of-bounds moves fail closed instead of overwriting newer state.

The primary Home page keeps the existing Favorites/Dock management path. Arbitrary drag across pages and moving primary-page Favorites through the secondary-page menu remain separate interaction milestones.

Unsupported Room item types are counted and surfaced rather than silently treated as applications. Folders, shortcuts, widgets, populated-page deletion, and destructive recovery/undo remain separate milestones.

This is a Development rendering/editing bridge, not a complete multi-page editor.

## Current Room item/page safety behavior

Post-cutover Room mutation APIs refuse access unless durable terminal Room authority is already established. Multi-page item moves fail closed on missing pages/items, identity substitution, invalid/null placement coordinates, collisions, out-of-bounds state, or a workspace snapshot that changes between validation and the transactional write.

Page creation validates the complete current page set before appending an empty page. Page deletion is limited to empty non-primary pages and repeats page/item snapshot and emptiness checks inside the transaction before the foreign-key cascade can run.

The rendered page observer is likewise Room-authority gated. If authoritative paged Room state is unavailable, the application does not fabricate secondary pages and remains on the accepted safe path.

Source/emulator acceptance of these paths does not equal complete physical-device Home acceptance or Stable qualification.

## Build baseline

Current Android source uses Kotlin, Jetpack Compose, AndroidX Room/SQLite, DataStore, and Android SDK 36-era build tooling. See repository Gradle files and CI for the exact pinned versions used by the current source tree rather than relying on copied version text in external documentation.

Run the repository validation commands documented by CI, including privacy/manifest/Glaze guards, lint, JVM tests, debug assembly, Room schema checks, and the complete Android instrumentation suite on the configured emulator target.

## Current limitations

Still incomplete or separately gated:

- mature cross-page drag/drop and direct live cell/span editing UI;
- primary-page-to-secondary-page item movement through the rendered editor;
- populated-page deletion plus confirmation/recovery/undo semantics;
- folders, shortcuts, widgets/AppWidgetHost, and richer placement UI;
- complete icon/label customization and broader gesture bindings;
- complete first-party Glaze Theme Engine behavior;
- complete GoreeCloud Search/context/feed behavior described in approved scope;
- accepted cross-device Sync/Mesh/Identity continuity behavior;
- versioned backup/restore and Everkeep acceptance;
- complete applicable Privacy Shield and Wardveil Security integration acceptance;
- Android OS process-death/schema-upgrade recovery acceptance;
- representative physical-device default-HOME and drag interaction acceptance;
- signed release packaging/distribution; and
- production/Stable qualification.

## Documentation

- [USER-MANUAL.md](USER-MANUAL.md) — current Development user guidance.
- [SPECIFICATIONS.md](SPECIFICATIONS.md) — launcher architecture, scope, and authority boundaries.
- [FEATURES.md](FEATURES.md) — implemented Development behavior plus the approved target capability inventory.
- [BENEFITS.md](BENEFITS.md) — supportable current benefits and intended product benefits.
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — first-party product objectives and competitive completeness targets.
- [Rendered HOME page navigation](docs/rendered-home-page-navigation.md) — current terminal-Room page rendering and bounded page-editing boundary.
- `docs/` — architecture, persistence, Glaze UI adoption, validation, and implementation records.
- Canonical application/service project specifications are maintained under `GoreeCloud/Projects` in the authorized GoreeCloud project documentation scope.

## License

GPL-3.0. See `LICENSE`.
