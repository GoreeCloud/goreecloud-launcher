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
- Explicit Reorder mode with direct Favorite and Dock drag/drop target ordering while normal launch behavior is temporarily disabled.
- Visual drag lift and valid-drop-target highlighting during Reorder mode.
- All-apps drawer with local name/package search.
- App launching.
- Persisted System / Light / Dark Glaze appearance foundation.
- Native metric mapping for the canonical Glaze UI 1.4 Stable spacing, radius, and touch-target tokens currently consumed by Launcher.
- Fail-closed Glaze UI 1.4 contract validation in CI for the mapped token subset and adoption evidence.
- Unit-tested workspace ordering, direct-drop target semantics, movement boundaries, and Dock-limit logic.
- No `INTERNET` permission.
- CI privacy, HOME-manifest, and Glaze UI contract guards.

Still planned: multiple workspace pages, relational cell/span placement, folders, icon/label customization, gesture bindings, shortcuts, `AppWidgetHost`, richer profile UI, the full Glaze Theme Engine, versioned backup/restore, complete Gradle wrapper publication, and physical-device acceptance.

## Workspace interaction model

Normal Home mode prioritizes launching and management: tap launches an app and long-press opens local placement controls. Selecting **Reorder** enters a separate edit state for Favorites and Dock. In Reorder mode, ordinary tile launching and long-press management are disabled so pointer movement cannot accidentally launch or rearrange an app at the same time.

Dragging a Favorite or Dock tile over another live tile highlights the current drop target. Releasing commits the change through the same `WorkspaceRepository` used by Move earlier / Move later. The non-drag controls remain permanent rather than being removed after drag support, preserving an explicit path for keyboard, switch-access, and users who do not want gesture-only ordering.

Source/CI validation of this path does not equal emulator or physical-device touch acceptance. Pointer hit targeting, drag feedback, rotation behavior, accessibility-service behavior, and default-HOME use still require runtime validation.

## Persistence model

Milestone 1 begins with Android Preferences DataStore for small ordered launcher preferences such as Favorites and Dock membership. This keeps the first persistence slice dependency-light and local-only. The richer workspace model described in the project specification can move to Room or another Android-native SQLite abstraction when page placement, folders, widgets, spans, and migrations require relational persistence.

Workspace application keys combine a `UserHandle` discriminator with the flattened component name so the same package can be represented independently across supported profiles without relying on a hidden Android profile identifier API.

`WorkspaceCodec.moved` supports deterministic one-step accessible movement. `WorkspaceCodec.movedToTarget` supports direct drag/drop by moving a live entry toward another live target key while failing safely for self-drops or unknown keys. Both operations are persisted through one repository so future Room migration has one ordering contract to preserve.

## Glaze UI adoption boundary

The current launcher explicitly targets **Glaze UI 1.4 Stable** as the canonical design-system baseline and records the reviewed canonical revision in `docs/glaze-ui-adoption.md`. The Android client maps only the Glaze semantics it currently consumes into native Compose controls: comfortable/minimum touch targets, spacing, control/content radii, local material hierarchy, and accessible native interactions.

`scripts/check_glaze_ui.py` verifies that the native metric subset still matches the recorded Glaze UI 1.4 values and that the repository keeps its exact design-system reference and unresolved acceptance boundaries explicit. This supports an evidence-backed adoption-candidate state; it does not claim full Glaze UI conformance. Phone/tablet rendered/native acceptance and the remaining semantic mappings are still product-specific acceptance work.

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
python3 scripts/check_privacy.py
python3 scripts/check_manifest.py
python3 scripts/check_glaze_ui.py
gradle --no-daemon lintDebug testDebugUnitTest assembleDebug
```

## Validation boundary

Automated CI covers the Privacy Shield dependency/permission guard, HOME-manifest contract guard, Glaze UI mapped-subset contract, Android lint, unit tests, and debug APK assembly. Direct drag/drop source behavior is included in this build/test boundary, but emulator behavior, physical touch interaction, signed release packaging, full Glaze UI visual acceptance, and physical-device default-HOME acceptance remain separate gates and must not be inferred from a successful CI build.

## License

GPL-3.0. See `LICENSE`.
