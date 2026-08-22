# Architecture

GoreeCloud Launcher is Android-native because its role depends on HOME-role integration, `LauncherApps`, shortcuts, widgets, wallpaper/system behavior, profiles and launcher lifecycle callbacks.

The initial archive stays one module to keep Milestone 0 inspectable. Planned boundaries are `core:launcher`, `core:data`, `core:widgets`, `core:theme`, `core:designsystem`, `feature:home`, `feature:drawer`, `feature:search`, `feature:settings`, `feature:onboarding`, and `feature:backup`.

The first-party Glaze Theme Engine begins with local semantic colors and persistent System/Light/Dark appearance. Later milestones add wallpaper palettes, user accents, icon masks, themed icons, dock/folder/drawer surfaces, motion controls and exportable presets.

## Current source state

Milestone 0 establishes the Android HOME contract, app/profile discovery, Glaze UI shell, local appearance persistence, and privacy/manifest guards. The first Milestone 1 slice adds local app-drawer filtering by application label or package name. The search path is entirely on-device and introduces no network permission or provider dependency.
