# Architecture

GoreeCloud Launcher is Android-native because its role depends on HOME-role integration, `LauncherApps`, shortcuts, widgets, wallpaper/system behavior, profiles, and launcher lifecycle callbacks.

The repository remains a single Android application module during the early milestones so launcher behavior stays easy to inspect while product boundaries stabilize. Planned long-term module boundaries are `core:launcher`, `core:data`, `core:widgets`, `core:theme`, `core:designsystem`, `feature:home`, `feature:drawer`, `feature:search`, `feature:settings`, `feature:onboarding`, and `feature:backup`.

The first-party Glaze Theme Engine begins with local semantic colors and persistent System/Light/Dark appearance. Later milestones add wallpaper palettes, user accents, icon masks, themed icons, dock/folder/drawer surfaces, motion controls, and exportable presets.

## Current source state

Milestone 0 establishes the Android HOME contract, app/profile discovery, Glaze UI shell, local appearance persistence, and privacy/manifest guards.

Milestone 1 currently adds two daily-launcher foundations:

1. Local application search by installed-app label or package name.
2. Durable workspace preferences for ordered Favorites and Dock membership.

The workspace slice uses Android Preferences DataStore because Favorites and Dock membership are currently small ordered preference sets rather than relational placement data. Each stored application key combines the Android user/profile identifier with the flattened component name. This avoids treating identical package names across profiles as one launcher item.

The first successful app inventory initializes up to twelve Favorites and four Dock items, excluding the launcher itself. After initialization, user choices are authoritative: removing every Favorite or Dock entry does not cause the defaults to be silently re-created.

Favorites and Dock items resolve against the live `LauncherApps` inventory at render time. Missing or uninstalled components therefore disappear safely from the rendered workspace without requiring a launch attempt. A later cleanup/migration layer can prune stale keys once the richer workspace database exists.

The Dock is currently capped at five items. The persistence codec preserves insertion order and is covered by local JVM unit tests. App-drawer long-press opens an explicit local management dialog for adding/removing an app from Favorites or the Dock.

## Persistence evolution

Preferences DataStore is intentionally temporary for simple launcher preferences. When Milestone 1 expands into multiple workspace pages, cell coordinates, spans, folders, widgets, ordering migrations, and backup/restore relationships, the authoritative workspace model should move to Room or another Android-native SQLite abstraction as specified by the project architecture.

The migration must preserve existing Favorites and Dock choices and use an explicit schema version. DataStore should remain appropriate for independent settings such as appearance, accessibility toggles, and small launcher preferences even after workspace entities move to SQLite.

## Privacy boundary

Search, Favorites, Dock state, and appearance settings are all local. This architecture adds no network permission, remote recommendation service, analytics SDK, advertising SDK, or cloud-account dependency.
