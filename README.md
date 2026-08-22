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

The repository contains the Milestone 0 native Android foundation plus the first Milestone 1 usability work:

- Android HOME activity declaration.
- User-controlled `ROLE_HOME` onboarding and lifecycle-aware default-HOME status.
- `LauncherApps` discovery across available profiles.
- Package lifecycle callbacks.
- Basic Glaze home/favorites surface.
- All-apps drawer with local name/package search.
- App launching.
- Persisted System / Light / Dark Glaze appearance foundation.
- No `INTERNET` permission.
- CI privacy and manifest guards.

Still planned: persistent workspace and dock, drag/drop, folders, icon/label customization, gesture bindings, shortcuts, `AppWidgetHost`, richer profile UI, the full Glaze Theme Engine, versioned backup/restore, and physical-device acceptance.

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

Source-level XML, privacy, and manifest guards have been validated. A successful Gradle build, emulator run, signed APK, and physical-device launcher acceptance are not claimed until CI/device testing proves them.

## License

GPL-3.0. See `LICENSE`.
