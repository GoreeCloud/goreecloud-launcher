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
- Staged Room 3 relational workspace database with page/item entities, deterministic DataStore import mapping, and a fail-safe compatibility mirror.
- Room mirror write/readback verification that compares persisted compatibility rows against the deterministic expected mapping before returning a verified result.
- API 36 instrumentation coverage that creates the real file-backed Room database, verifies a mirrored snapshot, closes/reopens the database, replaces the snapshot, proves stale rows are removed, and verifies empty-container recovery.
- All-apps drawer with local name/package search.
- App launching.
- Persisted System / Light / Dark Glaze appearance foundation.
- Native metric mapping for the canonical Glaze UI 1.4 Stable spacing, radius, and touch-target tokens currently consumed by Launcher.
- Fail-closed Glaze UI 1.4 contract validation in CI for the mapped token subset and adoption evidence.
- Unit-tested workspace ordering, direct-drop target semantics, movement boundaries, Dock-limit logic, legacy-to-relational mapping, and relational snapshot comparison.
- No `INTERNET` permission.
- CI privacy, HOME-manifest, Glaze UI, Room schema-history, and Android-emulator Room runtime gates.

Still planned: relational workspace authority/cutover, multiple workspace pages and live cell/span placement, folders, icon/label customization, gesture bindings, shortcuts, `AppWidgetHost`, richer profile UI, the full Glaze Theme Engine, versioned backup/restore, complete Gradle wrapper publication, and physical-device acceptance.

## Workspace interaction model

Normal Home mode prioritizes launching and management: tap launches an app and long-press opens local placement controls. Selecting **Reorder** enters a separate edit state for Favorites and Dock. In Reorder mode, ordinary tile launching and long-press management are disabled so pointer movement cannot accidentally launch or rearrange an app at the same time.

Dragging a Favorite or Dock tile over another live tile highlights the current drop target. Releasing commits the change through the same `WorkspaceRepository` used by Move earlier / Move later. The non-drag controls remain permanent rather than being removed after drag support, preserving an explicit path for keyboard, switch-access, and users who do not want gesture-only ordering.

Source/CI validation of this path does not equal physical-device touch acceptance. Pointer hit targeting, drag feedback, rotation behavior, accessibility-service behavior, and default-HOME use still require representative-device validation.

## Persistence model

Preferences DataStore remains the **live workspace authority** during the current transition. It stores the accepted ordered Favorite and Dock state and remains the source consumed by Home. Workspace application keys combine a `UserHandle` discriminator with the flattened component name so the same package can be represented independently across supported profiles without relying on a hidden Android profile identifier API.

`WorkspaceCodec.moved` supports deterministic one-step accessible movement. `WorkspaceCodec.movedToTarget` supports direct drag/drop by moving a live entry toward another live target key while failing safely for self-drops or unknown keys. Both operations use one repository ordering contract.

The next persistence layer is present as a staged compatibility mirror:

- AndroidX Room **3.0.1**.
- Kotlin Symbol Processing for Room code generation.
- AndroidX SQLite **2.7.0** with `AndroidSQLiteDriver`.
- Version-1 `LauncherDatabase`.
- `workspace_pages` for stable Home/Dock page identity and rank.
- `workspace_items` for item identity, item type, application key, deterministic rank, optional cell coordinates, and spans.
- Reserved item-type vocabulary for apps, shortcuts, folders, and widgets without claiming those later features are implemented.
- `WorkspaceLegacyImportMapper` converts current Favorite/Dock lists into `home:0` and `dock:0` relational pages.
- `WorkspaceRelationalMirror` transactionally refreshes the relational compatibility snapshot after initialized DataStore state changes.
- After each mirror write, DAO readback retrieves the same compatibility pages/items and `WorkspaceRelationalVerifier` compares them against the deterministic expected snapshot. Equivalent row sets are accepted regardless of query order; missing, extra, reranked, retagged, or placement-altered records return a typed mismatch.
- `WorkspaceRoomRuntimeTest` exercises that path against the real Android SQLite driver and a persistent database file on an API 36 emulator, including close/reopen durability, replacement semantics, stale-row removal, and empty-container clearing.

The mirror is deliberately **not** the live source of truth yet. It is fail-safe so a database exception or verification mismatch does not replace the accepted DataStore launcher path; coroutine cancellation is never swallowed. Verification results do not expose application keys or installed-app inventory. Emulator persistence/readback evidence is a required cutover prerequisite, not the cutover itself. A later authority change must still define one-way migration state, rollback/recovery behavior, and representative device acceptance before Home consumes Room placement as authoritative state.

The Room Gradle plugin exports schema history to `app/schemas`. The exact generated version-1 schema is committed, and CI fails if regeneration modifies or creates uncommitted schema history. Future Room schema upgrades must preserve committed schema evidence and use explicit migration or accepted auto-migration paths; destructive fallback is not the normal upgrade strategy.

## Glaze UI adoption boundary

The current launcher explicitly targets **Glaze UI 1.4 Stable** as the canonical design-system baseline and records the reviewed canonical revision in `docs/glaze-ui-adoption.md`. The Android client maps only the Glaze semantics it currently consumes into native Compose controls: comfortable/minimum touch targets, spacing, control/content radii, local material hierarchy, and accessible native interactions.

`scripts/check_glaze_ui.py` verifies that the native metric subset still matches the recorded Glaze UI 1.4 values and that the repository keeps its exact design-system reference and unresolved acceptance boundaries explicit. This supports an evidence-backed adoption-candidate state; it does not claim full Glaze UI conformance. Phone/tablet rendered/native acceptance and the remaining semantic mappings are still product-specific acceptance work.

## Build baseline

- Kotlin + Jetpack Compose
- Android Gradle Plugin 8.10.1
- Kotlin 2.1.21
- KSP 2.1.21-2.0.2
- AndroidX Room 3.0.1
- AndroidX SQLite 2.7.0
- AndroidX Test Runner 1.7.0
- AndroidX Test Ext JUnit 1.3.0
- Gradle 8.11.1
- `compileSdk 36`
- `targetSdk 36`
- provisional `minSdk 29`
- JDK 17
- package: `com.goreecloud.launcher`

CI installs Gradle 8.11.1 explicitly. The repository currently includes `gradle-wrapper.properties` but does not yet contain the generated Gradle wrapper scripts/JAR; wrapper completion remains a release-engineering task.

Run source/build validation with an Android SDK 36 environment:

```bash
python3 scripts/check_privacy.py
python3 scripts/check_manifest.py
python3 scripts/check_glaze_ui.py
gradle --no-daemon lintDebug testDebugUnitTest assembleDebug
python3 scripts/check_room_schema.py
```

With an API 36 emulator connected, run the focused relational runtime gate with:

```bash
gradle --no-daemon connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goreecloud.launcher.core.workspace.db.WorkspaceRoomRuntimeTest
```

## Validation boundary

The normal validation job covers the Privacy Shield dependency/permission guard, HOME-manifest contract guard, Glaze UI mapped-subset contract, KSP/Room compilation and schema generation, Room schema-history drift detection, Android lint, JVM unit tests, and debug APK assembly. A second CI job uses an API 36 `x86_64` Android emulator and an immutable-pinned Android Emulator Runner revision to execute the focused Room runtime instrumentation test.

Passing the emulator gate proves real Android database creation, DAO write/readback behavior, close/reopen persistence, compatibility-snapshot replacement, stale-row removal, and empty-container recovery for this test scenario. It does **not** prove process-death recovery, schema-version upgrade migration, Room-authority cutover, representative physical-device behavior, physical drag interaction, signed release packaging, complete Glaze UI visual acceptance, or physical-device default-HOME acceptance.

## License

GPL-3.0. See `LICENSE`.
