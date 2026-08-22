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
- `LauncherApps` discovery across available profiles and package lifecycle callbacks.
- Ordered, locally persisted Favorites and a five-item Dock.
- First-run Favorites/Dock seeding from installed launchable apps.
- Long-press placement management from All apps, Favorites, and Dock.
- Explicit move-earlier/move-later controls plus Reorder-mode Favorite/Dock drag/drop using the same persistence source of truth.
- All-apps drawer with local label/package search and app launching.
- Persisted System / Light / Dark Glaze appearance foundation.
- Native metric mapping for the canonical Glaze UI 1.4 Stable spacing, radius, and touch-target tokens currently consumed by Launcher.
- Staged AndroidX Room 3 relational workspace database with versioned page/item entities and deterministic DataStore import mapping.
- Room mirror write/readback verification and strict canonical relational comparison.
- Durable `DATASTORE`, `ROOM_VERIFIED`, and guarded terminal `ROOM` authority phases bound to a local order-sensitive snapshot fingerprint.
- Independent Room dual-read reconciliation that preserves DataStore workspace state on mismatch or read failure.
- `WorkspaceStartupReconciler` for deterministic pre-cutover startup/recovery and retryable Room acquisition.
- Reserved `WorkspaceRoomPlacementRepository` read/write primitives that refuse access until persisted authority is already `ROOM`.
- Source-controlled Room schema history with fail-closed CI drift detection.
- API 36 instrumentation using the production file-backed Room database and Android SQLite driver.
- No `INTERNET` permission.

Preferences DataStore remains the **live Home workspace authority**. Production does not call guarded Room promotion and Home does not use `WorkspaceRoomPlacementRepository` yet.

Still planned: production Room cutover/routing, multiple workspace pages and live cell/span placement, folders, icon/label customization, gesture bindings, shortcuts, `AppWidgetHost`, richer profile UI, the full Glaze Theme Engine, versioned backup/restore, complete Gradle wrapper publication, Android process-death acceptance, and representative physical-device acceptance.

## Workspace interaction model

Normal Home mode prioritizes launching and management: tap launches an app and long-press opens local placement controls. Selecting **Reorder** enters a separate edit state for Favorites and Dock. Reorder mode disables ordinary tile launching/long-press handling, visually lifts the dragged tile, highlights a valid live target, and commits the final move through `WorkspaceRepository`.

Move earlier / Move later remains a permanent non-drag path for keyboard, switch-access, and users who do not want gesture-only ordering.

Source/CI validation of this path does not equal physical-device touch acceptance. Pointer hit targeting, drag feedback, rotation behavior, TalkBack behavior, and default-HOME use still require representative-device validation.

## Persistence and authority model

Preferences DataStore stores the currently accepted ordered Favorite/Dock state and remains the source consumed by Home. Application keys combine a `UserHandle` discriminator with the flattened component name so supported profiles can represent the same package independently without relying on hidden Android APIs.

The staged relational layer uses:

- AndroidX Room **3.0.1**.
- Kotlin Symbol Processing for Room code generation.
- AndroidX SQLite **2.7.0** with `AndroidSQLiteDriver`.
- Version-1 `LauncherDatabase`.
- `workspace_pages` for stable page identity/container rank.
- `workspace_items` for item identity, item type, application key, deterministic rank, optional cell coordinates, and spans.
- Reserved item-type vocabulary for apps, shortcuts, folders, and widgets without claiming those later behaviors are implemented.

`WorkspaceLegacyImportMapper` converts current ordered Favorites/Dock into `home:0` and `dock:0` compatibility pages. `WorkspaceRelationalMirror` writes that compatibility snapshot and reads it back. `WorkspaceRelationalVerifier` rejects missing, extra, reranked, retagged, identity-changed, or placement-altered records.

The durable authority state machine has three phases:

- `DATASTORE` — live production authority.
- `ROOM_VERIFIED` — the exact current DataStore snapshot is mirrored, verified, fingerprint-bound, and eligible for independent dual-read reconciliation.
- `ROOM` — guarded terminal marker reserved for an accepted cutover.

Any pre-cutover Favorite/Dock mutation invalidates `ROOM_VERIFIED`. `WorkspaceRelationalReader` independently reconstructs canonical Room Favorites/Dock while verified. `WorkspaceStartupReconciler` centralizes the mirror/read/fallback decision and keeps ordinary DataStore operation available when Room is unavailable.

`WorkspaceRepository.promoteRoomAuthority()` is a guarded one-way primitive but is not called by production runtime.

### Reserved Room-backed placement I/O

`WorkspaceRoomPlacementRepository` is intentionally disconnected from Home. It reads the persisted authority before every operation:

- Uninitialized, `DATASTORE`, or `ROOM_VERIFIED` -> `Reserved`.
- Missing Room DAO -> `Unavailable`.
- `ROOM` -> canonical Room placement read/write may proceed.

The current `replace()` primitive deduplicates ordered Favorite/Dock keys, retains the five-item Dock limit, transactionally replaces the two compatibility containers, reads them back, and returns `Written` only when canonical Room state matches the normalized request.

This is a post-cutover contract rehearsal, not production cutover. The repository still covers only the current app-based Home/Dock compatibility containers; richer multi-page, folder, shortcut, widget, and cell/span operations remain future work.

## Runtime validation

The Android instrumentation suite currently contains:

- `WorkspaceRoomRuntimeTest` — real Room creation, mirror/readback, close/reopen persistence, replacement semantics, authority metadata, guarded test-only promotion, dual-read Match/Mismatch, and sanitized failure behavior.
- `WorkspaceStartupReconcilerRuntimeTest` — repeated DataStore/Room client reopen, verified-state durability, pre-cutover Room failure fallback, workspace preservation, and later Room recovery.
- `WorkspaceRoomPlacementRepositoryRuntimeTest` — pre-cutover access denial, guarded test-only promotion, canonical Room reads, normalized Room writes, Dock-limit enforcement, and unavailable-DAO behavior.

A validation gap was discovered while adding the third test: the previous CI command filtered instrumentation to `WorkspaceRoomRuntimeTest` only. That means the separate startup-recovery test added in the preceding source slice existed but was not executed by that exact earlier emulator command. Historical evidence is not retroactively upgraded.

The CI workflow is corrected in this slice to run the complete `connectedDebugAndroidTest` suite without a single-class filter. The complete-suite configuration becomes accepted only after the exact pull-request head passes its emulator job.

Repeated persistence-client reopen does not equal Android OS process-death testing; the instrumentation process is not killed and recreated.

## Glaze UI adoption boundary

The launcher explicitly targets **Glaze UI 1.4 Stable** as its canonical design-system baseline and records the reviewed revision in `docs/glaze-ui-adoption.md`. The Android client currently maps the Glaze semantics it consumes into native Compose controls: comfortable/minimum touch targets, spacing, control/content radii, local material hierarchy, and accessible native interactions.

`scripts/check_glaze_ui.py` verifies that the mapped subset still matches the recorded Glaze UI 1.4 contract. This supports an adoption-candidate state; it does not claim complete Glaze UI conformance. Phone/tablet rendered/native acceptance and remaining semantic mappings are product-specific acceptance work.

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

Run source/build validation with Android SDK 36:

```bash
python3 scripts/check_privacy.py
python3 scripts/check_manifest.py
python3 scripts/check_glaze_ui.py
gradle --no-daemon lintDebug testDebugUnitTest assembleDebug
python3 scripts/check_room_schema.py
```

With an API 36 emulator connected, run the complete Android runtime suite with:

```bash
gradle --no-daemon connectedDebugAndroidTest
```

## Validation boundary

The normal validation job covers the Privacy Shield dependency/permission guard, HOME-manifest contract guard, Glaze UI mapped-subset contract, KSP/Room compilation and schema generation, Room schema-history drift detection, Android lint, JVM unit tests, and debug APK assembly. A second job uses an API 36 `x86_64` emulator and immutable-pinned Android Emulator Runner revision `a421e43855164a8197daf9d8d40fe71c6996bb0d` to execute all Android instrumentation tests.

Passing that suite proves only the controlled emulator scenarios encoded by the tests. It does **not** prove Android OS process-death recovery, schema-version upgrade migration, production Room-authority cutover, post-cutover failure recovery, representative physical-device behavior, physical drag interaction, signed release packaging, complete Glaze UI visual acceptance, or physical-device default-HOME acceptance.

## License

GPL-3.0. See `LICENSE`.
