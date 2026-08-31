# GoreeCloud Launcher Specifications

## Status

**Active Development. Not Stable. Production/release acceptance is incomplete.**

GoreeCloud Launcher is GoreeCloud's first-party native Android launcher and the intended home, application-navigation, search, personalization, and contextual-access experience for GoreeCloud devices.

This repository specification describes both current implementation architecture and approved product direction. [FEATURES.md](FEATURES.md) maintains the detailed capability inventory. A target capability is not an implementation or acceptance claim unless repository evidence separately establishes that state.

## Product role

Launcher is intended to serve as the personalized front door to GoreeCloud: applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions in one adaptive interface.

Launcher intelligence and personalization should remain transparent and user-controlled.

## Native application requirement

Launcher must remain original GoreeCloud-owned software built from the ground up as a native Android application. Android platform APIs and narrowly justified foundational dependencies may be used where direct operating-system integration, standards compatibility, security, interoperability, rendering, or maintainability requires them.

## Current architecture

- Kotlin + Jetpack Compose with Android-native APIs where launcher contracts require them.
- Android HOME-role onboarding remains user-controlled through platform role authority.
- `LauncherApps` remains authoritative for launchable-activity discovery and profile-aware application identity.
- Android package visibility is scoped to launchable `MAIN` + `LAUNCHER` activities; broad `QUERY_ALL_PACKAGES` access is not used.
- Home, Apps, and Launcher Settings are separate product surfaces.
- Presentation preferences are persisted locally with DataStore.
- Workspace persistence and placement use the guarded Room-backed workspace model for terminal Room paths.
- Rendered paged Home state is projected from authoritative workspace state; UI convenience is not a second placement authority.
- Privacy, security, continuity, identity, design, and cross-service responsibilities remain separated into applicable GoreeCloud platform-system boundaries.

## Current daily-launcher shell

### Home

The rebuilt primary Home is a launcher-style surface rather than an engineering Favorites screen. Android renders the system wallpaper behind the launcher window through the native window-wallpaper contract, requiring no wallpaper/storage privilege. The primary surface renders the current Home application grid, Dock, Apps affordance, and Launcher Settings affordance.

Current supported presentation settings include Home grid presets within the 4–6 column / 4–7 row bounds exposed by the UI, Apps columns of 4/5/6, Small/Medium/Large icon presentation, app-label visibility, and System/Light/Dark appearance.

The primary `WorkspaceLegacyImportMapper.HOME_PAGE_ID` page remains the protected compatibility representation for Favorites. Its canonical compatibility items retain null grid coordinates and its rank remains zero. The new presentation grid does not silently convert this authority model into the secondary spatial model.

### Apps

The Apps surface displays the launchable inventory provided through `LauncherApps`, supports local filtering by label/package, and launches selected applications. Long-press opens current placement management. Home page controls do not overlay the Apps surface.

### Launcher Settings

Launcher Settings is a distinct scrollable surface for current persisted presentation options. Settings changes affect rendering; they do not widen Room workspace mutation authority.

## Current multi-page Home behavior

Current Development source supports:

- authoritative Home page rendering and selection;
- compact/lazy page navigation with authoritative accessibility context;
- guarded page creation, eligible empty-secondary deletion, and secondary page reordering without crossing protected primary rank zero;
- app launching from supported secondary pages;
- ordinary icon-grid rendering for secondary pages rather than permanent engineering controls;
- long-press management for secondary movement actions;
- secondary-to-secondary page movement;
- within-secondary-page nearest-free-cell earlier/later movement;
- guarded exact one-cell left/right/up/down movement;
- fail-closed movement for collisions, invalid bounds, malformed/ambiguous placement, stale snapshots, or primary-page spatial source/target requests; and
- canonical primary/Dock compatibility validation before secondary spatial writes.

Primary-grid coordinates and primary↔secondary spatial item movement require a separate accepted migration.

## Launcher Unified Search architecture — approved next capability

### Default interaction

A **one-finger downward swipe on an unobstructed Home-screen area** is the approved default direct gesture for opening **Launcher Unified Search**.

The gesture opens a Launcher-owned native search overlay/sheet, immediately focuses the query field, and may show the software keyboard where appropriate. The experience must provide equivalent hardware-keyboard, switch-access, and screen-reader paths and must not require understanding gesture animation to operate.

### Ownership and local-first boundary

Launcher Unified Search is a first-party local-first search orchestration, ranking, and action surface. It is not merely a web field and core local results must remain usable without a network connection, GoreeCloud account, or GoreeCloud Search availability.

A normalized provider/result model should support independent providers while keeping source identity visible. Planned provider classes include:

- installed applications through `LauncherApps`;
- app shortcuts/direct actions through supported Android launcher APIs;
- Launcher settings/actions and supported Android settings actions;
- contacts only when explicitly enabled and permissioned;
- photos, screenshots, videos, and other supported media through scoped Android media APIs;
- files/documents exposed through supported Android document/provider access or user-granted locations;
- explicit first-party GoreeCloud searchable-content contracts rather than scraping another app's private storage;
- authorized GoreeCloud Drive content;
- compatible connected-device providers when separately authorized; and
- optional GoreeCloud Search online/web/current-information results.

### GoreeCloud Search boundary

GoreeCloud Search is an optional first-party provider for web/current-information categories. It does not own or receive the private Launcher local index by default. Local files, photos, contacts, installed-app inventory, Launcher history, and local result payloads must not be uploaded merely to produce local search results.

Where practical, the integration should use an explicit first-party provider/handoff contract so Launcher can preserve its current offline-capable/no-`INTERNET` core. Any later direct Launcher networking requires separate destination/privacy/failure review and acceptance.

### Android platform boundaries

Launcher must not request unrestricted filesystem privileges merely to emulate a privileged system index. File/document coverage must use supported document providers, user-granted locations, app-owned provider contracts, Android search/index APIs, or other platform-compliant access. Media search must use scoped APIs. Work/private profile isolation must follow Android and GoreeCloud Identity policy.

### Ranking and actions

Exact/prefix matches should generally outrank fuzzy matches. Optional local recency/frequency/context signals must remain transparent, disableable, and local by default. There is no sponsored, promoted, affiliate, or advertising ranking.

Supported results may expose direct actions such as launch app, invoke shortcut, open file/photo, contact communication action, open setting, open a GoreeCloud service result, or hand the query to GoreeCloud Search. External intents, deep links, provider payloads, and cross-app searchable-content records are untrusted input and require validation.

### Privacy and controls

Sensitive/permissioned source categories must be independently controllable. Search history remains local, separately clearable, and disableable. The user must be able to understand enabled sources and clear/rebuild local index state. Local search must continue to function when online providers are disabled or unavailable.

### Current acceptance boundary

This architecture is approved product scope. The one-finger gesture, overlay, provider interface, local content indexes, contacts/media/document providers, and GoreeCloud Search integration are not claimed as implemented by the current beta-shell rebuild.

## Official Launcher product identity specification

Launcher requires a unique first-party visual identity distinct from framework defaults, upstream products, and the generic GoreeCloud platform/corporate logo.

The canonical source artwork must:

- be stored in `GoreeCloud/goreecloud-launcher`;
- communicate Launcher/system-utility semantics such as application/activity access, navigation, launching, discovery, or the personalized GoreeCloud entry point;
- remain recognizable at small launcher-icon sizes;
- preserve one recognizable identity across Android and other supported surfaces;
- generate traceable Android adaptive foreground/background resources;
- generate a monochrome/themed-icon derivative; and
- produce other required raster/vector derivatives from the same approved source.

Automatically generated/unreviewed artwork, framework/default Android imagery, copied/upstream identities, and a generic corporate-logo substitute do not qualify as the official Launcher identity.

No approved canonical Launcher artwork is currently committed; official product-identity acceptance is therefore incomplete.

## Approved product capability domains

Detailed approved features are maintained in [FEATURES.md](FEATURES.md). Major domains include customizable Home/workspace, rich Apps organization, Launcher Unified Search, GoreeCloud Search provider integration, appearance/personalization, gestures, contextual experiences, feeds/cards, notifications, folders, widgets, adaptive Dock/layout/motion, application management, backup/Sync/configuration, and applicable GoreeCloud platform integrations.

## GoreeCloud platform integration boundaries

Naming an integration establishes no implementation claim. Each participating system must satisfy its own implementation, authorization, privacy, security, availability, and acceptance boundary.

- **Glaze UI / Design Center** governs visual hierarchy, components, motion, responsiveness, accessibility, adaptive layouts, wallpaper-aware presentation, and design-system acceptance.
- **Privacy Shield / Privacy Center** governs personalization signals, sensitive search sources/results, history exposure, consent, location/usage-derived behavior, and user control.
- **Wardveil Security / Security Center** governs applicable package trust, risky cross-application actions, security-state surfaces, and protection of Launcher configuration/index state.
- **Everkeep / Continuity Center** governs accepted preservation, backup/recovery, portability, and device-transition continuity.
- **GoreeCloud Identity** governs profiles, authentication/authorization, managed application visibility, and identity-aware continuity.
- **GoreeCloud Mesh** governs authorized cross-device coordination, device awareness, handoff, connected-device results/cards, and coordinated Launcher state.
- **GoreeCloud Drive** may provide authorized recent/searchable files/folders and launcher shortcuts/widgets.
- **GoreeCloud Search** may provide optional web/current-information results without becoming local-index authority.
- **Sync/Backups/Location/Maps/Mail/Messenger/Calendar** may provide their approved integrations only where substantively implemented and accepted.

## Authority and privacy principles

- Android remains authoritative for installed/launchable applications, platform roles, and operating-system launcher capabilities.
- GoreeCloud workspace persistence maintains one accepted placement authority at a time.
- Compatibility and secondary spatial models must not be mixed in ways that invalidate authority/recovery.
- Cross-device continuity must not create ambiguous writable workspace authorities.
- Search/personalization signals should remain transparent and user-controlled.
- Sensitive content must not be exposed through search/cards/widgets/notifications without applicable authorization/privacy policy.
- Platform integration must be substantive; visual labels do not prove integration.
- Core Home and current Apps behavior remain offline-capable and currently request no Android `INTERNET` permission.

## Stable blockers

Stable qualification still requires, as applicable:

- approved official Launcher identity artwork and derivative asset pipeline;
- implemented/accepted Launcher Unified Search with the release-intended provider set;
- complete intended workspace/user flows and recovery semantics;
- accepted primary compatibility-page grid migration and complete intended cross-page movement semantics;
- folders/widgets/shortcuts required by release scope;
- mature cross-page placement editing and accessible alternatives;
- representative-device, rotation/posture, performance, physical-interaction, and accessibility acceptance;
- complete current Glaze UI application acceptance;
- accepted applicable Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, Search, Sync, Backup, and continuity integrations;
- Android process-death and schema-upgrade recovery evidence;
- signed distribution and upgrade/recovery validation; and
- release/production evidence supporting every capability represented as implemented.
