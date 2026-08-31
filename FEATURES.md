# GoreeCloud Launcher Features

## Status and interpretation

GoreeCloud Launcher is in **Development**. This file distinguishes current implemented Development behavior from the approved product capability scope.

A capability listed under **Approved product scope** is a product requirement or target capability. It is **not** an implementation, runtime-acceptance, release-acceptance, or Stable claim unless it is also listed under the implemented/current sections and supported by the repository's validation evidence.

## Implemented in Development source

Current source includes:

- Native Android launcher application foundations using Kotlin and Jetpack Compose.
- Android HOME-role onboarding and lifecycle-aware default-HOME state.
- Android `LauncherApps` launchable-application discovery and profile-aware stable workspace-key mapping.
- Local All apps search by application label/package information.
- Locally persisted Favorites and a bounded Dock.
- Explicit non-drag placement controls and Reorder-mode Favorites/Dock drag ordering.
- Room-backed authoritative workspace cutover/read foundations.
- Multi-page Home projection from authoritative Room state.
- Home page selection, creation, guarded reordering, and guarded deletion of eligible empty secondary pages.
- Scrollable/lazy Home page selector with authoritative app/unsupported-item context.
- Automatic focus/scroll of the selected Home page after selection, reorder, or page-count changes.
- App launch from rendered Home pages.
- App movement between authoritative Home pages.
- Within-page nearest-free-cell earlier/later movement.
- Guarded exact one-cell movement left/right/up/down that fails closed on occupied or out-of-bounds targets.
- Development presentation of unsupported workspace-item counts instead of silently hiding their presence.
- Persisted System / Light / Dark appearance selection.
- Repository-level Glaze UI 2.0 Adoption Candidate mapping and validation guard.
- Privacy/HOME/Room/schema/lint/test/debug-build validation in CI, with Android 16 emulator runtime coverage for the currently exercised Development paths.

## Development / acceptance work still required

Important incomplete or separately gated work includes:

- Production Room-authority cutover/recovery acceptance for the complete intended workspace experience.
- Mature cross-page drag/drop and direct live cell/span editing.
- Primary-page-to-secondary-page rendered item movement.
- Populated-page deletion with recovery/undo semantics.
- Complete folders, shortcuts, widgets/AppWidgetHost, folder/widget editing, and richer workspace editing.
- Complete icon/label customization and broader gesture bindings.
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

## GoreeCloud Search

- Persistent home-screen search and application-drawer search.
- Installed application and application-content search.
- Contact search.
- Device-settings and GoreeCloud-settings search.
- File, document, and screenshot search.
- Shortcut and action search.
- Web search.
- Application-store and GoreeCloud-service search.
- Search across compatible connected GoreeCloud devices.
- Recently accessed content suggestions.
- Recently used, usage-based, and routine-based application recommendations.
- Context-aware results with user-configurable result categories.
- Search-bar customization, themes, shortcuts, and animations.
- Direct actions from search results.

## Icons and Visual Appearance

- Icon-pack support.
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
- Configurable swipe-up and swipe-down actions.
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
- Search Drive from Launcher search.
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
- User control over personalization, location, and usage-derived signals.
- Sensitive-content visibility controls.
- Privacy-status surfaces and direct Privacy Center access.

## Wardveil Security

- Security-aware launcher experiences and security-state indicators.
- Protected application access where supported.
- Suspicious-application warnings where supported.
- Direct Security Center access.
- Security-sensitive contextual actions.
- Protection of Launcher configuration/personalization data.

## GoreeCloud Mesh

- Compatible-device awareness.
- Cross-device application handoff and nearby-device actions.
- Connected-device contextual cards and device-triggered layouts.
- Cross-device content suggestions.
- Coordinated Launcher state across supported endpoints.

## GoreeCloud Location

- Location-aware application suggestions when explicitly enabled.
- Destination/travel information and location-sensitive cards.
- Nearby-action suggestions and GoreeCloud Maps shortcuts.
- User-controlled location personalization.

## GoreeCloud Mail

- Unread-mail widgets and important-message cards.
- Mail search from GoreeCloud Search.
- Contact communication shortcuts and contextual email actions.

## GoreeCloud Messenger

- Recent-conversation and contact communication shortcuts.
- Unread-message indicators.
- Conversation widgets and contextual messaging actions.

## GoreeCloud Maps

- Destination shortcuts, commute information, upcoming-trip information, and navigation suggestions.
- Location-aware cards and destination search from Launcher.

## GoreeCloud Calendar

- Upcoming-event cards, Calendar widgets, meeting shortcuts, event-based application suggestions, and context-aware schedule information.

## Glaze UI

Launcher is a native Glaze UI experience. The intended product scope includes consistent GoreeCloud visual language, adaptive layouts across supported device classes, dynamic materials/interface depth, responsive motion, wallpaper-aware appearance, unified accessibility behavior, consistent components, device-appropriate interaction patterns, platform theme integration, and seamless visual continuity with other GoreeCloud applications.

## Core product principle

GoreeCloud Launcher is intended to be more than an application grid. It is the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together in one adaptive interface.

Launcher intelligence and personalization should remain transparent and user-controlled.
