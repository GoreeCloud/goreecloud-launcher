# GoreeCloud Launcher — Experience Expansion

**Status:** Proposed capability expansion with multiple implementation tranches in Development  
**Date:** September 13, 2026  
**Design system:** Glaze UI  
**Search authority:** GoreeCloud Index remains the canonical universal indexing, provider, ranking, and cross-domain search authority  
**Security:** Wardveil Security  
**Privacy:** Privacy Shield  
**Continuity:** Everkeep

## Purpose

This document captures the next GoreeCloud Launcher experience expansion inspired by mature Android launcher interaction patterns, including the user-provided Samsung/One UI reference images. The references are design and workflow inspiration only. GoreeCloud Launcher must reinterpret useful interaction patterns through its own Glaze UI identity, architecture, privacy model, security boundaries, and local-first behavior rather than reproducing another launcher.

All capabilities below are **planned/proposed unless explicitly marked as implemented in the current Development branch and backed by repository evidence**. Development implementation does not imply Release Candidate or Stable acceptance.

## Product direction

GoreeCloud Launcher should become a highly polished, configurable, local-first Android HOME experience that combines fast app access with deep organization, strong one-handed ergonomics, accessible customization, native GoreeCloud service integration, and explicit user control.

The Launcher should remain visually calm by default while making advanced controls available when the user asks for them. It must avoid behavioral advertising, profiling, or opaque cloud personalization.

## 1. Home screen

Planned capabilities include:

- Multi-page home screens with page overview, page reordering, page creation, safe empty-page deletion, and explicit default-page selection.
- Configurable home grids, icon sizing, icon spacing, page padding, row/column density, and label behavior.
- Persistent or optional favorites dock with configurable capacity and spacing.
- Large, medium, compact, and minimal weather/date/clock compositions using GoreeCloud-native widget surfaces where available.
- Configurable GoreeCloud Index search affordance placement, including permanent pill, swipe-only entry, and future top/bottom placement choices.
- Wallpaper-responsive Glaze surfaces with adaptive contrast and user-controlled blur/transparency.
- Optional minimal home mode that can suppress nonessential chrome.
- Per-page wallpaper and appearance presets if supported without destabilizing Android wallpaper behavior.
- Page-specific widgets, folders, collections, and shortcuts.
- Home-page overview/edit mode with thumbnails and explicit page-management controls.
- One-handed reachability options for search, drawer entry, and edit controls.

## 2. App drawer

Planned drawer modes:

- Grid.
- List.
- Compact grid.
- Category/tab view.
- Search-first view.
- Optional paged grid presentation after gesture/transition validation.

Planned controls include:

- Configurable column count, icon size, spacing, density, and label behavior.
- Top or bottom search placement.
- Alphabetical ordering.
- Explicit custom ordering where the storage model safely supports it.
- Recent and frequently used views only if implemented locally, transparently, and without behavioral tracking.
- Category and folder organization.
- Smart folders that require explicit user acceptance before persistent reorganization.
- Hidden-app controls and future private-space/work-profile filtering.
- Alphabetical fast-scroll/jump index.
- Page cleanup and empty-page removal for paged mode.
- Search that always preserves local installed-app results even when GoreeCloud Index is unavailable.

### Current Development implementation tranches

The first bounded implementation tranche adds persistent **Grid** and **List** drawer presentation modes. The existing grid remains the fail-safe default. List mode reuses the same Android-provided application inventory, launch path, long-press management path, local search filtering, icon cache, and icon scaling controls.

Draft PR #96 exact head `b4d95027cb9fda979919f3caa21b819f6c7595c3` passed Android CI run `34735922867` attempt 2, including both the normal validation job and the Android 16 Room/runtime job. That evidence verifies the exact Development source under automation but does not replace representative physical-device/default-HOME acceptance.

The second stacked Development tranche on `feat/drawer-navigation-controls-20260913` adds:

- Persistent **A→Z** and **Z→A** local installed-app ordering with a fail-safe A→Z default.
- Case-insensitive label comparison with package/class tie-breakers for deterministic local ordering.
- Persistent **Top** or **Bottom** drawer-search placement with a fail-safe Top default.
- An alphabetical fast-navigation rail for both Grid and List drawer layouts.
- Fast-navigation sections derived only from sections actually present in the local app inventory, using `#` for non-A–Z starts.
- Section controls sized as accessible touch targets and hidden while a search query is filtering results.
- Shared local search, app launch, long-press management, icon rendering, and optional `Search all GoreeCloud` handoff behavior regardless of layout/sort/search-position choices.

Drawer layout, sort order, and search-position preferences are intentionally persisted outside the strict v1 portable backup/recovery subset. This prevents presentation-only changes from silently changing the existing seven-field recovery contract. A future versioned portable snapshot format may add these settings explicitly.

The new navigation-controls tranche remains Development work until exact-head CI is green. It does not claim Release Candidate, Stable, production approval, or physical-device acceptance.

## 3. Search and discovery

GoreeCloud Launcher must not become a second universal search authority.

Launcher responsibilities:

- Fast local installed-app filtering.
- Local launcher settings/actions discovery where appropriate.
- Safe handoff to GoreeCloud Index for broader results.
- Explicit degraded behavior when Index is unavailable.
- User controls for whether an app may appear in launcher search or suggestions.

GoreeCloud Index responsibilities remain universal indexing, cross-domain provider orchestration, ranking, and broader GoreeCloud search authority. GoreeCloud Search remains the web/current-information provider through the Index architecture where applicable.

Future Launcher search capabilities may include local settings shortcuts, folders, widgets, contacts, files, calendar items, messages, browser history, and actions **only through the appropriate authoritative provider and permission boundary** rather than by duplicating those stores inside Launcher.

## 4. Folders, collections, and organization

Planned capabilities include:

- Standard folders on Home and in the app drawer.
- Configurable folder grids and expanded/full-screen folder modes.
- Manual sort, alphabetical sort, and safe custom ordering.
- Folder search.
- Folder-specific icon/label presentation.
- Smart folders using local signals with explicit user confirmation.
- Categories, tags, favorites, and user-defined collections.
- Locked/private folders backed by applicable platform privacy/security authority rather than cosmetic hiding.
- Bulk folder creation from multi-select edit mode.

## 5. Widgets and Glaze Cards

Planned capabilities include:

- Android `AppWidgetHost` placement, configuration, resizing, restoration, and provider lifecycle handling.
- Searchable widget gallery with live or representative previews.
- Widget size guidance and grid-aware placement.
- Widget stacks with manual ordering as the initial authority.
- Optional local smart rotation only after privacy, power, and predictability review.
- GoreeCloud-native Glaze Cards for Weather, Calendar, Memos, Media, Device Status, Search, Privacy Shield, Wardveil Security, and other first-party services.
- Widget failure containment so a bad provider does not destabilize the whole launcher.
- Safe-mode handling for repeatedly crashing widgets/providers.

## 6. Long-press and context actions

Planned app actions include:

- App info.
- Add/remove from Home.
- Add/remove from Dock.
- Reorder/move actions.
- Pinned/dynamic shortcuts.
- Widgets.
- Uninstall or disable when Android policy permits.
- Share app information where appropriate.
- Select multiple items.
- Create folder or collection.
- Hide from drawer/search/suggestions.
- Privacy Shield access explanation and applicable data-surface controls.
- Wardveil-backed security status or warnings where authoritative evidence exists.

Context menus must never imply security/privacy protection that the underlying authority has not accepted or proven.

## 7. Edit mode and layout management

Planned capabilities include:

- Multi-select.
- Group drag/move between pages.
- Automatic alignment and optional gap cleanup.
- Page cleanup and safe removal of empty pages.
- Page reorder.
- Bulk folder creation.
- Undo for destructive layout operations.
- Short local layout-history window or transactional snapshots before high-impact edits.
- Layout lock with an intentional unlock path.
- Deterministic rollback for interrupted or failed layout mutations.

## 8. Personalization

Planned controls include:

- Independent Home, Drawer, Folder, and Dock density controls.
- Icon scale and future icon shape/mask/normalization controls.
- Optional icon-pack support subject to compatibility and security review.
- Per-app icon and label overrides.
- Label visibility and future label-size controls.
- Folder style, dock style, drawer style, corner radius, surface elevation, blur, and transparency controls within Glaze UI authority.
- Wallpaper-derived palettes with accessible contrast guarantees.
- Reduced motion and reduced transparency.
- Animation intensity and transition preference controls.
- Touch target scaling and large-text support.

## 9. Launcher Profiles and Spaces

Planned Launcher Profiles may include:

- Personal.
- Work.
- Travel.
- Focus.
- Minimal.
- User-defined profiles.

Profiles may capture selected launcher presentation and organization preferences, but switching must be explicit and predictable. Any automatic/contextual switching must be local-first, opt-in, explainable, revocable, and must not become behavioral tracking.

A future **Glaze Shelf** may provide a user-curated strip or surface for pinned actions, media controls, device controls, temporary tasks, and compact first-party cards without forcing permanent home-screen placement.

## 10. Work profiles, private spaces, and multiple users

Planned capabilities include:

- Correct Android work-profile application enumeration and state handling.
- Clear visual distinction between personal/work applications.
- Profile-aware search and filtering.
- Respect for paused work profiles.
- Private-space behavior aligned with supported Android platform contracts.
- No cross-profile leakage through search, suggestions, widgets, previews, or backup.
- Multi-user correctness where the Android launcher APIs expose supported behavior.

## 11. Accessibility and alternative input

Release-quality requirements include:

- TalkBack semantics and meaningful control labels.
- Switch Access compatibility.
- Keyboard and D-pad navigation.
- Predictable focus order.
- Large text without clipping or inaccessible overflow.
- Touch targets that meet accessibility requirements.
- High-contrast behavior.
- Reduced motion and reduced transparency.
- One-handed reachability.
- Accessible reordering/edit actions that do not require drag-only interaction.

## 12. Devices and form factors

Planned adaptive behavior includes:

- Phones.
- Tablets.
- Foldables in folded and unfolded postures.
- Landscape orientation where supported.
- External displays where Android HOME behavior and project scope permit it.
- Posture-specific or width-class-specific grids rather than blindly stretching phone layouts.

## 13. Performance and power

Launcher must remain responsive because it is part of the primary Android interaction loop.

Planned controls and acceptance work include:

- Bounded icon caching and preloading.
- Lazy rendering for drawer modes.
- Reduced recomposition and bitmap churn.
- Measured drawer/home transition latency.
- Startup and HOME-return latency budgets.
- Reduced-blur/power-saving presentation mode.
- Memory-pressure recovery.
- Widget/provider isolation.
- Physical-device profiling across representative low-, mid-, and high-resource devices.

## 14. Privacy Shield integration

Planned integration includes:

- Per-app controls for appearance in search, suggestions, smart folders, and contextual surfaces.
- Clear purpose and scope when launcher data is shared with another GoreeCloud authority.
- Local-first processing for launcher organization and personalization.
- No behavioral advertising or sale of launcher activity.
- No silent cloud synchronization of launcher usage history.
- Fail-closed handling when an operation requires privacy authorization that is absent or stale.
- “Why this suggestion?” / “Why this result?” explanations for future intelligent surfaces where applicable.

## 15. Wardveil Security integration

Planned integration includes:

- Security evidence surfaced only when Wardveil has authoritative, current evidence.
- Warnings for applications or shortcuts that Wardveil identifies as risky, subject to user-understandable explanation.
- Protection-state explanations rather than a misleading global “safe” badge.
- Safe handling of untrusted shortcut/widget metadata.
- Recovery paths for launcher crashes caused by third-party providers.

## 16. Everkeep and recovery

Planned capabilities include:

- Versioned launcher layout export/import.
- Everkeep integration for accepted backup/restore flows.
- Profile-aware restoration.
- Safe handling when an app/widget/provider is missing on the target device.
- Deterministic restore with explicit conflict behavior.
- Recovery snapshots before high-impact migrations.
- Schema-versioned restoration rather than silently interpreting incompatible state.

## 17. Additional capabilities to evaluate

The following are candidate capabilities and remain proposed until separately accepted:

- Home-screen stacks for related first-party Glaze Cards.
- Temporary task/workflow spaces.
- App pairs or multi-window launch shortcuts on supported devices.
- Local “recently installed” grouping derived from authoritative package data.
- Local device-state shortcuts such as Bluetooth, flashlight, hotspot, or Do Not Disturb only through supported Android APIs and explicit permission/policy boundaries.
- Universal deep-link shortcuts registered by trusted applications.
- Search-driven launcher settings through GoreeCloud Index action providers.
- Glaze quick-actions surface for frequently invoked explicit actions.
- Import assistance from another launcher when Android/platform APIs and user consent make this reliable and lawful.

## 18. Non-goals and constraints

- Do not clone Samsung One UI Home or any other launcher.
- Do not transfer GoreeCloud Index universal-search authority into Launcher.
- Do not add behavioral advertising or opaque usage profiling.
- Do not claim Wardveil or Privacy Shield protection without accepted runtime evidence.
- Do not silently reorder the user’s layout based on inferred behavior.
- Do not broaden the portable backup schema without an explicit versioned migration.
- Do not call a Development implementation Release Candidate or Stable without required validation and release evidence.

## Implementation sequence

The recommended sequence is:

1. Finish and validate persistent Grid/List drawer presentation modes. **Source implemented; exact-head automated Development validation is green on PR #96. Physical-device/default-HOME acceptance remains open.**
2. Add drawer sorting and fast alphabetical navigation while preserving deterministic, case-insensitive local ordering. **Source implementation is active on `feat/drawer-navigation-controls-20260913`; exact-head CI remains required.**
3. Add bottom/top search placement and one-handed drawer ergonomics. **Top/bottom search placement source implementation is active in the same tranche; broader reachability/transition tuning remains planned.**
4. Introduce explicit edit-mode/page-overview workflows.
5. Add folders and shortcut support on top of stable workspace authority.
6. Add Android widget hosting, resizing, and recovery.
7. Add deeper Glaze personalization and responsive form-factor layouts.
8. Add Launcher Profiles, Smart Spaces, and local intelligent surfaces only after the core remains stable under physical-device acceptance.
9. Complete Privacy Shield, Wardveil, Everkeep, accessibility, performance, provenance, and release-gate evidence before RC/Stable claims.

## Verification boundary

Grid/List exact-head CI is green on Draft PR #96. The stacked drawer-navigation-controls tranche must independently pass the same exact-head validation and Android 16 runtime suite after its final documentation/code head is established. Representative Android physical-device/default-HOME acceptance, accessibility review, and performance evaluation remain required before the expanded drawer experience can be treated as release-ready. All other features in this document remain planned unless separately verified.
