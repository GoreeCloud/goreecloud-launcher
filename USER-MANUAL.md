# GoreeCloud Launcher User Manual

## Current availability

GoreeCloud Launcher is a **Development** Android HOME application. It is not yet a signed production/Stable release. Current source provides a substantially rebuilt daily-launcher shell with a real Home surface, complete scoped launchable-app discovery, Apps, Launcher Settings, local placement controls, persisted presentation preferences, and the existing guarded terminal-Room multi-page Home foundation.

Features described under **Approved future product direction** are planned/target capabilities and are **not currently available** unless a current-behavior section explicitly says otherwise.

## Make GoreeCloud Launcher your Home app

After installing a Development build, open GoreeCloud Launcher and use the default-Home control when shown. Android remains the authority for which launcher is the default and presents the system chooser.

You can change the default launcher later through Android system settings. Exact labels vary by device and Android version.

## Home screen

The primary Home experience is now a launcher-style surface rather than an app-management screen. Android renders the device wallpaper behind the launcher window, and Home presents the persisted application grid and Dock over that surface without requesting wallpaper-storage privileges.

- Tap an app icon to launch it.
- Long-press a supported Home or Dock icon to manage its placement.
- Open **Apps** from the Home affordance to browse installed launchable applications.
- Open **Launcher settings** to change supported Home, Apps, icon, label, and appearance preferences.
- Favorites and Dock are seeded from installed launchable apps on first run when needed.
- The Dock is currently bounded to five items.

The launcher discovers launchable activities through Android `LauncherApps` across available profiles. The manifest uses a scoped `MAIN` + `LAUNCHER` package-visibility query so the launcher can enumerate launchable applications without requesting broad `QUERY_ALL_PACKAGES` access.

The primary Home page is still the protected Favorites compatibility representation used by the current Room-authority path. It remains HOME rank zero and is not yet a secondary spatial grid page.

## Apps

Open **Apps** from Home to browse the launchable application inventory exposed to the launcher. The Apps surface is separate from Home and Launcher Settings; Home page-management controls are not rendered over it.

Use the search field to filter locally by application label or package information. Search is local and does not require Internet access.

Long-press an app to open its current placement dialog. Depending on its state, you can add/remove it from Home or the Dock and use accessible earlier/later ordering controls.

## Launcher settings

The current Development settings surface is scrollable and persists supported presentation choices locally.

### Home screen grid

Current presets cover Home grids from 4 to 6 columns and 4 to 7 rows through the supported preset combinations in the UI. Changing the grid affects rendered Home density; it does not migrate the protected primary compatibility page into the secondary spatial-authority model.

### Apps screen

You can choose 4, 5, or 6 columns for the Apps grid.

### Icons and labels

You can choose Small, Medium, or Large icon presentation and turn app labels on or off. These settings apply to the rebuilt primary surface and are also used by the current secondary-page presentation where applicable.

### Appearance

The launcher supports persisted **System**, **Light**, and **Dark** appearance selection. The current repository targets the Glaze UI design-system baseline through its Adoption Candidate mapping; complete rendered/native/device acceptance remains separately gated.

## Multi-page Home navigation

When the guarded workspace has reached terminal Room authority, the Development build can expose authoritative HOME pages through a compact horizontal page selector.

Each page selector entry conveys page identity plus authoritative app/unsupported-item context through its accessibility semantics. The selected page is automatically brought into view as page selection/order/count changes.

### Page controls

The current selector can expose guarded controls to:

- **Add page**;
- move eligible secondary pages earlier or later without crossing the protected primary page; and
- **Delete empty page** when the selected secondary page is eligible.

The protected primary compatibility Home page remains first and cannot be moved later or deleted. A secondary page cannot be moved ahead of it. Page mutations continue through the authoritative Room mutation boundary; the switcher is not a second workspace source of truth.

### Apps on secondary pages

Secondary authoritative Room pages now render as ordinary icon grids rather than engineering/debug panels. Tap an icon to launch the app. Long-press a supported secondary-page icon to open its management dialog.

Current secondary management actions can request:

- move to another authoritative secondary Home page;
- move earlier/later to the nearest permitted free cell; and
- exact one-cell moves left/right/up/down.

These controls are intentionally behind long-press rather than permanently displayed under every icon.

The protected primary Favorites compatibility page is not offered as a secondary spatial source or destination. Primary-to-secondary and secondary-to-primary movement require a separately accepted primary-grid/compatibility migration.

Exact-cell requests fail closed if the target is occupied or outside the authoritative grid. Secondary spatial mutations also fail closed when authority/placement health is invalid or when the workspace changes during the transaction. Unsupported item types are reported rather than falsely rendered as applications.

If authoritative paged Room state is unavailable, Launcher does not fabricate secondary-page state.

## Search — current and planned boundary

The current accepted user-facing search is the local Apps search described above.

The approved product direction also includes a first-party **Launcher Unified Search** surface opened by a one-finger downward swipe on Home. That future surface is intended to orchestrate local results such as apps, actions, contacts (when enabled and permissioned), photos/media through scoped Android APIs, files/documents exposed through supported providers, first-party GoreeCloud app content, and other authorized sources, with GoreeCloud Search participating as an optional online/web provider.

That unified swipe-down experience and its broader device-content providers are **not implemented or accepted by this Development shell rebuild yet**. Local device content must not be uploaded to GoreeCloud Search merely to produce local results.

## Official Launcher identity

The current Development package still requires an approved product-specific GoreeCloud Launcher visual identity. A framework/default Android-style placeholder is not the official release identity. The approved artwork must be stored in the Launcher repository and supply traceable Android adaptive and monochrome/themed icon derivatives.

No official artwork is claimed by this Development build until a reviewed canonical Launcher identity is supplied and accepted.

## Privacy and network behavior

The current launcher has no Android `INTERNET` permission and core Home/App operation remains offline-capable.

- No broad `QUERY_ALL_PACKAGES` permission is used for launcher discovery.
- No wallpaper/storage permission is required to show the system wallpaper behind Home.
- Launcher presentation preferences remain local.
- Privacy Shield governs applicable privacy/user-control surfaces.
- Wardveil Security governs applicable security/trust surfaces.
- Everkeep governs accepted backup/restore, continuity, preservation, and portability.
- GoreeCloud Identity governs future account/profile-backed authorization where applicable.
- GoreeCloud Mesh governs authenticated/authorized cross-service and cross-device integration.
- Glaze UI governs interface/design-system conformance.

Naming a platform system does not mean every integration is currently implemented or accepted.

## Current limitations

Still incomplete or separately gated include mature cross-page drag/drop editing; primary compatibility-page grid migration and primary↔secondary spatial movement; folders; shortcuts; widgets/AppWidgetHost; complete icon/theme customization; broader configurable gestures including the approved swipe-down Unified Search interaction; device files/photos/contacts/document search providers; GoreeCloud Search provider integration; official Launcher identity artwork; full Glaze Theme Engine behavior; versioned backup/restore; cross-device continuity; complete platform-system integration acceptance; Android OS process-death/schema-upgrade recovery acceptance; representative physical-device default-HOME acceptance; signed release packaging; and Stable qualification.

# Approved future product direction — not currently available

The long-term Launcher product scope is substantially broader than the current Development build.

## Home and organization

Future Launcher releases are intended to support deeply customizable Home pages and grids, margins/padding, folders, shortcuts, widgets, multiple dock pages, page indicators, wallpaper behavior, precise placement, layout locking, overlapping supported elements, and adaptive layouts for different form factors.

## Application drawer

The intended Apps/application-drawer experience includes folders/tabs, categories, smart groups, suggested/recent/frequent applications, hiding, richer visual customization, and context-sensitive ordering in addition to local search.

## Launcher Unified Search and GoreeCloud Search

The approved direction includes one-finger swipe-down access to a Launcher-owned unified search surface spanning applications, application content, contacts, device/GoreeCloud settings, files/documents/screenshots/photos, shortcuts/actions, GoreeCloud services, connected devices, and supported first-party providers. GoreeCloud Search is an optional provider for web/current-information results rather than the authority for the private local device index.

## Appearance and gestures

The intended personalization surface includes icon packs, GoreeCloud/adaptive themed icons, icon shapes, wallpaper-derived palettes, custom colors/transparency, custom Home/Apps/folder/dock styling, richer gesture assignments, reduced-motion behavior, and high-contrast/accessibility preferences.

## Smart information and cards

Future optional contextual experiences may include application suggestions, calendar/weather/event/delivery/travel/flight/navigation/media/file/device information, privacy/security status, backup/sync state, and other GoreeCloud service cards. These surfaces must remain configurable and privacy-aware.

## Backup and continuity

The approved direction includes Launcher configuration/layout backup and restore, configuration history, device migration, supported Sync continuity, Everkeep preservation, and safe device-replacement recovery.

## GoreeCloud integration

The intended product can integrate, where implemented and authorized, with GoreeCloud Drive, Sync, Backups, Everkeep, Identity, Privacy Shield, Wardveil Security, Mesh, Location, Mail, Messenger, Maps, Calendar, Search, Glaze UI, and other compatible GoreeCloud services.

Personalization and contextual intelligence should remain transparent and user-controlled. Privacy, security, identity, continuity, and cross-device features require substantive implementation and acceptance rather than being inferred from names or visuals.

Refer to `README.md`, `SPECIFICATIONS.md`, `FEATURES.md`, `BENEFITS.md`, `COMPETITIVE-OBJECTIVES.md`, and the `docs/` directory for scope, implementation state, architecture, and acceptance details.
