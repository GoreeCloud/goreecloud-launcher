# GoreeCloud Launcher User Manual

## Current availability

GoreeCloud Launcher is a **Development** Android HOME application. It is not yet a signed production/Stable release. Current source supports daily-launcher foundations including HOME-role onboarding, app discovery, Favorites, Dock, All apps search, local placement/reorder controls, and Light/Dark/System appearance behavior.

## Make GoreeCloud Launcher your Home app

After installing a Development build, open GoreeCloud Launcher and use its Home-role onboarding control to request Android's default HOME role. Android remains the authority for which launcher is the default.

You can change the default launcher later through Android's system settings. Exact labels vary by device and Android version.

## Home screen

The current Home experience shows locally persisted app Favorites and a Dock.

- Tap an app tile to launch it.
- Long-press a supported tile to open local placement controls.
- Favorites and Dock are seeded from installed launchable apps on first run when needed.
- The Dock is currently bounded to five items.

The launcher discovers launchable applications through Android's `LauncherApps` APIs and keeps profile-aware application identities distinct.

## All apps

Open **All apps** to browse launchable applications available to the launcher.

Use the local search field to filter by application label or package information. Search is local; the current launcher does not require Internet access for core app discovery or launch.

## Add or move apps

Long-press an app from supported Home/All apps surfaces to access placement actions.

Current controls include explicit move-earlier/move-later operations and Reorder mode for Favorites/Dock. The non-drag move controls remain important for keyboard, switch-access, and users who prefer not to rely on gestures.

### Reorder mode

When Reorder mode is active:

- ordinary tile launching/long-press behavior is suppressed for the editable surface;
- the dragged tile receives edit-state feedback;
- valid targets are indicated by the current native UI; and
- the final accepted move is committed through the launcher's workspace persistence boundary.

Source/emulator validation does not equal complete physical-device drag acceptance. Touch targeting, rotation, TalkBack, switch access, and representative-device behavior remain separate acceptance work.

## Appearance

The current launcher supports persisted **System**, **Light**, and **Dark** appearance selection and targets the current Glaze UI 2.0 design-system baseline through an Adoption Candidate source mapping.

Complete rendered Glaze UI 2.0 acceptance, all accessibility configurations, and representative physical-device design acceptance remain incomplete unless a later acceptance record states otherwise.

## Multi-page workspace status

The repository now contains deterministic multi-page workspace mutation contracts and Room-backed page-order and cross-page item-persistence foundations for the guarded terminal Room-authority path.

These are not yet a claim that ordinary users can manage a fully rendered multi-page grid from the production Home UI. Rendered page creation/navigation, live cell/span editing, broader drag/drop controls, folders, shortcuts, widgets, and production Room cutover/routing remain separate milestones.

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

Still incomplete or separately gated include production Room authority cutover/routing, rendered multi-page grid management, folders, shortcuts, widgets/AppWidgetHost, complete icon/label customization, broader gesture bindings, full Glaze Theme Engine behavior, versioned backup/restore, process-death acceptance, physical-device default-HOME acceptance, signed release packaging, and Stable qualification.

Refer to `README.md`, `SPECIFICATIONS.md`, and the `docs/` directory for implementation and acceptance details.
