# GoreeCloud Launcher Specifications

## Status

**Active Development. Not Stable. Production/release acceptance is incomplete.**

GoreeCloud Launcher is GoreeCloud's first-party native Android launcher and the intended home, application-navigation, search, personalization, and contextual-access experience for GoreeCloud devices.

This repository specification describes both the current implementation architecture and the approved product direction. The detailed target capability inventory is maintained in [FEATURES.md](FEATURES.md). A target capability is not an implementation or acceptance claim unless repository evidence separately establishes that state.

## Product role

Launcher is intended to serve as the personalized front door to GoreeCloud. Its long-term role is to bring applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together in one adaptive interface.

Launcher intelligence and personalization should remain transparent and user-controlled.

## Native application requirement

Launcher must remain original GoreeCloud-owned software built from the ground up as a native application. Complete-product forks or adopted launcher implementations must not become the product authority.

Android platform APIs and narrowly justified foundational dependencies may be used where direct operating-system integration, standards compatibility, security, interoperability, rendering, or maintainability requires them.

## Current architecture

- Native Android application using Kotlin and Jetpack Compose, with platform-native Android APIs where launcher contracts require them.
- Android HOME-role onboarding remains user-controlled through Android's platform role authority.
- Android `LauncherApps` remains authoritative for installed/launchable application discovery and available profile/application identity.
- Workspace persistence and authoritative placement are represented through the repository's guarded Room-backed workspace model for terminal Room paths.
- The rendered paged Home projection is read from authoritative workspace state; UI convenience must not become a second workspace source of truth.
- Installed launchable application identity is mapped through the repository's stable workspace-key representation.
- Privacy, security, continuity, identity, design, and cross-service integration must remain separated into their applicable GoreeCloud platform-system responsibilities rather than being implied by visual branding.

## Current Home-page behavior

Current Development source supports:

- multiple authoritative Home pages that can be rendered and selected;
- a lazy/scrollable page selector with authoritative app counts and unsupported-item counts;
- automatic scrolling of the selected page into view after selection, reorder, or page-count changes;
- guarded page creation, reordering, and deletion of eligible empty non-primary pages;
- app launching from supported rendered pages;
- app movement to another existing authoritative page;
- within-page nearest-free-cell earlier/later movement;
- guarded exact one-cell left/right/up/down movement;
- fail-closed movement when targets are occupied, outside the authoritative grid, malformed, ambiguous, or based on stale workspace state; and
- existing Favorites/Dock placement controls on the supported primary surface.

## Approved product capability domains

The approved Launcher product scope includes the following major domains. Detailed features and implementation-state boundaries are in [FEATURES.md](FEATURES.md).

### Home and workspace

- Customizable multi-page Home layouts.
- Independent row/column/grid configuration, icon sizing, margins/padding, sub-grid placement, folders, shortcuts, widgets, docks, page indicators, wallpaper behavior, overlapping supported elements, layout locking, and device-adaptive layouts.

### Application drawer

- Swipe-up All apps experience with custom grids, folders/tabs, categorization, smart groups, suggestions, recency/frequency surfaces, search, hiding, visual customization, and context-sensitive ordering.

### GoreeCloud Search

- Search across installed applications, application content, contacts, device/GoreeCloud settings, files/documents/screenshots, shortcuts/actions, web results, application store, GoreeCloud services, and compatible connected GoreeCloud devices.
- Context-aware recommendations, result-category controls, search-bar personalization, animation, and direct actions.

### Appearance and personalization

- Icon packs, GoreeCloud-native/adaptive themed icons, icon shapes/sizes, per-app/folder icons, wallpaper-derived color systems, interface/accent colors, appearance switching, transparency, custom Home/drawer/folder/dock styling, text styling, context-aware themes, per-device/per-display personalization, reduced motion, and high-contrast options.

### Gestures and interaction

- Configurable swipe, horizontal swipe, double-tap, pinch, two-finger, long-press, drag-and-drop, search, notification, feed, application, shortcut, and GoreeCloud action interactions with accessible non-gesture alternatives where required.

### Smart and contextual experiences

- Optional recommendations and contextual surfaces based on recent/long-term usage, routines, time of day, permitted location, device state, connected devices, calendar, weather, events, deliveries, travel, flights, navigation, media, files, and relevant GoreeCloud actions.
- Contextual intelligence must respect Privacy Shield, explicit user controls, authorization, sensitive-content visibility, and data-minimization boundaries.

### Feed, cards, notifications, folders, widgets, and dock

- Optional personalized information feed and configurable cards.
- Notification dots/badges/previews with privacy-aware visibility.
- Complete Home/drawer folder experiences, including smart/automatic organization where accepted.
- Complete widget library, resizing, preview, interactive/context-aware behavior, precise placement, privacy-aware content, and update controls.
- Multi-page/adaptive Dock behavior with optional search/widgets/suggestions and GoreeCloud actions.

### Adaptive layout and motion

- Per-page, portrait/landscape, foldable, multi-display, tablet, and desktop-style layouts where appropriate.
- Custom transitions and motion with reduced-motion, accessibility, and device-performance-aware behavior.

### Application management

- Launcher shortcuts for uninstall/application information, hide/rename/icon customization, folder/category organization, pinning, installed/recently installed/recently updated discovery, and integrated access to permission, storage, notification, privacy, and security controls.

### Backup, Sync, and configuration

- Versioned Launcher-layout backup/restore, multiple saved configurations, restoration of layouts/folders/dock/preferences/icons/widgets, compatible layout import/migration, supported cross-device preference/layout synchronization, device-specific overrides, configuration history, and safe reset/replacement recovery.

## GoreeCloud platform integration boundaries

Every integration below is a target requirement only to the extent applicable and implemented. Naming an integration does not establish acceptance.

### Glaze UI / Design Center

Glaze UI governs visual hierarchy, components, interaction, responsiveness, motion, accessibility, adaptive layouts, wallpaper-aware styling, and design-system acceptance. Launcher currently targets the Glaze UI 2.0 Stable baseline through an Adoption Candidate mapping; complete rendered/native/accessibility acceptance remains separate.

### Privacy Shield / Privacy Center

Privacy Shield governs personalization signals, usage-derived ranking, contextual recommendations, location-derived behavior, search/history exposure, sensitive-content visibility, privacy status, consent, and direct user control.

### Wardveil Security / Security Center

Wardveil governs applicable package trust, suspicious-application warnings, protected application access, security-state surfaces, risky cross-application actions, and protection of Launcher configuration/personalization data.

### Everkeep / Continuity Center

Everkeep governs accepted long-term preservation, backup/recovery, portability, device-transition continuity, restoration evidence, and preservation of selected Launcher configurations.

### GoreeCloud Identity / Identity Center

Identity governs user/profile identity, authentication/authorization, secure profile switching, managed application visibility, profile-specific personalization, credentials/sessions where introduced, and identity-aware continuity.

### GoreeCloud Mesh / Mesh Center

Mesh governs authenticated/authorized cross-device coordination, device awareness, handoff, nearby-device actions, connected-device cards, device-triggered layouts, cross-device suggestions, and coordinated Launcher state where implemented.

### GoreeCloud Drive

Target integration includes recent files/folders, Launcher search, pinned file/folder shortcuts/widgets, contextual document suggestions, and synchronized-content access.

### GoreeCloud Sync and Backups

Target integration includes supported preference/layout/folder/application-organization synchronization and automatic configuration/layout/widget/folder/preference recovery where accepted.

### GoreeCloud Location and Maps

Target integration includes explicitly enabled location-aware application suggestions, travel/destination information, nearby actions, commute/navigation suggestions, Maps shortcuts, and destination search.

### GoreeCloud Mail, Messenger, and Calendar

Target integration includes unread/message/event widgets and cards, communication shortcuts, search/actions, meeting/event shortcuts, and context-aware schedule information where authorized and accepted.

## Authority and privacy principles

- Android remains authoritative for installed applications, launchability, platform roles, and operating-system launcher capabilities.
- GoreeCloud workspace persistence must maintain one accepted placement authority at a time.
- Cross-device continuity must not create ambiguous writable workspace authorities.
- Personalization and recommendations must be transparent, optional/configurable where appropriate, and bounded by Privacy Shield.
- Sensitive content must not be exposed through cards, search, widgets, notification previews, or recommendations without the applicable privacy/authorization policy.
- Platform integration must be substantive. A badge, label, icon, or named menu entry is not sufficient evidence of integration.
- Current source remains offline-capable for core Home behavior and currently requests no Android `INTERNET` permission.

## Stable blockers

Stable qualification still requires, as applicable:

- complete intended workspace/user flows and recovery semantics;
- folder/widget/shortcut functionality required by the accepted release scope;
- mature cross-page placement editing and accessible alternatives;
- representative-device, rotation/posture, performance, physical-interaction, and accessibility acceptance;
- complete current Glaze UI application acceptance;
- accepted applicable Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, and GoreeCloud Mesh integrations;
- accepted Sync/Backup/continuity behavior for any release claiming those capabilities;
- Android process-death and schema-upgrade recovery evidence;
- signed distribution and upgrade/recovery validation; and
- release/production evidence supporting every capability represented as implemented.
