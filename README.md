# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application and the intended native home, application-navigation, search, personalization, and contextual-access experience for GoreeCloud devices. It is not a fork or visual clone of another launcher.

## Status

**Development — not a signed production/Stable release.**

The current repository contains a usable native daily-launcher foundation, a rebuilt Home / Apps / Launcher Settings product shell, and guarded Room-authority multi-page workspace foundations. Passing CI/emulator tests does not establish complete physical-device interaction acceptance, signed release, complete platform integration, or Stable qualification.

The complete approved target capability inventory is maintained in [FEATURES.md](FEATURES.md). Target capabilities are not implementation claims unless separately identified as current Development behavior and supported by repository evidence.

## Product role

Launcher is intended to become the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together within one adaptive interface.

Launcher intelligence and personalization should remain transparent and user-controlled.

## Product rules

- No ads, sponsorships, promoted apps, affiliate placement, or monetized search ranking.
- No behavioral advertising or mandatory analytics.
- Core Home and local application use remain offline-capable.
- No required GoreeCloud server or account for core launcher use.
- Minimal, documented permissions; current source has no Android `INTERNET` permission.
- No broad `QUERY_ALL_PACKAGES` access for ordinary launcher discovery.
- Glaze UI is the Design Center authority for applicable interface behavior.
- Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, and GoreeCloud Mesh govern their applicable platform boundaries.
- A platform integration is not considered implemented merely because it appears in approved scope or UI copy.

## Current rebuilt daily-launcher shell

Current Development source includes:

- Android HOME activity and user-controlled `ROLE_HOME` onboarding;
- lifecycle-aware default-HOME status;
- scoped Android package visibility for `MAIN` + `LAUNCHER` activities and `LauncherApps` discovery across available profiles;
- package/profile lifecycle refresh and stable launcher-item deduplication;
- a distinct wallpaper-backed **Home** surface rather than an engineering Favorites screen;
- a separate **Apps** surface with local label/package search and launching;
- a separate scrollable **Launcher Settings** surface;
- locally persisted Home grid presets, Apps-grid columns, app-label visibility, icon-size preference, and System / Light / Dark appearance;
- ordered persisted Home Favorites and a five-item Dock;
- long-press placement management with accessible earlier/later controls;
- terminal-Room multi-page HOME observation, page selection, protected-primary/secondary-page reordering, empty-page creation/deletion, and secondary application pages;
- a compact/lazy Home page selector with authoritative accessibility context;
- secondary-page icons rendered as normal launcher tiles, with movement/cell controls moved behind long-press management instead of permanently shown beneath every app;
- bounded secondary-to-secondary page movement, nearest-free-cell movement, and guarded exact one-cell movement; and
- Android system wallpaper presentation through the native window-wallpaper mechanism without requesting wallpaper/storage privileges.

## Complete app discovery boundary

Android 11+ package visibility requires launchers to declare which external activity class they need to discover. GoreeCloud Launcher declares a scoped `MAIN` + `LAUNCHER` visibility query and continues to use `LauncherApps` for actual launchable-activity discovery.

This fixes the earlier Development APK behavior where only a small visible subset of installed apps could appear. It does **not** add `QUERY_ALL_PACKAGES`, Internet access, analytics, or an installed-application export path.

## Home, Apps, and Settings

### Home

Home uses the system wallpaper behind the launcher-owned surface, renders the current app grid and Dock, and keeps placement management behind long-press. The protected primary compatibility Home remains rank zero while the separate primary-grid migration is still pending.

### Apps

Apps presents the launchable application inventory in a configurable 4/5/6-column grid and supports local search by label/package. Page-management controls are not overlaid on the Apps surface.

### Launcher Settings

Current persisted settings include supported Home-grid presets, Apps columns, Small/Medium/Large icon presentation, app-label visibility, and System/Light/Dark appearance. These are presentation preferences; they do not widen Room workspace mutation authority.

## Multi-page Room boundary

When terminal Room authority is active, Launcher can expose page selection, create empty pages, delete only revalidated empty non-primary pages, reorder secondary pages while keeping the protected primary page at rank zero, render secondary application pages, and request supported secondary spatial moves.

Room writes continue to verify the protected primary compatibility projection and re-read the complete HOME page/item snapshot so concurrent changes, malformed placement, collisions, invalid bounds, or attempts to use primary Home as a secondary spatial source/target fail closed.

This remains a Development editing bridge, not a complete multi-page drag/drop editor.

## Launcher Unified Search — approved next capability

The approved product interaction now defines a **one-finger swipe downward on an unobstructed Home area** as the default direct gesture for opening **Launcher Unified Search**.

Launcher Unified Search is intended to be a first-party **local-first orchestration surface**, not merely a web search box. Planned providers include installed apps, shortcuts/actions, Launcher/device settings, contacts when enabled and permissioned, scoped photos/screenshots/media, files/documents exposed through Android-supported providers, explicit first-party GoreeCloud app search contracts, authorized GoreeCloud Drive content, and other compatible local/connected-device sources.

**GoreeCloud Search is an optional first-party online provider** for web/current-information categories. It is not the authority for the private Launcher device index, and local result payloads must not be uploaded merely to obtain local results.

This swipe-down unified search and its broader providers are approved scope but are **not implemented/accepted by the current beta-shell rebuild yet**.

## Official Launcher identity

GoreeCloud Launcher requires a unique product-specific official icon/logo/artwork. The current generic Android/framework-style placeholder must not be treated as the official release identity.

The approved canonical artwork must live in this repository and produce traceable Android adaptive foreground/background resources, monochrome/themed icon resources, and other required cross-platform derivatives from one recognizable product identity. No generated/unreviewed, upstream, framework-default, or generic GoreeCloud platform-logo substitute is accepted as the official Launcher mark.

An approved canonical Launcher artwork asset has not yet been committed, so product-identity acceptance remains incomplete.

## Privacy and search architecture

Core Launcher behavior remains local-first. Sensitive unified-search sources must be separately controlled where required. Contacts require explicit enablement and Android permission. Media must use scoped Android APIs. Files/documents must use supported document/provider access rather than unrestricted filesystem privileges. Work/private profiles must remain appropriately isolated.

Search history is intended to remain local, separately clearable, and disableable. Optional contextual/local ranking must remain transparent and user-controlled. There is no sponsored or paid ranking.

## Glaze UI boundary

Launcher retains repository-level Glaze UI Adoption Candidate evidence. Source token mapping and automated guards do not establish complete rendered/native/accessibility/device acceptance. Representative phones/tablets/foldables, physical touch behavior, TalkBack/switch access, contrast, reduced-transparency/reduced-motion behavior, and other production design gates remain separate evidence requirements.

## Current limitations

Still incomplete or separately gated:

- mature cross-page drag/drop and live cell/span editing;
- primary compatibility-page grid migration and primary↔secondary spatial movement;
- populated-page deletion with confirmation/recovery/undo;
- folders, shortcuts, widgets/AppWidgetHost, and richer workspace editing;
- complete icon/theme customization and broader configurable gestures;
- one-finger swipe-down Launcher Unified Search implementation;
- local files/photos/documents/contacts search providers and first-party searchable-content contracts;
- GoreeCloud Search provider integration inside Launcher Unified Search;
- approved official Launcher artwork and derivative asset pipeline;
- complete Glaze Theme Engine behavior;
- accepted cross-device Sync/Mesh/Identity continuity;
- versioned backup/restore and Everkeep acceptance;
- complete Privacy Shield and Wardveil Security integration acceptance;
- Android OS process-death/schema-upgrade recovery acceptance;
- representative physical-device default-HOME interaction acceptance;
- signed release packaging/distribution; and
- production/Stable qualification.

## Documentation

- [USER-MANUAL.md](USER-MANUAL.md) — current Development behavior and user guidance.
- [SPECIFICATIONS.md](SPECIFICATIONS.md) — architecture, scope, and authority boundaries.
- [FEATURES.md](FEATURES.md) — implemented Development behavior plus approved target scope.
- [BENEFITS.md](BENEFITS.md) — current and intended benefits.
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — product completeness objectives.
- [Rendered HOME page navigation](docs/rendered-home-page-navigation.md) — terminal-Room page behavior.
- `docs/` — architecture, persistence, design-system, validation, and implementation records.

Canonical project specifications and acceptance/change records are maintained in the authorized GoreeCloud project documentation hierarchy.

## Build baseline

Current Android source uses Kotlin, Jetpack Compose, AndroidX Room/SQLite, DataStore, and Android SDK 36-era tooling. CI enforces privacy/manifest/Glaze/Room guards, Android lint, JVM tests, debug assembly, Room schema validation, and an Android 16 runtime-emulator suite.

## License

GPL-3.0. See `LICENSE`.
