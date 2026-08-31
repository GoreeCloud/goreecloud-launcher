# GoreeCloud Launcher User Manual

## Current availability

GoreeCloud Launcher is a **Development** Android HOME application. It is not yet a signed production/Stable release. Current source supports daily-launcher foundations including HOME-role onboarding, app discovery, Favorites, Dock, All apps search, local placement/reorder controls, Light/Dark/System appearance behavior, and a guarded terminal-Room multi-page Home surface.

Features described under **Approved future product direction** are planned/target capabilities and are **not currently available** unless a later current-behavior section or acceptance record explicitly says otherwise.

## Make GoreeCloud Launcher your Home app

After installing a Development build, open GoreeCloud Launcher and use its Home-role onboarding control to request Android's default HOME role. Android remains the authority for which launcher is the default.

You can change the default launcher later through Android's system settings. Exact labels vary by device and Android version.

## Home screen

The primary Home experience shows locally persisted app Favorites and a Dock.

- Tap an app tile to launch it.
- Long-press a supported primary-Home tile to open local placement controls.
- Favorites and Dock are seeded from installed launchable apps on first run when needed.
- The Dock is currently bounded to five items.

The launcher discovers launchable applications through Android's `LauncherApps` APIs and keeps profile-aware application identities distinct.

## Multi-page Home navigation

When the guarded workspace has reached terminal Room authority, the Development build can expose authoritative HOME pages through a horizontal page selector.

Each page selector entry shows:

- the page number;
- the authoritative number of app items on that page; and
- the count of unsupported workspace items when present, so those items are not silently hidden from page context.

The selector is a lazy horizontal list. When you select a page, reorder pages, or change the page count, Launcher automatically scrolls the selected page back into view instead of leaving the active context off-screen.

### Page controls

The current selector can expose guarded controls to:

- **Add page**;
- **Move earlier**;
- **Move later**; and
- **Delete empty page** when the selected secondary page is eligible.

The primary legacy-import Home page is not deleted by the empty-secondary-page control. Page mutations continue through the existing authoritative workspace mutation boundary; the switcher itself is not a second workspace source of truth.

### Apps on secondary pages

Application items on authoritative Room pages can be launched. Current secondary-page controls also support:

- move to another authoritative Home page;
- move earlier/later to the nearest permitted free cell; and
- exact one-cell moves left/right/up/down.

Exact-cell requests fail closed if the target cell is occupied or outside the authoritative grid. Unsupported item types are reported rather than rendered falsely as apps.

If authoritative paged Room state is unavailable, Launcher does not fabricate secondary-page state and remains on the safe primary Home path.

This remains a Development workspace surface. Mature drag/drop page editing, folders, shortcuts, widgets, and complete release acceptance remain separate milestones.

## All apps

Open **All apps** to browse launchable applications available to the launcher.

Use the local search field to filter by application label or package information. Search is local; the current launcher does not require Internet access for core app discovery or launch.

## Add or move apps

Long-press an app from supported primary Home/All apps surfaces to access placement actions.

Current controls include explicit move-earlier/move-later operations and Reorder mode for Favorites/Dock. The non-drag move controls remain important for keyboard, switch-access, and users who prefer not to rely on gestures.

### Reorder mode

When Reorder mode is active:

- ordinary tile launching/long-press behavior is suppressed for the editable primary surface;
- the dragged tile receives edit-state feedback;
- valid targets are indicated by the current native UI; and
- the final accepted move is committed through the launcher's workspace persistence boundary.

Source/emulator validation does not equal complete physical-device drag acceptance. Touch targeting, rotation, TalkBack, switch access, and representative-device behavior remain separate acceptance work.

## Appearance

The current launcher supports persisted **System**, **Light**, and **Dark** appearance selection and targets the current Glaze UI 2.0 design-system baseline through an Adoption Candidate source mapping.

Complete rendered Glaze UI 2.0 acceptance, all accessibility configurations, and representative physical-device design acceptance remain incomplete unless a later acceptance record states otherwise.

## Workspace persistence status

The repository contains deterministic multi-page workspace mutation contracts and Room-backed page-order, app-placement, cross-page movement, and guarded exact-cell movement foundations for the terminal Room-authority path. The rendered page selector and secondary-page surface read from that terminal Room authority; they do not bypass it.

Broader drag/drop controls, folders, shortcuts, widgets, complete live cell/span editing, and complete release acceptance remain separate milestones.

## Privacy and network behavior

The current launcher has no Android `INTERNET` permission and core operation is intended to remain offline-capable.

- Privacy Shield governs applicable privacy/user-control surfaces.
- Wardveil Security governs applicable security/trust surfaces.
- Everkeep governs accepted backup/restore, continuity, preservation, and portability.
- GoreeCloud Identity governs any future account-backed authorization.
- GoreeCloud Mesh governs authenticated/authorized cross-service and cross-device integration.
- Glaze UI governs interface/design-system conformance.

Naming these platform systems does not mean every integration is currently implemented or accepted.

The current launcher does not require a GoreeCloud account for core Home operation.

## Current limitations

Still incomplete or separately gated include mature cross-page drag/drop editing, folders, shortcuts, widgets/AppWidgetHost, complete icon/label customization, broader gesture bindings, full Glaze Theme Engine behavior, unified GoreeCloud Search/context/feed capabilities, versioned backup/restore, cross-device continuity, complete applicable platform-system integration acceptance, process-death/schema-upgrade recovery acceptance, representative physical-device default-HOME acceptance, signed release packaging, and Stable qualification.

# Approved future product direction — not currently available

The long-term Launcher product scope is substantially broader than the current Development build. The following areas are approved targets, not instructions for features that can be used today.

## Home and organization

Future Launcher releases are intended to support deeply customizable Home pages and grids, icon/label sizing, margins/padding, folders, shortcuts, widgets, multiple dock pages, page indicators, wallpaper behavior, precise placement, layout locking, overlapping supported elements, and adaptive layouts for different form factors.

## Application drawer

The intended application drawer includes custom grids, folders/tabs, categories, smart groups, suggested/recent/frequent applications, hiding, visual customization, and context-sensitive ordering in addition to search.

## GoreeCloud Search

The approved direction includes search across applications, application content, contacts, device/GoreeCloud settings, files/documents/screenshots, shortcuts/actions, web results, application-store/services, and compatible connected devices with direct actions and configurable result categories.

## Appearance and gestures

The intended personalization surface includes icon packs, GoreeCloud/adaptive themed icons, icon shapes, wallpaper-derived palettes, custom colors, transparency, custom Home/drawer/folder/dock styling, custom text styling, richer gesture assignments, reduced-motion behavior, and high-contrast/accessibility preferences.

## Smart information and cards

Future optional contextual experiences may include application suggestions, calendar/weather/event/delivery/travel/flight/navigation/media/file/device information, privacy/security status, backup/sync state, and other GoreeCloud service cards. These surfaces must remain configurable and privacy-aware.

## Backup and continuity

The approved direction includes Launcher configuration/layout backup and restore, configuration history, device migration, supported Sync continuity, Everkeep preservation, and safe device-replacement recovery.

## GoreeCloud integration

The intended product can integrate, where implemented and authorized, with GoreeCloud Drive, Sync, Backups, Everkeep, Identity, Privacy Shield, Wardveil Security, Mesh, Location, Mail, Messenger, Maps, Calendar, Search, Glaze UI, and other compatible GoreeCloud services.

Personalization and contextual intelligence should remain transparent and user-controlled. Privacy, security, identity, continuity, and cross-device features must not be inferred from a name or visual surface alone; each requires substantive implementation and acceptance.

Refer to `README.md`, `SPECIFICATIONS.md`, `FEATURES.md`, `BENEFITS.md`, `COMPETITIVE-OBJECTIVES.md`, and the `docs/` directory for product scope, implementation state, architecture, and acceptance details.
