# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application and the intended native home, application-navigation, personalization, and contextual-access experience for GoreeCloud devices. It is also a primary first-party Android entry point into **GoreeCloud Index**, the canonical GoreeCloud unified/universal search and indexing system. Launcher is not a fork or visual clone of another launcher.

## Status

**Development — not a signed production/Stable release.**

The current repository contains a usable native daily-launcher foundation, a rebuilt Home / Apps / Launcher Settings product shell, guarded Room-authority multi-page workspace foundations, and a Development Launcher-to-Index universal-search handoff. Passing CI/emulator tests does not establish complete physical-device interaction acceptance, signed release, complete platform integration, or Stable qualification.

The complete approved target capability inventory is maintained in [FEATURES.md](FEATURES.md). Target capabilities are not implementation claims unless separately identified as current Development behavior and supported by repository evidence.

## Product role

Launcher is intended to become the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together within one adaptive interface.

**GoreeCloud Index owns universal search orchestration and indexing.** Launcher owns launcher interaction/presentation and may expose Launcher-specific searchable context through explicit provider contracts. GoreeCloud Search remains the Internet/Web/current-information provider that Index may invoke when authorized.

Launcher intelligence and personalization should remain transparent and user-controlled.

## Product rules

- No ads, sponsorships, promoted apps, affiliate placement, or monetized search ranking.
- No behavioral advertising or mandatory analytics.
- Core Home and local application use remain offline-capable.
- No required GoreeCloud server or account for core launcher use.
- Minimal, documented permissions; current source has no Android `INTERNET` permission.
- No broad `QUERY_ALL_PACKAGES` access for ordinary launcher discovery.
- Universal search is delegated to GoreeCloud Index rather than duplicated as a hidden Launcher-owned index/ranking engine.
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
- a separate **Apps** surface with local label/package filtering and launching;
- a separate scrollable **Launcher Settings** surface;
- locally persisted Home grid presets, Apps-grid columns, app-label visibility, icon-size preference, and System / Light / Dark appearance;
- ordered persisted Home Favorites and a five-item Dock;
- long-press placement management with accessible earlier/later controls;
- terminal-Room multi-page HOME observation, page selection, protected-primary/secondary-page reordering, empty-page creation/deletion, and secondary application pages;
- a compact/lazy Home page selector with authoritative accessibility context;
- secondary-page icons rendered as normal launcher tiles, with movement/cell controls moved behind long-press management instead of permanently shown beneath every app;
- bounded secondary-to-secondary page movement, nearest-free-cell movement, and guarded exact one-cell movement;
- a visible **Search GoreeCloud** Home affordance that hands universal search to GoreeCloud Index;
- the approved one-finger downward Home gesture wired to the same Index handoff;
- bounded package visibility for the Index search action without broad package access; and
- Android system wallpaper presentation through the native window-wallpaper mechanism without requesting wallpaper/storage privileges.

## Complete app discovery boundary

Android 11+ package visibility requires launchers to declare which external activity class they need to discover. GoreeCloud Launcher declares a scoped `MAIN` + `LAUNCHER` visibility query and continues to use `LauncherApps` for actual launchable-activity discovery.

The Launcher-to-Index integration adds only a scoped visibility declaration for `com.goreecloud.index.action.SEARCH`. It does **not** add `QUERY_ALL_PACKAGES`, Internet access, analytics, or an installed-application export path.

## Home, Apps, and Settings

### Home

Home uses the system wallpaper behind the launcher-owned surface, renders the current app grid and Dock, and keeps placement management behind long-press. The protected primary compatibility Home remains rank zero while the separate primary-grid migration is still pending.

Home now provides a visible Search GoreeCloud control and an unobstructed top-area downward-swipe path that invoke GoreeCloud Index. If Index is unavailable, Launcher reports that state instead of silently substituting a rival universal search engine.

### Apps

Apps presents the launchable application inventory in a configurable 4/5/6-column grid and supports a narrow local filter by label/package. This **Search apps** filter is a Launcher navigation feature, not GoreeCloud universal search and not an alternative Index provider/ranking pipeline.

### Launcher Settings

Current persisted settings include supported Home-grid presets, Apps columns, Small/Medium/Large icon presentation, app-label visibility, and System/Light/Dark appearance. These are presentation preferences; they do not widen Room workspace mutation authority.

## Multi-page Room boundary

When terminal Room authority is active, Launcher can expose page selection, create empty pages, delete only revalidated empty non-primary pages, reorder secondary pages while keeping the protected primary page at rank zero, render secondary application pages, and request supported secondary spatial moves.

Room writes continue to verify the protected primary compatibility projection and re-read the complete HOME page/item snapshot so concurrent changes, malformed placement, collisions, invalid bounds, or attempts to use primary Home as a secondary spatial source/target fail closed.

This remains a Development editing bridge, not a complete multi-page drag/drop editor.

## GoreeCloud Index universal search integration

**GoreeCloud Index is the canonical unified/universal first-party search and indexing authority.** Launcher is a first-party invocation/presentation surface and a potential provider of Launcher-owned application/action/settings/folder/widget context. Launcher does not own a separate cross-provider index, result normalization system, or universal ranking engine.

The current Development integration uses the explicit action contract `com.goreecloud.index.action.SEARCH`. Launcher can resolve both the production package identity `com.goreecloud.index` and the Development package identity `com.goreecloud.index.dev` while testing. The Home Search GoreeCloud affordance and one-finger downward gesture use this same handoff.

The first Index Android slice currently targets installed applications only. Broader provider classes remain separate Index work, including contacts, calendar, files/documents, media, first-party GoreeCloud application content, Drive, connected devices, extensions, optional third-party services, and Web/current-information results.

**GoreeCloud Search is the Internet/Web/current-information provider**, reached through Index when enabled and authorized. Neither Launcher nor Index should upload unrelated local result payloads merely to obtain local search results.

Sensitive and permissioned providers remain governed by their own Android and GoreeCloud authority boundaries. Android scoped-storage, media, profile, package-visibility, Privacy Shield, Wardveil Security, Identity, Mesh, and Everkeep requirements are not bypassed by universal search.

## Official Launcher identity

GoreeCloud Launcher requires a unique product-specific official icon/logo/artwork. A first-party Launcher identity candidate and Android adaptive/monochrome resources are present in the Development source, but production product-identity acceptance remains a separate review gate.

The approved canonical artwork must live in this repository and produce traceable Android adaptive foreground/background resources, monochrome/themed icon resources, and other required cross-platform derivatives from one recognizable product identity. Generated/unreviewed, upstream, framework-default, or generic GoreeCloud platform-logo substitutes are not accepted as the official Launcher mark.

## Privacy and search architecture

Core Launcher behavior remains local-first. Universal provider participation is controlled by GoreeCloud Index and the applicable source authority rather than by Launcher scraping other applications' private storage. Contacts require explicit authorization. Media must use scoped Android APIs. Files/documents must use supported document/provider access. Work/private profiles must remain appropriately isolated.

Index search history, provider controls, contextual ranking, and remote-provider behavior must remain transparent and user-controlled under the applicable platform contracts. There is no sponsored or paid ranking.

## Glaze UI boundary

Launcher retains repository-level Glaze UI Adoption Candidate evidence. Source token mapping and automated guards do not establish complete rendered/native/accessibility/device acceptance. Representative phones/tablets/foldables, physical touch behavior, TalkBack/switch access, contrast, reduced-transparency/reduced-motion behavior, and other production design gates remain separate evidence requirements.

## Current limitations

Still incomplete or separately gated:

- mature cross-page drag/drop and live cell/span editing;
- primary compatibility-page grid migration and primary↔secondary spatial movement;
- populated-page deletion with confirmation/recovery/undo;
- folders, shortcuts, widgets/AppWidgetHost, and richer workspace editing;
- complete icon/theme customization and broader configurable gestures;
- embedded Index result presentation inside Launcher beyond the current activity handoff;
- Index providers for files/photos/documents/contacts/calendar and first-party searchable-content contracts;
- GoreeCloud Search provider implementation in Index;
- complete production Launcher identity acceptance;
- complete Glaze Theme Engine behavior;
- accepted cross-device Sync/Mesh/Identity continuity;
- versioned backup/restore and Everkeep acceptance;
- complete Privacy Shield and Wardveil Security integration acceptance;
- Android OS process-death/schema-upgrade recovery acceptance;
- representative physical-device default-HOME and universal-search gesture/accessibility acceptance;
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
