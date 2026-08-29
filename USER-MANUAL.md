# GoreeCloud Launcher User Manual

## Current availability

GoreeCloud Launcher is a **Development** Android HOME application. It is not yet a signed production/Stable release. Current source supports daily-launcher foundations including HOME-role onboarding, app discovery, Favorites, Dock, All apps search, local placement/reorder controls, Light/Dark/System appearance behavior, and an initial terminal-Room multi-page navigation surface.

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

When the guarded workspace has reached terminal Room authority and more than one HOME page exists, the current Development build can expose a page selector and render application items from those authoritative Room pages.

The primary page continues to use the existing Favorites/Dock management experience. Secondary Room pages are currently **read-only for placement**:

- you can select a secondary page;
- application items on that page can be launched;
- unsupported workspace item types are reported rather than silently rendered as apps; and
- placement editing, cross-page drag/drop, page creation/deletion, and page reordering are not yet exposed from this surface.

If authoritative paged Room state is unavailable, Launcher does not fabricate secondary-page state and remains on the primary Home surface.

This feature is a Development rendering bridge. It does not establish production Room cutover or a complete multi-page workspace editor.

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

The repository contains deterministic multi-page workspace mutation contracts and Room-backed page-order and cross-page item-persistence foundations for the guarded terminal Room-authority path. The rendered page selector/secondary-page surface reads from that terminal Room authority; it does not bypass it.

Secondary-page editing, live cell/span controls, broader drag/drop controls, folders, shortcuts, widgets, and production Room cutover/routing remain separate milestones.

## Privacy and network behavior

The current launcher has no Android `INTERNET` permission and core operation is intended to remain offline-capable.

- Privacy Shield governs privacy/user-control surfaces.
- Wardveil Security governs applicable security/trust surfaces.
- Everkeep governs accepted backup/restore, continuity, preservation, and portability.
- GoreeCloud Identity governs any future account-backed authorization.
- GoreeCloud Mesh governs authenticated cross-service integration.
- Glaze UI governs interface/design-system conformance.

The current launcher does not require a GoreeCloud account for core Home operation.

## Current limitations

Still incomplete or separately gated include production Room authority cutover/routing, user-facing page creation/deletion/reordering, secondary-page placement editing, live cell/span editing, cross-page drag/drop UI, folders, shortcuts, widgets/AppWidgetHost, complete icon/label customization, broader gesture bindings, full Glaze Theme Engine behavior, versioned backup/restore, process-death acceptance, physical-device default-HOME acceptance, signed release packaging, and Stable qualification.

Refer to `README.md`, `SPECIFICATIONS.md`, and the `docs/` directory for implementation and acceptance details.
