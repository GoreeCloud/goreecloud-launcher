# GoreeCloud Launcher Specifications

## Status

**Active Development. Not Stable. Production/release acceptance is incomplete.**

GoreeCloud Launcher is a first-party Android launcher surface. New behavior must preserve Android platform authority for installed activities and the repository's terminal Room workspace authority after cutover.

## Current architecture

- Native Android / Jetpack Compose application.
- Workspace persistence and authoritative placement are represented through the repository's Room-backed workspace model after cutover.
- The rendered paged Home projection is read from authoritative Room page/item state; the UI does not maintain a second workspace source of truth.
- Installed launchable application identity comes from Android `LauncherActivityInfo` and the repository's stable workspace-key mapping.

## Current Home-page behavior

- Multiple authoritative Home pages can be rendered and selected.
- The page switcher displays authoritative app counts and otherwise unsupported item counts.
- The page selector uses a lazy horizontal list and automatically scrolls the currently selected page into view after selection, reorder, or page-count changes.
- Page create/reorder/delete requests use existing guarded mutation paths. The primary legacy-import Home page cannot be deleted through the empty-secondary-page control.
- App movement supports existing page moves, nearest-free-cell earlier/later movement, and guarded exact one-cell movement.
- Exact-cell movement fails closed when the target is outside the authoritative grid or occupied.

## Platform boundaries

- **Glaze UI / Design Center:** governs visual hierarchy, targets, accessibility, responsiveness, and rendered acceptance.
- **Privacy Shield:** governs any future launcher personalization, usage-derived ranking, search/history, or cross-app context that could expose user behavior.
- **Wardveil Security:** governs applicable package/security trust and risky cross-application operations where introduced.
- **Everkeep:** governs accepted workspace continuity, restore, portability, and recovery behavior.
- Android remains authoritative for installed applications, launchability, profiles, and platform launcher capabilities.

## Stable blockers

Stable qualification still requires complete workspace/user flows, accessibility and representative-device acceptance, folder/widget/shortcut presentation as applicable, signed distribution, upgrade/recovery validation, and accepted GoreeCloud platform integrations where required.
