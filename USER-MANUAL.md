# GoreeCloud Launcher User Manual

## Current availability

GoreeCloud Launcher is a **Development** Android HOME application. It is not yet a signed production/Stable release. Current source provides a substantially rebuilt daily-launcher shell with a real Home surface, complete scoped launchable-app discovery, Apps, Launcher Settings, local placement controls, persisted presentation preferences, GoreeCloud Index invocation, Home layout locking, configurable Index Home entry, and the guarded terminal-Room multi-page Home foundation.

Features described under **Approved future product direction** are planned/target capabilities and are **not currently available** unless a current-behavior section explicitly says otherwise.

## Make GoreeCloud Launcher your Home app

After installing a Development build, open GoreeCloud Launcher and use the default-Home control when shown. Android remains the authority for which launcher is the default and presents the system chooser.

You can change the default launcher later through Android system settings. Exact labels vary by device and Android version.

## Home screen

The primary Home experience is a launcher-style surface. Android renders the device wallpaper behind the launcher window, and Home presents the persisted application grid and Dock over that surface without requesting wallpaper-storage privileges.

- Tap an app icon to launch it.
- Long-press a supported Home or Dock icon to manage its placement.
- Open **Apps** from the Home affordance to browse installed launchable applications.
- Open **Launcher settings** to change supported Home, Apps, icon, label, appearance, layout-lock, and GoreeCloud Index entry preferences.
- Swipe one finger downward through the unobstructed Home search zone to open GoreeCloud Index universal search.
- Favorites and Dock are seeded from installed launchable apps on first run when needed.
- The Dock is currently bounded to five items.

The launcher discovers launchable activities through Android `LauncherApps` across available profiles. The manifest uses a scoped `MAIN` + `LAUNCHER` package-visibility query plus a bounded GoreeCloud Index search-action query, without requesting broad `QUERY_ALL_PACKAGES` access.

The primary Home page is still the protected Favorites compatibility representation used by the current Room-authority path. It remains HOME rank zero and is not yet a secondary spatial grid page.

## GoreeCloud Index from Home

**GoreeCloud Index** is the GoreeCloud universal search and indexing authority. Launcher provides Home entry points into Index; Launcher does not maintain a second universal index or cross-provider ranking engine.

The current Launcher Development integration uses the explicit `com.goreecloud.index.action.SEARCH` handoff. If a compatible GoreeCloud Index activity is not available, Launcher reports that Index is not installed rather than silently switching to a different universal-search engine.

Launcher Settings provides two Home-entry modes:

### Permanent on Home

This is the compatibility-preserving default. Home shows the **Search GoreeCloud** affordance, and the one-finger downward gesture also opens Index.

### Swipe down only

The persistent Search GoreeCloud affordance is hidden. The one-finger downward Home gesture remains available and opens the same GoreeCloud Index experience.

Changing this preference changes only the Launcher entry presentation. GoreeCloud Index still owns universal query/provider/index/ranking behavior in both modes.

The current Index Android foundation searches installed applications. Files, contacts, calendar, media, Drive, connected-device, extension, optional third-party, and Internet/Web provider coverage remains separately gated unless later accepted Index work says otherwise. GoreeCloud Search is intended to provide Internet/Web/current-information results through Index rather than becoming local-index authority.

## Home layout lock

Launcher Settings includes **Lock Home screen layout**.

When the lock is enabled, current placement-changing operations are blocked for the item/page types that Launcher currently implements. This includes current Favorite and Dock membership/order changes, Home-page creation/deletion/reordering, moving supported applications between secondary Home pages, and current within-secondary-page movement controls.

The following normal actions remain available while Home is locked:

- launching applications;
- selecting Home pages;
- opening Apps;
- opening Launcher Settings;
- invoking GoreeCloud Index; and
- changing non-placement presentation preferences.

If you long-press an app while locked, the placement dialog can still open so it can explain the locked state, but its current placement-changing controls are disabled.

### Unlock from Settings

Open **Launcher settings → Home screen → Lock Home screen layout** and turn the switch off. This is the deterministic non-gesture unlock path.

### Unlock by holding on Home

When the layout is locked, Home shows a **Layout locked** control. Press and continuously hold that control for **5 seconds**. Launcher shows progress while you hold. Releasing before the five seconds completes cancels the unlock. Completing the hold turns the persisted layout lock off.

The five-second interaction remains subject to representative physical-device and accessibility acceptance before release/Stable qualification. Future folders, shortcuts, widgets, and other placeable item types must join the same lock policy when those features are implemented; they are not current runtime behavior merely because the target scope mentions them.

## Apps

Open **Apps** from Home to browse the launchable application inventory exposed to the launcher. The Apps surface is separate from Home and Launcher Settings; Home page-management controls are not rendered over it.

Use the **Search apps** field to filter locally by application label or package information. This is an Apps-navigation filter, not GoreeCloud Index universal search. It is local and does not require Internet access.

Long-press an app to open its current placement dialog. When the Home layout is unlocked, you can add/remove it from Home or the Dock and use accessible earlier/later ordering controls. When the layout is locked, the dialog explains the lock and disables those current placement changes.

## Launcher settings

The current Development settings surface is scrollable and persists supported choices locally.

### Home screen grid

Current presets cover Home grids from 4 to 6 columns and 4 to 7 rows through the supported preset combinations in the UI. Changing the grid affects rendered Home density; it does not migrate the protected primary compatibility page into the secondary spatial-authority model.

The Home screen settings card also contains the **Lock Home screen layout** switch described above.

### GoreeCloud Index

Choose **Permanent on Home** or **Swipe down only**. Swipe-down invocation remains active in both modes; Permanent mode additionally keeps the Search GoreeCloud affordance visible.

### Apps screen

You can choose 4, 5, or 6 columns for the Apps grid.

### Icons and labels

You can choose Small, Medium, or Large icon presentation and turn app labels on or off. These settings apply to the rebuilt primary surface and are also used by the current secondary-page presentation where applicable.

A full Theme Manager, third-party icon-pack selection, icon masking, and richer optical icon normalization are approved future capabilities and are not implemented by this Development slice.

### Appearance

The launcher supports persisted **System**, **Light**, and **Dark** appearance selection. Launcher retains evidence-backed Glaze UI Adoption Candidate mapping. The approved current product target is Glaze UI 2.1.0 Stable, but complete rendered/native/accessibility/device acceptance against that release remains separately gated.

## Multi-page Home navigation

When the guarded workspace has reached terminal Room authority, the Development build can expose authoritative HOME pages through a compact horizontal page selector.

Each page selector entry conveys page identity plus authoritative app/unsupported-item context through its accessibility semantics. The selected page is automatically brought into view as page selection/order/count changes.

### Page controls

When Home layout is unlocked, the current selector can expose guarded controls to:

- **Add page**;
- move eligible secondary pages earlier or later without crossing the protected primary page; and
- **Delete empty page** when the selected secondary page is eligible.

The protected primary compatibility Home page remains first and cannot be moved later or deleted. A secondary page cannot be moved ahead of it. Page mutations continue through the authoritative Room mutation boundary; the switcher is not a second workspace source of truth.

When Home layout is locked, Launcher blocks current page mutation callbacks. Page selection remains available because it does not modify placement.

### Apps on secondary pages

Secondary authoritative Room pages render as ordinary icon grids. Tap an icon to launch the app. Long-press a supported secondary-page icon to open its management dialog.

When Home layout is unlocked, current secondary management actions can request:

- move to another authoritative secondary Home page;
- move earlier/later to the nearest permitted free cell; and
- exact one-cell moves left/right/up/down.

These controls are intentionally behind long-press rather than permanently displayed under every icon. Current mutation callbacks are blocked while layout lock is enabled.

The protected primary Favorites compatibility page is not offered as a secondary spatial source or destination. Primary-to-secondary and secondary-to-primary movement require a separately accepted primary-grid/compatibility migration.

Exact-cell requests fail closed if the target is occupied or outside the authoritative grid. Secondary spatial mutations also fail closed when authority/placement health is invalid or when the workspace changes during the transaction. Unsupported item types are reported rather than falsely rendered as applications.

If authoritative paged Room state is unavailable, Launcher does not fabricate secondary-page state.

## Official Launcher identity and artwork

All canonical GoreeCloud Launcher logos, icons, symbols, illustrations, and artwork are maintained in **`GoreeCloud/goreecloud-branding-assets`**. The canonical Launcher source is `products/launcher/app-icon.svg`.

The Launcher repository is only a consumer. It contains traceable Android adaptive/round/monochrome derivatives plus provenance metadata required to build the Development APK. A consumer derivative is not an independent canonical artwork source.

The current derivatives have source/build/runtime validation, but that does not by itself establish production visual-identity acceptance. Representative icon-mask, themed-icon, small-size, system-chooser, device, and release review remain separate gates.

## Privacy and network behavior

The current launcher has no Android `INTERNET` permission and core Home/App operation remains offline-capable.

- No broad `QUERY_ALL_PACKAGES` permission is used for launcher discovery.
- The GoreeCloud Index handoff uses bounded package/action visibility rather than broad inventory access.
- No wallpaper/storage permission is required to show the system wallpaper behind Home.
- Launcher presentation, layout-lock, and Index-entry preferences remain local.
- GoreeCloud Index owns universal provider/query/ranking behavior and must preserve source/authorization/privacy boundaries.
- Privacy Shield governs applicable privacy/user-control surfaces.
- Wardveil Security governs applicable security/trust surfaces.
- Everkeep governs accepted backup/restore, continuity, preservation, and portability.
- GoreeCloud Identity governs account/profile-backed authorization where applicable.
- GoreeCloud Mesh governs authenticated/authorized cross-service and cross-device integration.
- Glaze UI governs interface/design-system conformance.

Naming a platform system does not mean every integration is currently implemented or accepted.

## Current limitations

Still incomplete or separately gated include mature cross-page drag/drop editing; primary compatibility-page grid migration and primary↔secondary spatial movement; folders; shortcuts; widgets/AppWidgetHost; complete Theme Manager/icon-pack/masking behavior; broader configurable gestures; broader GoreeCloud Index providers for device/GoreeCloud/third-party content; GoreeCloud Search provider integration in Index; fully polished Glaze UI 2.1 Index presentation and complete Launcher Glaze UI 2.1 acceptance; layout-lock coverage for future placeable item types plus representative-device five-second-hold acceptance; production visual-identity acceptance; full Glaze Theme Engine behavior; versioned backup/restore; cross-device continuity; complete platform-system integration acceptance; Android OS process-death/schema-upgrade recovery acceptance; representative physical-device default-HOME acceptance; signed release packaging; and Stable qualification.

# Approved future product direction — not currently available

The long-term Launcher product scope is substantially broader than the current Development build.

## Home and organization

Future Launcher releases are intended to support deeply customizable Home pages and grids, margins/padding, folders, shortcuts, widgets, multiple dock pages, page indicators, wallpaper behavior, precise placement, lock enforcement across all supported placeable item types, overlapping supported elements, and adaptive layouts for different form factors.

## Application drawer

The intended Apps/application-drawer experience includes folders/tabs, categories, smart groups, suggested/recent/frequent applications, hiding, richer visual customization, and context-sensitive ordering in addition to the current local Apps filter.

## GoreeCloud Index and GoreeCloud Search

The approved direction expands the current Launcher-to-Index handoff into a complete universal search experience spanning applications, application content, contacts, device/GoreeCloud settings, files/documents/screenshots/photos, shortcuts/actions, GoreeCloud services, connected devices, extensions, optional third-party services, and other supported providers. GoreeCloud Index remains the universal query/provider/index/ranking authority. GoreeCloud Search is an optional provider for Internet/Web/current-information results rather than the authority for the private local device index.

The result experience should be original GoreeCloud design work governed by the latest Stable Glaze UI contract, with premium visual hierarchy, immediate focus, grouped/provenance-aware results, restrained depth/translucency, fluid bounded motion, and first-class reduced-motion/reduced-transparency/accessibility behavior.

## Appearance and gestures

The intended personalization surface includes a native Theme Manager, icon packs, icon masking, bounded icon scaling/normalization, GoreeCloud/adaptive themed icons, icon shapes, wallpaper-derived palettes, custom colors/transparency, custom Home/Apps/folder/dock styling, richer gesture assignments, reduced-motion behavior, and high-contrast/accessibility preferences.

## Smart information and cards

Future optional contextual experiences may include application suggestions, calendar/weather/event/delivery/travel/flight/navigation/media/file/device information, privacy/security status, backup/sync state, and other GoreeCloud service cards. These surfaces must remain configurable and privacy-aware.

## Backup and continuity

The approved direction includes explicit **Backup Launcher configuration** and **Restore Launcher configuration** actions using a versioned, validated local/offline-capable format, configuration history, safe widget rebinding/reconfiguration, device migration, supported Sync continuity, Everkeep preservation, and safe device-replacement recovery. These actions are not implemented in the current Development source.

## GoreeCloud integration

The intended product can integrate, where implemented and authorized, with GoreeCloud Drive, Sync, Backups, Everkeep, Identity, Privacy Shield, Wardveil Security, Mesh, Location, Mail, Messenger, Maps, Calendar, Search, Index, Glaze UI, and other compatible GoreeCloud services.

Personalization and contextual intelligence should remain transparent and user-controlled. Privacy, security, identity, continuity, and cross-device features require substantive implementation and acceptance rather than being inferred from names or visuals.

Refer to `README.md`, `SPECIFICATIONS.md`, `FEATURES.md`, `BENEFITS.md`, `COMPETITIVE-OBJECTIVES.md`, and the `docs/` directory for scope, implementation state, architecture, and acceptance details.