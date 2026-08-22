# GoreeCloud Launcher

Privacy-first, open-source Android home-screen launcher for GoreeCloud, designed with Glaze UI and a first-party theme engine.

GoreeCloud Launcher is an original GoreeCloud implementation inspired by mature launcher interaction patterns. It is not a fork or visual clone of Nova Launcher, Pixel Launcher, or Samsung One UI Home.

## Product rules

- No ads, sponsorships, promoted apps, or affiliate placement.
- No behavioral advertising or mandatory analytics.
- Core operation is offline-capable.
- No required GoreeCloud server or cloud account.
- Minimal, documented permissions.
- Glaze UI + first-party Glaze Theme Engine.
- Wardveil Security on security surfaces.
- GoreeCloud Privacy Shield on privacy surfaces.

## Current implementation

The repository contains the Milestone 0 native Android foundation and the first Milestone 1 daily-launcher capabilities:

- Android HOME activity declaration.
- User-controlled `ROLE_HOME` onboarding and lifecycle-aware default-HOME status.
- `LauncherApps` discovery across available profiles.
- Package lifecycle callbacks.
- Glaze home surface driven by persisted workspace state.
- Ordered, locally persisted Favorites.
- Ordered, locally persisted Dock with a five-item limit.
- First-run Favorites and Dock seeding from installed launchable apps.
- Long-press app management from All apps, Favorites, and Dock.
- Explicit move-earlier/move-later controls for accessible Favorite and Dock ordering.
- All-apps drawer with local name/package search.
- App launching.
- Persisted System / Light / Dark Glaze appearance foundation.
- Initial native metric mapping for the canonical Glaze UI 1.4 Stable spacing, radius, and touch-target tokens.
- Unit-tested workspace ordering, movement boundaries, and Dock-limit logic.
- No `INTERNET` permission.
- CI privacy and manifest guards.

Still planned: direct drag/drop reordering, multiple workspace pages, folders, icon/label customization, gesture bindings, shortcuts, `AppWidgetHost`, richer profile UI, the full Glaze Theme Engine, versioned backup/restore, and physical-device acceptance.

## Persistence model

Milestone 1 begins with Android Preferences DataStore for small ordered launcher preferences such as Favorites and Dock membership. This keeps the first persistence slice dependency-light and local-only. The richer workspace model described in the project specification can move to Room or another Android-native SQLite abstraction when page placement, folders, widgets, spans, and migrations require relational persistence.

Workspace application keys combine a `UserHandle` discriminator with the flattened component name so the same package can be represented independently across supported profiles without relying on a hidden Android profile identifier API.

## Glaze UI adoption boundary

The current launcher explicitly targets **Glaze UI 1.4 Stable** as the canonical design-system baseline. The Android client maps only the Glaze semantics it currently consumes into native Compose controls: comfortable/minimum touch targets, spacing, control/content radii, local material hierarchy, and accessible native interactions. This mapping does not claim full Glaze UI conformance; phone/tablet visual acceptance and the remaining semantic mappings are still product-specific acceptance work.

## Build baseline

- Kotlin + Jetpack Compose
- Android Gradle Plugin 8.10.1
- Kotlin 2.1.21
- Gradle 8.11.1
- `compileSdk 36`
- `targetSdk 36`
- provisional `minSdk 29`
- JDK 17
- package: `com.goreecloud.launcher`

CI installs Gradle 8.11.1 explicitly. The repository currently includes `gradle-wrapper.properties` but does not yet contain the generated Gradle wrapper scripts/JAR; wrapper completion remains a release-engineering task.

Run with an Android SDK 36 environment:

```bash
gradle --no-daemon lintDebug testDebugUnitTest assembleDebug
```

## Validation boundary

Automated CI covers the Privacy Shield dependency/permission guard, HOME-manifest contract guard, Android lint, unit tests, and debug APK assembly. Emulator behavior, signed release packaging, direct drag/drop behavior, Glaze UI visual acceptance, and physical-device default-HOME acceptance remain separate gates and must not be inferred from a successful CI build.

## License

GPL-3.0. See `LICENSE`.
