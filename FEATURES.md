# GoreeCloud Launcher Features

## Status and interpretation

GoreeCloud Launcher is in **Development**. This file distinguishes current implemented Development behavior from the approved product capability scope.

A capability listed under **Approved product scope** is a product requirement or target capability. It is **not** an implementation, runtime-acceptance, release-acceptance, or Stable claim unless it is also listed under the implemented/current sections and supported by repository evidence.

## Implemented in Development source

Current source includes:

- Native Android launcher foundations using Kotlin and Jetpack Compose.
- Android HOME-role onboarding and lifecycle-aware default-HOME state.
- Scoped Android `MAIN` + `LAUNCHER` package visibility rather than broad `QUERY_ALL_PACKAGES` access.
- `LauncherApps` discovery across available profiles, package/profile refresh callbacks, and launchable-activity deduplication.
- A rebuilt wallpaper-backed primary Home surface with application grid, Dock, Apps affordance, and Launcher Settings affordance.
- Native Android system-wallpaper presentation behind the launcher window without wallpaper/storage privileges.
- A separate Apps surface with local label/package search and application launching.
- A separate scrollable Launcher Settings surface.
- Persisted Home grid presets covering supported 4–6 column / 4–7 row combinations.
- Persisted Apps-grid density of 4, 5, or 6 columns.
- Persisted Small/Medium/Large icon presentation and app-label visibility.
- Persisted System / Light / Dark appearance selection.
- Locally persisted Favorites and a bounded five-item Dock.
- Long-press app placement management with accessible earlier/later controls.
- Room-backed authoritative workspace cutover/read foundations.
- Multi-page Home projection from authoritative Room state.
- Home page selection, creation, guarded secondary-page reordering, and guarded deletion of eligible empty secondary pages while the protected primary compatibility page remains rank zero.
- Compact/lazy Home page selector with authoritative accessibility context.
- App launch from rendered secondary Home pages.
- Secondary Home pages rendered as ordinary icon grids instead of engineering panels.
- Secondary app movement controls hidden behind long-press management rather than permanently displayed under each app.
- App movement between authoritative secondary Home pages.
- Within-secondary-page nearest-free-cell earlier/later movement.
- Guarded exact one-cell movement left/right/up/down that fails closed on occupied or out-of-bounds targets.
- Protection of the canonical primary Favorites compatibility page from secondary spatial moves and page-rank changes until a separate primary-grid migration is accepted.
- Development presentation of unsupported workspace-item counts instead of silently hiding their presence.
- Repository-level Glaze UI Adoption Candidate mapping and validation guard.
- Privacy/HOME/Room/schema/lint/test/debug-build validation in CI, with Android 16 emulator runtime coverage for the exercised Development paths.

## Explicitly approved next capability — Launcher Unified Search

The approved interaction contract defines a **one-finger swipe downward on an unobstructed Home-screen area** as the default direct gesture for opening **Launcher Unified Search**.

Launcher Unified Search is a first-party local-first orchestration surface. Planned providers include installed apps, app shortcuts/actions, Launcher/device settings, contacts when explicitly enabled and permissioned, scoped photos/screenshots/media, files/documents exposed through supported Android providers, explicit first-party GoreeCloud application search contracts, authorized GoreeCloud Drive content, and compatible connected-device sources.

GoreeCloud Search participates as an optional first-party online provider for web/current-information categories. It is not the authority for the private local device index. Local files, photos, contacts, app inventory, Launcher history, and local result payloads must not be uploaded merely to obtain local results.

This swipe-down unified search and its broader providers are **approved but not implemented/accepted by the current beta-shell rebuild**.

## Official product identity requirement

GoreeCloud Launcher requires a unique first-party product-specific icon/logo/artwork. The current generic Android/framework-style placeholder is not the approved official release identity.

Canonical artwork must live in this repository and produce traceable Android adaptive foreground/background resources, a monochrome/themed-icon derivative, and other required platform derivatives while retaining one recognizable cross-platform identity. Generated/unreviewed, upstream, framework-default, or generic corporate-logo substitutes do not qualify as the official Launcher identity.

No approved canonical Launcher artwork is currently committed, so product-identity acceptance remains incomplete.

## Development / acceptance work still required

Important incomplete or separately gated work includes:

- One-finger swipe-down Launcher Unified Search and its provider architecture.
- Contacts, scoped media/photos/screenshots, documents/files, first-party searchable-content, GoreeCloud Drive, connected-device, and GoreeCloud Search provider implementation/acceptance.
- Approved official Launcher artwork and repository-local derivative pipeline.
- Production Room-authority cutover/recovery acceptance for the complete intended workspace experience.
- Mature cross-page drag/drop and direct live cell/span editing.
- Primary compatibility-page grid migration and primary-to-secondary/secondary-to-primary spatial item movement.
- Populated-page deletion with recovery/undo semantics.
- Complete folders, shortcuts, widgets/AppWidgetHost, folder/widget editing, and richer workspace editing.
- Complete icon/theme customization and broader gesture bindings.
- Complete first-party Glaze Theme Engine behavior.
- Full Glaze UI rendered/native/accessibility acceptance across representative phones, tablets, foldables, desktop-style modes, and other supported surfaces.
- Complete Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Mesh, GoreeCloud Identity, Sync, Backup, Location, Search, Drive, Mail, Messenger, Maps, Calendar, and other applicable platform integrations.
- Android OS process-death and schema-upgrade recovery acceptance.
- Representative physical-device default-HOME and interaction acceptance.
- Signed release packaging/distribution and Stable qualification.

# Approved product scope

The following capability inventory is the approved Launcher product direction. These are target capabilities unless separately evidenced as implemented.

## Home Screen

- Multiple customizable home-screen pages.
- Adjustable home-screen grid sizes with independent row and column configuration.
- Adjustable application icon sizes.
- Drag-and-drop application placement and widget placement.
- Precise sub-grid positioning.
- Custom screen margins and padding.
- Application folders with custom names and intelligent folder-name suggestions.
- Home-screen application shortcuts and contextual shortcuts.
- Resizable widgets and a searchable widget picker.
- Favorite-application dock with customizable layouts, multiple dock pages, and scrolling.
- Home-screen page scrolling with optional infinite scrolling.
- Wallpaper scrolling and page indicators.
- Automatic creation of additional home-screen pages where appropriate.
- Remove applications from Home without uninstalling them.
- Lock the home-screen layout.
- Hide application labels and adjust label size/positioning.
- Overlapping widgets and supported interface elements.
- Adaptive layouts based on device size, posture, orientation, and display configuration.

## Application Drawer

- Swipe-up application drawer.
- Alphabetically organized application library.
- Independent/custom application-drawer grids.
- Application-drawer folders and tabs.
- Automatic categorization, intelligent groups, and smart folders.
- Suggested, frequently used, recently installed, and recently used applications.
- Application-drawer search.
- Hide applications from the drawer.
- Custom drawer organization, backgrounds, transparency, and vertical scrolling.
- Context-sensitive application ordering.

## Launcher Unified Search and GoreeCloud Search

- Default one-finger swipe-down access from an unobstructed Home area.
- A first-party Launcher-owned search overlay/sheet with immediate query focus.
- Offline-capable local results independent of GoreeCloud Search availability.
- Installed application and application-content search.
- App shortcuts and direct actions.
- Launcher settings/actions and supported device-setting search.
- Contact search when explicitly enabled and permissioned.
- File and document search through supported Android document/provider access.
- Photo, screenshot, video, and supported media search through scoped Android media access.
- First-party GoreeCloud application searchable-content providers.
- Authorized GoreeCloud Drive search.
- Web/current-information results through optional GoreeCloud Search provider integration.
- Application-store and GoreeCloud-service search.
- Search across compatible connected GoreeCloud devices when authorized.
- Recently accessed content suggestions.
- Recently used, usage-based, routine-based, and contextual local recommendations where enabled.
- Context-aware results with user-configurable result categories.
- Search-bar customization, themes, shortcuts, and animations.
- Direct actions from search results.
- Local history that is disableable and independently clearable.
- Per-source privacy/permission controls for sensitive categories.
- Clear/rebuild controls for local index state.
- No sponsored, promoted, affiliate, or advertising ranking.

## Icons and Visual Appearance

- Unique official GoreeCloud Launcher product identity with repository-local canonical artwork.
- Android adaptive foreground/background icon resources derived from the canonical identity.
- Monochrome/themed Launcher icon derivative.
- Icon-pack support for other applications.
- GoreeCloud-native themed icons and adaptive themed icons.
- Custom icon shapes and sizes.
- Individual application-icon customization and custom folder icons.
- Wallpaper-based icon styling and wallpaper-derived palettes.
- Automatic interface color extraction.
- Custom interface and accent colors.
- Light, Dark, and automatic appearance switching.
- Transparent interface elements.
- Custom folder, dock, drawer, and Home appearance.
- Adaptive visual themes and custom text styling.
- Dynamic interface styling based on device context.

## Gestures and Interaction

- Custom swipe, horizontal swipe, double-tap, pinch, and two-finger gestures.
- Swipe down opens Launcher Unified Search by default on an unobstructed Home area.
- Configurable swipe-up and other gesture actions without removing accessible non-gesture alternatives.
- Custom gesture assignments.
- Gesture-based application, shortcut, GoreeCloud-action, search, notification, and feed access.
- Double-tap screen locking where supported.
- Long-press contextual menus and application shortcuts.
- Drag-and-drop organization and application grouping.
- Configurable interaction sensitivity.

## Smart and Contextual Experiences

- Context-aware Home and drawer application suggestions.
- Pinnable suggested applications and per-application suggestion controls.
- Recent-usage, long-term usage, routine, and time-of-day recommendations.
- Location-aware recommendations when permitted.
- Device-state-aware and connected-device-aware recommendations.
- Context-sensitive launcher content.
- Calendar, weather, upcoming-event, package-delivery, travel, flight, navigation, and destination information.
- Media recommendations and controls.
- Relevant file/document suggestions.
- Contextual information cards and personalized information surfaces.
- Suggested GoreeCloud actions.
- Adaptive launcher layouts based on context.

## GoreeCloud Feed and Cards

- Optional side-mounted personalized information feed.
- Customizable information cards.
- Application, contact, widget, media, calendar, weather, travel, and device-status cards.
- Conditional, pinnable, and context-triggered cards.
- Connected-device-triggered cards and launcher layouts.
- Launcher content recommendations and GoreeCloud service cards.
- Privacy/security status cards.
- Backup/synchronization status cards.
- Cross-device activity cards.
- User-configurable card ordering and visibility.

## Notifications

- Notification dots and numeric/custom notification badges.
- Unread-status indicators.
- Custom badge appearance and application-specific badge controls.
- Long-press notification previews and notification access from application icons.
- Notification-aware contextual shortcuts.
- Privacy-aware notification visibility.

## Folders

- Home-screen and application-drawer folders.
- Automatic and smart folders.
- Custom/suggested folder names and custom folder icons.
- Custom folder grids, backgrounds, transparency, and opening animations.
- Folder swipe actions and organization controls.
- Adaptive folder layouts.
- Automatic and manual application grouping.
- Nested organization where supported.

## Widgets

- Home-screen widgets with resizing, previews, grouping, padding controls, and precise positioning.
- Searchable widget library.
- Overlapping widgets where supported.
- Information, dynamic, interactive, and context-aware widgets.
- GoreeCloud service widgets.
- Smart widget recommendations and adaptive widget layouts.
- Cross-device information widgets.
- Privacy-aware widget content.
- Widget refresh/update controls.

## Dock

- Custom dock size and adjustable application count.
- Multiple dock pages and scrolling.
- Dock background, transparency, and padding customization.
- Search placement within the dock.
- Dock widgets.
- Optional dock removal.
- Suggested/context-aware dock applications.
- Pinned GoreeCloud actions.
- Adaptive dock layouts.

## Layout Customization

- Custom row/column counts.
- Independent Home, application-drawer, and folder grids.
- Icon-label positioning, size adjustment, and visibility controls.
- Custom screen margins and interface padding.
- Sub-grid and precise widget positioning.
- Overlapping supported interface elements.
- Home-layout locking.
- Per-page customization.
- Different layouts for device modes, portrait/landscape, foldables, multi-display devices, tablets, and desktop-style launcher experiences where appropriate.

## Navigation and Animation

- Custom Home page transitions, drawer animations, folder animations, and search animations.
- Smooth scrolling and spring-based motion.
- Gesture-responsive and wallpaper transition effects.
- Application-launch/return and contextual transition animations.
- Reduced-motion behavior.
- Accessibility-conscious animation behavior.
- Device-performance-aware animation scaling.

## Application Management

- Application uninstall and information shortcuts.
- Hide applications from the drawer.
- Rename application display labels and replace individual icons.
- Organize applications into folders/categories and pin applications.
- Search installed applications and quickly access recently installed/updated apps.
- Integrated access to application permissions, storage, notification, privacy, and security controls.
- Application-specific launcher actions.

## Personalization

- Wallpaper integration and dynamic color extraction.
- Automatic color matching and custom accent colors.
- Adaptive themes, icon styling, text styling, search appearance, drawer styling, folder styling, and dock styling.
- Custom animation preferences.
- Per-device and per-display personalization.
- Context-based and time-based appearance changes.
- Wallpaper-driven launcher styling.
- Accessibility personalization, reduced-motion preferences, and high-contrast options.

## Backup, Sync, and Configuration

- Launcher-layout backup and restore.
- Multiple saved launcher configurations.
- Restore Home layouts, folders, dock configuration, preferences, icon customization, and widget placement.
- Import compatible launcher layouts.
- Migrate launcher layouts between devices.
- Synchronize supported Launcher preferences and compatible layouts across GoreeCloud devices.
- Device-specific configuration overrides.
- Configuration version history where supported.
- Safe recovery after device reset or replacement.

# GoreeCloud platform integration scope

Launcher is intended to act as a unified entry point into compatible GoreeCloud applications, services, devices, and system capabilities. Integration must remain substantive, permission-aware, privacy-aware, security-aware, and subject to each participating service's implementation and authorization boundaries.

## GoreeCloud Drive

- Surface recently accessed files/folders.
- Search Drive from Launcher Unified Search when authorized.
- Pin files/folders to Home.
- File/folder shortcut widgets.
- Contextual document recommendations.
- Quick access to synchronized content.

## GoreeCloud Sync

- Synchronize supported Launcher settings.
- Synchronize compatible Home layouts, folders, application organization, and personalization preferences.
- Maintain continuity across GoreeCloud devices.

## GoreeCloud Backups

- Automatic Launcher-configuration backup.
- Home-layout, widget, folder, and preference restoration.
- Device-replacement restoration workflows.

## Everkeep

- Long-term preservation of selected Launcher configurations.
- Recovery of important personalization state.
- Configuration portability and device-transition continuity.
- Integration with continuity/recovery experiences.

## GoreeCloud Identity

- Identity-aware personalization and user-specific Home configurations.
- Secure profile switching.
- Managed application visibility.
- Profile-specific recommendations.
- Identity-aware cross-device continuity.

## Privacy Shield

- Privacy-aware search results and contextual recommendations.
- Per-provider controls for permissioned/sensitive Launcher Unified Search categories.
- User control over personalization, location, and usage-derived signals.
- Sensitive-content visibility controls.
- Privacy-status surfaces and direct Privacy Center access.

## Wardveil Security

- Security-aware launcher experiences and security-state indicators.
- Protected application access where supported.
- Suspicious-application warnings where supported.
- Direct Security Center access.
- Security-sensitive contextual actions.
- Protection of Launcher configuration/personalization and local-search index state.

## GoreeCloud Mesh

- Compatible-device awareness.
- Cross-device application handoff and nearby-device actions.
- Connected-device contextual cards and device-triggered layouts.
- Cross-device content suggestions and authorized connected-device search.
- Coordinated Launcher state across supported endpoints.

## GoreeCloud Location

- Location-aware application suggestions when explicitly enabled.
- Destination/travel information and location-sensitive cards.
- Nearby-action suggestions and GoreeCloud Maps shortcuts.
- User-controlled location personalization.

## GoreeCloud Mail

- Unread-mail widgets and important-message cards.
- Mail results through explicit first-party search integration where implemented and authorized.
- Contact communication shortcuts and contextual email actions.

## GoreeCloud Messenger

- Recent-conversation and contact communication shortcuts.
- Unread-message indicators.
- Conversation widgets and contextual messaging actions.
- Searchable first-party conversation/content contracts where implemented and authorized.

## GoreeCloud Maps

- Destination shortcuts, commute information, upcoming-trip information, and navigation suggestions.
- Location-aware cards and destination search from Launcher.

## GoreeCloud Calendar

- Upcoming-event cards, Calendar widgets, meeting shortcuts, event-based application suggestions, context-aware schedule information, and explicit searchable event integration where implemented.

## GoreeCloud Search

- Optional online/web/current-information provider for Launcher Unified Search.
- Provider handoff/integration that preserves Launcher local-first/offline operation.
- No authority over the Launcher private local device index.
- No implicit upload of local files, photos, contacts, app inventory, local history, or local result payloads.

## Glaze UI

Launcher is a native Glaze UI experience. The intended product scope includes consistent GoreeCloud visual language, adaptive layouts across supported device classes, dynamic materials/interface depth, responsive motion, wallpaper-aware appearance, unified accessibility behavior, consistent components, device-appropriate interaction patterns, platform theme integration, and seamless visual continuity with other GoreeCloud applications.

## Core product principle

GoreeCloud Launcher is intended to be more than an application grid. It is the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together in one adaptive interface.

Launcher intelligence and personalization should remain transparent and user-controlled.
