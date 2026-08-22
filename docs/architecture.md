# Architecture

GoreeCloud Launcher is Android-native because its role depends on HOME-role integration, `LauncherApps`, shortcuts, widgets, wallpaper/system behavior, profiles, and launcher lifecycle callbacks.

The repository remains a single Android application module during the early milestones so launcher behavior stays easy to inspect while product boundaries stabilize. Planned long-term module boundaries are `core:launcher`, `core:data`, `core:widgets`, `core:theme`, `core:designsystem`, `feature:home`, `feature:drawer`, `feature:search`, `feature:settings`, `feature:onboarding`, and `feature:backup`.

The first-party Glaze Theme Engine begins with local semantic colors and persistent System/Light/Dark appearance. Later milestones add wallpaper palettes, user accents, icon masks, themed icons, dock/folder/drawer surfaces, motion controls, and exportable presets.

## Current source state

Milestone 0 establishes the Android HOME contract, app/profile discovery, Glaze UI shell, local appearance persistence, and privacy/manifest guards.

Milestone 1 currently adds five daily-launcher foundations:

1. Local application search by installed-app label or package name.
2. Durable workspace preferences for ordered Favorites and Dock membership.
3. Explicit, accessible Favorite and Dock ordering controls with deterministic move-earlier/move-later operations.
4. An explicit Home Reorder mode that supports direct drag/drop ordering for Favorites and Dock while preserving the non-drag controls as a permanent accessibility path.
5. A staged Room 3 relational workspace foundation that mirrors the accepted DataStore state into a versioned page/item schema without prematurely replacing the live launcher source of truth.

## Current DataStore workspace authority

Android Preferences DataStore remains authoritative for the live Home Favorites and Dock during the relational-transition stage. Each stored application key combines a public `UserHandle` discriminator with the flattened component name. This avoids treating identical package names across profiles as one launcher item without depending on a hidden Android profile-identifier API.

The first successful app inventory initializes up to twelve Favorites and four Dock items, excluding the launcher itself. After initialization, user choices are authoritative: removing every Favorite or Dock entry does not cause the defaults to be silently re-created.

Favorites and Dock items resolve against the live `LauncherApps` inventory at render time. Missing or uninstalled components therefore disappear safely from the rendered workspace without requiring a launch attempt. A later relational cleanup/cutover layer can prune stale keys after runtime migration acceptance exists.

The Dock is currently capped at five items. `WorkspaceCodec.moved` repositions one existing key by one logical step, clamps movement at collection boundaries, and leaves unknown keys unchanged. `WorkspaceCodec.movedToTarget` is the direct-drag primitive: it moves an existing key toward the live target key while ignoring self-drops and unknown keys. Both paths are covered by local JVM unit tests.

App management is available by long-press from All apps, Favorites, and Dock. The management dialog exposes membership state plus Move earlier / Move later controls. Home also provides an explicit Reorder mode. Entering Reorder mode disables ordinary launch/long-press interactions on Home and Dock tiles, enables direct pointer dragging, visually lifts the dragged tile, highlights the current valid drop target, and commits a drop through the same `WorkspaceRepository` ordering source of truth. Leaving Reorder mode restores ordinary launcher interaction.

This separation is intentional. Normal mode prioritizes reliable app launching and management; Reorder mode makes movement explicit so minor pointer motion cannot accidentally rearrange the Home screen. Move earlier / Move later remains the keyboard-, switch-, and non-drag-friendly alternative rather than being replaced by gesture-only behavior.

## Room 3 relational workspace transition

The relational foundation uses AndroidX Room 3.0.1 with Kotlin Symbol Processing and AndroidX SQLite 2.7.0 through `AndroidSQLiteDriver`. Room 3 is Kotlin/coroutine-first and gives the launcher compile-time SQL verification plus explicit schema-version evolution for the richer workspace model.

`LauncherDatabase` starts at schema version 1 and contains:

- `workspace_pages` — stable page identity, container type, and page rank. The initial compatibility mirror creates `home:0` and `dock:0`; the model is intentionally capable of representing later multiple Home and Dock pages.
- `workspace_items` — stable item identity, parent page, item type, app key when applicable, deterministic rank, optional cell coordinates, and spans. The item type vocabulary reserves APP, SHORTCUT, FOLDER, and WIDGET without claiming that shortcut/folder/widget behavior is implemented yet.

`WorkspaceLegacyImportMapper` deterministically maps the current ordered Favorite and Dock keys into the two version-1 pages. The same app may legitimately appear in both Home and Dock as two distinct placement records. Duplicate keys within one legacy container are collapsed defensively while rank order is preserved.

`WorkspaceDao.replaceLegacySnapshot` transactionally replaces only the two compatibility pages and their cascading item rows. `WorkspaceRelationalMirror` runs after an initialized DataStore state is observed and mirrors every subsequent Favorite/Dock state change. This makes the operation idempotent and keeps the relational copy current while DataStore remains authoritative.

The mirror is intentionally fail-safe during this transition: ordinary database exceptions do not replace or corrupt the accepted DataStore launcher path. Coroutine cancellation is rethrown and never swallowed. A later cutover must add visible diagnostics/acceptance evidence and prove relational read/write behavior before Room becomes authoritative.

This dual-store period is temporary. It exists to preserve user-selected Favorites and Dock ordering while the richer relational path is built and tested. The final migration must remove ambiguous dual authority: Room becomes authoritative for workspace placement only after runtime migration acceptance, while DataStore remains appropriate for independent small preferences such as appearance and accessibility settings.

## Schema and migration governance

The Room Gradle plugin is configured with `app/schemas` as the exported schema directory. Schema history is intended to become source-controlled migration evidence as schema versions advance. Schema-version changes must not use destructive fallback for ordinary upgrades; they require an explicit migration or accepted auto-migration path plus migration tests before release acceptance.

The current version-1 Room schema is a new local database and therefore has no prior Room schema to migrate from. The relevant compatibility transition in this stage is from the existing DataStore Favorite/Dock representation into the Room version-1 relational representation. That compatibility mirror is not equivalent to declaring the Room cutover complete.

## Glaze UI native mapping

The launcher intentionally targets **Glaze UI 1.4 Stable** as its current design-system baseline. The canonical `GoreeCloud/glaze-ui` repository was reviewed before this slice. Its adoption guidance requires semantic mapping before visuals, native platform controls when they provide stronger behavior, practical touch targets, local presentation, and application-specific acceptance rather than assuming design-system stability equals application conformance.

`GlazeMetrics.kt` begins the Android-native mapping with the subset currently consumed by Launcher: canonical spacing steps, semantic radii, 44 dp minimum targets, and 48 dp comfortable targets. Current HOME, Dock, drawer, placement-management, and Reorder controls consume these metrics selectively. This is an intentional partial adoption record, not a claim of complete Glaze UI conformance.

Ordinary content remains Solid/Raised in accordance with the Glaze material hierarchy. Functional Glass is not being added merely for decoration; launcher chrome can adopt it later where rendering, reduced-transparency fallback, performance, and device acceptance are proven.

## Validation boundary

JVM tests validate deterministic ordering and DataStore-to-Room mapping semantics. Android CI must compile KSP-generated Room code and schemas, lint the exact pull-request head, run JVM tests, and assemble the debug APK before this source slice can merge.

Even successful CI does not prove on-device Room creation/mirroring, process-death recovery, upgrade behavior, physical drag interaction, rotation behavior, TalkBack behavior, or physical-device HOME use. Those remain emulator/device acceptance gates. Room must not become the live workspace authority until the relational runtime path has its own acceptance evidence.

## Privacy boundary

Search, Favorites, Dock state, ordering state, drag/drop state, Room workspace data, and appearance settings are all local. The relational database adds no Android permission, network permission, remote recommendation service, analytics SDK, advertising SDK, sponsorship system, or cloud-account dependency.
