# GoreeCloud Launcher Specifications

## Status

**Active Development. Not Stable. Production/release acceptance is incomplete.**

GoreeCloud Launcher is GoreeCloud's first-party native Android launcher and the intended home, application-navigation, personalization, and contextual-access experience for GoreeCloud devices. It is also a primary Android invocation/presentation surface for GoreeCloud Index, the canonical GoreeCloud unified/universal search and indexing authority.

This repository specification describes both current implementation architecture and approved product direction. [FEATURES.md](FEATURES.md) maintains the detailed capability inventory. A target capability is not an implementation or acceptance claim unless repository evidence separately establishes that state.

## Product role

Launcher is intended to serve as the personalized front door to GoreeCloud: applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions in one adaptive interface.

Universal search presented from Launcher is powered by GoreeCloud Index. Launcher remains authoritative for Launcher-specific interaction and state; it does not own a parallel universal index or cross-provider ranking authority.

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
- Universal search invocation is delegated through an explicit GoreeCloud Index action contract rather than duplicating Index provider/index/ranking logic inside Launcher.
- Privacy, security, continuity, identity, design, and cross-service responsibilities remain separated into applicable GoreeCloud platform-system boundaries.

## Current daily-launcher shell

### Home

The rebuilt primary Home is a launcher-style surface rather than an engineering Favorites screen. Android renders the system wallpaper behind the launcher window through the native window-wallpaper contract, requiring no wallpaper/storage privilege. The primary surface renders the current Home application grid, Dock, Apps affordance, Launcher Settings affordance, and a Search GoreeCloud affordance that invokes GoreeCloud Index.

Current supported presentation settings include Home grid presets within the 4–6 column / 4–7 row bounds exposed by the UI, Apps columns of 4/5/6, Small/Medium/Large icon presentation, app-label visibility, and System/Light/Dark appearance.

The primary `WorkspaceLegacyImportMapper.HOME_PAGE_ID` page remains the protected compatibility representation for Favorites. Its canonical compatibility items retain null grid coordinates and its rank remains zero. The presentation grid does not silently convert this authority model into the secondary spatial model.

### Apps

The Apps surface displays the launchable inventory provided through `LauncherApps`, supports a narrow local filter by label/package, and launches selected applications. Long-press opens current placement management. Home page controls do not overlay the Apps surface.

The Apps filter is a Launcher-specific navigation feature. It is not GoreeCloud Index and must not become a second universal-search provider/ranking pipeline.

### Launcher Settings

Launcher Settings is a distinct scrollable surface for current persisted presentation options. Settings changes affect rendering; they do not widen Room workspace mutation authority.

Approved target settings expand this surface substantially. Launcher must provide a dedicated native Theme Manager with Glaze Theme Engine integration, theme previews, icon-pack support, bounded icon masking and scaling/normalization, and coherent Home/Apps/folder/dock styling. Launcher Settings must also provide a Home layout-lock switch, GoreeCloud Index Home-entry mode selection (`Permanent on Home` or `Swipe down only`), and explicit local/offline-capable `Backup Launcher configuration` and `Restore Launcher configuration` actions. These target capabilities remain separately implementation- and acceptance-gated until repository/runtime evidence establishes them.

## Home layout lock target

The Home screen layout lock is intended to protect placement-changing operations for supported placeable content, including applications, shortcuts, widgets, folders, pages, and other Launcher-owned layout elements as those capabilities become implemented. Normal launching and non-placement interactions remain available while locked.

The deterministic accessible unlock path is the Launcher Settings switch. Launcher should additionally support intentional one-finger hold on an unobstructed Home area for five seconds, with clear progressive/completion feedback and cancellation when the gesture ceases to qualify. The five-second gesture must not require a privileged accessibility service and must not be the only unlock path.

## Theme Manager and icon presentation target

Launcher must natively support a dedicated Theme Manager rather than treating appearance as a single cycle action. The approved target includes current System/Light/Dark behavior, applicable Glaze Theme Engine modes, theme previews, wallpaper-derived/user-selected palettes where supported, icon-pack discovery/application, icon masking, bounded icon scaling/optical normalization, fallback/reset behavior, and coherent styling across Launcher-owned surfaces.

Third-party application identity must remain recognizable. Icon packs, masks, frames, normalization, and scaling may adapt presentation but must not misleadingly replace third-party brand identity, crop essential icon content, or bypass the current GoreeCloud/Glaze UI icon standards.

## Launcher configuration backup/restore target

Launcher Settings must expose explicit backup and restore actions using a versioned documented format. Core backup/restore must remain local/offline-capable and must not require a GoreeCloud account or network service. Imported backup data is untrusted input and must be structurally/version validated before any mutation; malformed or unsupported data must fail safely.

Backup scope should include supported Launcher preferences, Home/Dock/page organization, Theme Manager and icon-presentation choices, Index Home-entry mode, layout-lock state where appropriate, folders/shortcuts, and other Launcher-owned configuration. Widget restoration must use safe rebinding/reconfiguration semantics rather than copying stale Android AppWidget IDs. Everkeep, GoreeCloud Backup, Drive, and Sync may later transport/preserve the same validated format when separately authorized and accepted.

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

## GoreeCloud Index integration and universal search authority

### Authority model

**GoreeCloud Index is the canonical unified/universal first-party search and indexing authority.** It owns universal query lifecycle, provider discovery, authorization-aware query dispatch, local indexing where required, common result normalization, source provenance, ranking, grouping, deduplication, and universal result actions.

GoreeCloud Launcher is a primary Android invocation and presentation surface for Index. Launcher owns Home interaction, gesture detection, launcher-specific navigation, and Launcher-owned context exposed through explicit provider contracts. Launcher must not maintain a rival hidden cross-provider index, normalization pipeline, or universal ranking engine.

A user-facing Launcher entry point may be described as Launcher Unified Search when referring to the interaction surface, but its universal results are Index-powered.

### Default interaction and current Development handoff

A **one-finger downward swipe on an unobstructed Home-screen area** is the approved default direct gesture for opening universal search. Launcher also currently provides a visible **Search GoreeCloud** Home affordance so universal search does not depend on gesture discovery.

The approved configurable Home-entry modes are:

- **Permanent on Home** — keep a polished native Glaze UI GoreeCloud Index affordance visible on Home.
- **Swipe down only** — hide the persistent Home affordance while retaining one-finger downward invocation.

Both modes must preserve an accessible non-gesture path to search. This preference controls invocation/presentation only; it does not transfer indexing or ranking authority from GoreeCloud Index to Launcher.

The current Development integration invokes the explicit action `com.goreecloud.index.action.SEARCH`. The bounded handoff recognizes the production Index package `com.goreecloud.index` and Development package `com.goreecloud.index.dev`. If no compatible Index activity resolves, Launcher reports the unavailable state instead of silently substituting an independent universal-search implementation.

The search experience must provide equivalent hardware-keyboard, switch-access, screen-reader, and other supported non-gesture paths and must not require understanding gesture animation to operate.

### Search presentation quality target

The unified-search experience should achieve the immediacy, clarity, visual polish, and result legibility associated with leading modern universal-search products while remaining an original GoreeCloud design rather than a copy of Apple Spotlight, Pixel search, or another product. The current product target is **Glaze UI 2.1.0 Stable**, with application-specific implementation and acceptance still evidence-gated.

The intended Glaze UI search experience uses strong typography/hierarchy, a prominent immediately focused query field, restrained context-aware translucency/depth where useful, grouped source-aware result surfaces, clear provider/provenance cues, compact direct actions, bounded fluid motion, reduced-motion/reduced-transparency alternatives, predictable keyboard behavior, and first-class TalkBack/switch-access semantics. GoreeCloud Index owns the full result experience; Launcher owns Home invocation and any persistent Home entry affordance.

### Launcher as an Index provider

Launcher may expose installed applications, shortcuts/direct actions, Launcher actions/settings, folders, widgets, and other Launcher-owned contextual records through a versioned Index provider contract. Index must not bypass that contract by directly reading Launcher private persistence.

The current Index Android foundation has its own scoped installed-applications provider. A richer Launcher provider is planned separately and must preserve Android `LauncherApps`, profile, shortcut, and Launcher workspace authority rather than duplicating or scraping private data.

### Broader Index provider model

Planned Index provider classes include:

- installed applications and approved application actions;
- contacts only when explicitly authorized;
- calendar events through authoritative calendar/provider contracts;
- photos, screenshots, videos, and other supported media through scoped Android media APIs;
- files/documents exposed through supported Android document/provider access or user-granted locations;
- explicit first-party GoreeCloud searchable-content contracts rather than scraping another app's private storage;
- authorized GoreeCloud Drive content;
- compatible connected-device providers when separately authorized;
- extensions and optional third-party service providers under explicit trust/authorization boundaries; and
- GoreeCloud Search for Internet/Web/current-information results.

These providers belong to the Index query/provider pipeline rather than separate Launcher-owned universal-search logic.

### GoreeCloud Search boundary

GoreeCloud Search is the first-party provider for Internet, Web, and current-information categories. Launcher reaches those categories through GoreeCloud Index when enabled and authorized. GoreeCloud Search does not own or receive the private Launcher state or unrelated local Index results by default. Local files, photos, contacts, installed-app inventory, Launcher history, and local result payloads must not be uploaded merely to produce local search results.

The integration should preserve Launcher’s current offline-capable/no-`INTERNET` core. Any later direct Launcher networking requires separate destination/privacy/failure review and acceptance.

### Android and GoreeCloud platform boundaries

Universal search does not justify unrestricted filesystem or package privileges. File/document coverage must use supported document providers, user-granted locations, app-owned provider contracts, Android search/index APIs, or other platform-compliant access. Media search must use scoped APIs. Work/private profile isolation must follow Android and GoreeCloud Identity policy.

Privacy Shield governs applicable purpose/consent/minimization/retention/remote-processing controls. Wardveil Security governs applicable provider/action trust evidence. GoreeCloud Identity governs identity/profile/scoped authorization. GoreeCloud Mesh may coordinate bounded first-party provider discovery. Everkeep governs applicable durable configuration/continuity. Glaze UI governs the user-facing search presentation. None of these integrations may be inferred merely from UI labels.

### Ranking and actions

Universal ranking belongs to Index. Exact/prefix matches should generally outrank weaker fuzzy matches. Optional recency/frequency/context signals must remain transparent, disableable, and appropriately local or authority-bounded. There is no sponsored, promoted, affiliate, or advertising ranking.

Supported results may expose direct actions such as launch app, invoke shortcut, open file/photo, contact communication action, open setting, open a GoreeCloud service result, or hand an online query to GoreeCloud Search. External intents, deep links, provider payloads, and cross-app searchable-content records are untrusted input and require validation.

### Privacy and controls

Sensitive/permissioned Index source categories must be independently controllable. History, provider controls, local/remote processing disclosure, and index/cache clearing belong to the applicable Index/platform contracts. Local providers should continue to function when remote providers are disabled or unavailable. Launcher does not gain universal-search authority merely because Index is temporarily unavailable.

### Current acceptance boundary

PR #53 implemented the Launcher-side Search GoreeCloud Home affordance, one-finger downward invocation, scoped package visibility for the Index action, and bounded activity handoff. Exact PR head `b18cfa05a1b18243e52046ef581cb67fc3298a5f` passed Android CI run `33417830081`, including the Android 16 runtime-emulator suite, and merged to `main` as `fde148081cc292bcdfd7e221312fe830515331fb`. Post-merge main CI remains a separately verified acceptance record.

The Index Android foundation implements its first scoped installed-applications provider and provider-neutral query/result engine. Contacts, files/documents, calendar, media, Drive, first-party app-content providers, connected devices, extensions, third-party providers, GoreeCloud Search integration, complete platform-runtime adoption, representative-device accessibility/gesture acceptance, production signing/deployment, and Stable qualification remain separate gates.

## Official Launcher product identity specification

Launcher requires a unique first-party visual identity distinct from framework defaults, upstream products, and the generic GoreeCloud platform/corporate logo.

All canonical Launcher logos, icons, symbols, illustrations, and artwork must be stored, reviewed, and approved in **`GoreeCloud/goreecloud-branding-assets`**. The current canonical Launcher source is `products/launcher/app-icon.svg`. `GoreeCloud/goreecloud-launcher` is a consumer repository and may carry only traceable synchronized/generated/packaged Android derivatives required for the application.

`branding/provenance.json` records the canonical repository/path/blob used to create the current Android adaptive, round, and monochrome derivatives. A consumer derivative must never be edited into an independent canonical source. Future visual revisions begin in `goreecloud-branding-assets`, then propagate through a traceable derivative update.

The previous Launcher-local portal/activity-tile source is superseded and removed because it conflicted with the project-wide branding source-of-truth rule.

The existence of a canonical asset and synchronized Development derivatives does not by itself establish production visual-identity acceptance. Rendered small-size, adaptive-mask, themed-icon, representative-device/system-chooser, and release review remain required.

## Approved product capability domains

Detailed approved features are maintained in [FEATURES.md](FEATURES.md). Major domains include customizable Home/workspace, rich Apps organization, GoreeCloud Index-powered universal search, GoreeCloud Search Web/current-information provider integration through Index, native Theme Manager/icon packs/masking/scaling, Home layout locking, configurable Index Home entry, appearance/personalization, gestures, contextual experiences, feeds/cards, notifications, folders, widgets, adaptive Dock/layout/motion, application management, versioned local backup/restore plus optional continuity transports, and applicable GoreeCloud platform integrations.

## GoreeCloud platform integration boundaries

Naming an integration establishes no implementation claim. Each participating system must satisfy its own implementation, authorization, privacy, security, availability, and acceptance boundary.

- **GoreeCloud Index** is the universal query/provider/index/normalization/ranking authority and accepts Launcher as an invocation/presentation surface and future provider.
- **Glaze UI / Design Center** governs visual hierarchy, components, motion, responsiveness, accessibility, adaptive layouts, wallpaper-aware presentation, and design-system acceptance.
- **Privacy Shield / Privacy Center** governs personalization signals, sensitive search sources/results, history exposure, consent, location/usage-derived behavior, and user control.
- **Wardveil Security / Security Center** governs applicable package trust, risky cross-application actions, security-state surfaces, and protection of Launcher configuration/search integration state.
- **Everkeep / Continuity Center** governs accepted preservation, backup/recovery, portability, and device-transition continuity.
- **GoreeCloud Identity** governs profiles, authentication/authorization, managed application visibility, and identity-aware continuity.
- **GoreeCloud Mesh** governs authorized cross-device coordination, device awareness, handoff, connected-device results/cards, and coordinated Launcher state.
- **GoreeCloud Drive** may provide authorized recent/searchable files/folders through its appropriate Index/provider contracts plus launcher shortcuts/widgets.
- **GoreeCloud Search** provides optional Web/current-information results through Index without becoming local-index authority.
- **Sync/Backups/Location/Maps/Mail/Messenger/Calendar** may provide their approved integrations only where substantively implemented and accepted.

## Authority and privacy principles

- Android remains authoritative for installed/launchable applications, platform roles, and operating-system launcher capabilities.
- GoreeCloud Index remains authoritative for universal search orchestration/indexing/ranking; Launcher remains an invocation/presentation surface and source-specific participant.
- `GoreeCloud/goreecloud-branding-assets` remains authoritative for all GoreeCloud logos/icons/artwork; consumer repositories carry derivatives only.
- GoreeCloud workspace persistence maintains one accepted placement authority at a time.
- Compatibility and secondary spatial models must not be mixed in ways that invalidate authority/recovery.
- Cross-device continuity must not create ambiguous writable workspace authorities.
- Search/personalization signals should remain transparent and user-controlled.
- Sensitive content must not be exposed through search/cards/widgets/notifications without applicable authorization/privacy policy.
- Platform integration must be substantive; visual labels do not prove integration.
- Core Home and current Apps behavior remain offline-capable and currently request no Android `INTERNET` permission.

## Stable blockers

Stable qualification still requires, as applicable:

- accepted production Launcher identity artwork and derivative asset pipeline from the canonical branding repository;
- accepted GoreeCloud Index integration with the release-intended provider set and Launcher invocation/presentation behavior;
- accepted native Theme Manager/icon-pack/masking/scaling behavior for release scope;
- accepted Home layout-lock enforcement and accessible unlock behavior across supported placeable content;
- accepted configurable Index Home-entry modes;
- accepted versioned Launcher backup/restore behavior and safe migration/rebinding semantics;
- complete intended workspace/user flows and recovery semantics;
- accepted primary compatibility-page grid migration and complete intended cross-page movement semantics;
- folders/widgets/shortcuts required by release scope;
- mature cross-page placement editing and accessible alternatives;
- representative-device, rotation/posture, performance, physical-interaction, universal-search gesture, and accessibility acceptance;
- complete current Glaze UI application acceptance, currently targeting Glaze UI 2.1.0 Stable;
- accepted applicable Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, Index, Search, Sync, Backup, and continuity integrations;
- Android process-death and schema-upgrade recovery evidence;
- signed distribution and upgrade/recovery validation; and
- release/production evidence supporting every capability represented as implemented.
