# Architecture

GoreeCloud Launcher is Android-native because its role depends on HOME-role integration, `LauncherApps`, shortcuts, widgets, wallpaper/system behavior, profiles, and launcher lifecycle callbacks.

The repository remains a single Android application module during the early milestones so launcher behavior stays easy to inspect while product boundaries stabilize. Planned long-term module boundaries are `core:launcher`, `core:data`, `core:widgets`, `core:theme`, `core:designsystem`, `feature:home`, `feature:drawer`, `feature:search`, `feature:settings`, `feature:onboarding`, and `feature:backup`.

The first-party Glaze Theme Engine begins with local semantic colors and persistent System/Light/Dark appearance. Later milestones add wallpaper palettes, user accents, icon masks, themed icons, dock/folder/drawer surfaces, motion controls, and exportable presets.

## Current source state

Milestone 0 establishes the Android HOME contract, app/profile discovery, Glaze UI shell, local appearance persistence, and privacy/manifest guards.

Milestone 1 currently adds three daily-launcher foundations:

1. Local application search by installed-app label or package name.
2. Durable workspace preferences for ordered Favorites and Dock membership.
3. Explicit, accessible Favorite and Dock ordering controls with deterministic move-earlier/move-later operations.

The workspace slice uses Android Preferences DataStore because Favorites and Dock membership are currently small ordered preference sets rather than relational placement data. Each stored application key combines a public `UserHandle` discriminator with the flattened component name. This avoids treating identical package names across profiles as one launcher item without depending on a hidden Android profile-identifier API.

The first successful app inventory initializes up to twelve Favorites and four Dock items, excluding the launcher itself. After initialization, user choices are authoritative: removing every Favorite or Dock entry does not cause the defaults to be silently re-created.

Favorites and Dock items resolve against the live `LauncherApps` inventory at render time. Missing or uninstalled components therefore disappear safely from the rendered workspace without requiring a launch attempt. A later cleanup/migration layer can prune stale keys once the richer workspace database exists.

The Dock is currently capped at five items. The persistence codec preserves insertion order. `WorkspaceCodec.moved` repositions one existing key by one logical step, clamps movement at collection boundaries, and leaves unknown keys unchanged. This behavior is covered by local JVM unit tests.

App management is available by long-press from All apps, Favorites, and Dock. The management dialog exposes membership state plus Move earlier / Move later controls. This is the first ordering interaction because it is deterministic, keyboard/accessibility compatible through native controls, and provides a non-drag alternative before direct drag/drop is introduced. Future drag/drop must call the same ordering model rather than creating a second source of truth.

## Glaze UI native mapping

The launcher intentionally targets **Glaze UI 1.4 Stable** as its current design-system baseline. The canonical `GoreeCloud/glaze-ui` repository was reviewed before this slice. Its adoption guidance requires semantic mapping before visuals, native platform controls when they provide stronger behavior, practical touch targets, local presentation, and application-specific acceptance rather than assuming design-system stability equals application conformance.

`GlazeMetrics.kt` begins the Android-native mapping with the subset currently consumed by Launcher: canonical spacing steps, semantic radii, 44 dp minimum targets, and 48 dp comfortable targets. Current HOME, Dock, drawer, and placement-management surfaces consume these metrics selectively. This is an intentional partial adoption record, not a claim of complete Glaze UI conformance.

Ordinary content remains Solid/Raised in accordance with the Glaze material hierarchy. Functional Glass is not being added merely for decoration; launcher chrome can adopt it later where rendering, reduced-transparency fallback, performance, and device acceptance are proven.

## Persistence evolution

Preferences DataStore is intentionally temporary for simple launcher preferences. When Milestone 1 expands into multiple workspace pages, cell coordinates, spans, folders, widgets, ordering migrations, and backup/restore relationships, the authoritative workspace model should move to Room or another Android-native SQLite abstraction as specified by the project architecture.

The migration must preserve existing Favorites and Dock choices and use an explicit schema version. DataStore should remain appropriate for independent settings such as appearance, accessibility toggles, and small launcher preferences even after workspace entities move to SQLite.

## Privacy boundary

Search, Favorites, Dock state, ordering state, and appearance settings are all local. This architecture adds no network permission, remote recommendation service, analytics SDK, advertising SDK, or cloud-account dependency.
