# Workspace authority transition

GoreeCloud Launcher uses an explicit persisted authority state while it moves from the accepted Preferences DataStore Favorites/Dock representation toward the Room relational workspace.

This document describes the Milestone 1 authority-state foundation. It does **not** declare the Room cutover complete.

## Persisted phases

`WorkspaceAuthority` has three durable phases:

- `DATASTORE` — Preferences DataStore is the live workspace authority. This is the current production source consumed by Home.
- `ROOM_VERIFIED` — the exact current DataStore-shaped Favorites/Dock snapshot has been mirrored to Room and verified by DAO readback.
- `ROOM` — a guarded terminal authority marker reserved for the future cutover where Home will consume Room placement as the source of truth.

Unknown or malformed persisted authority values fail closed to `DATASTORE`.

## Snapshot binding

A `ROOM_VERIFIED` marker is bound to the exact initialized Favorites/Dock content with a local SHA-256 fingerprint. The fingerprint is deterministic and order-sensitive, includes the initialization state, separates Favorites from Dock, and is stored only as local migration metadata.

The fingerprint is not telemetry, is not exported, and does not contain a reversible copy of the installed-application inventory.

Every legacy DataStore workspace mutation invalidates `ROOM_VERIFIED` back to `DATASTORE` before the changed workspace can be treated as verified. This prevents a stale verification marker from surviving a Favorite/Dock change.

## Mirror reconciliation

`MainActivity` continues to mirror initialized DataStore state through `WorkspaceRelationalMirror` while DataStore or verified compatibility state is active.

- `Verified` calls `WorkspaceRepository.markRoomVerified(expectedState)`.
- `Mismatch` or sanitized `Failed` calls `WorkspaceRepository.markDataStoreAuthoritative()`.
- `Skipped` leaves the current not-yet-initialized state alone.
- A future `ROOM` authority state stops legacy DataStore-to-Room mirroring so the compatibility path cannot overwrite relational authority after cutover.

`markRoomVerified` rechecks the exact current DataStore snapshot inside the same DataStore edit that writes the authority metadata. If the workspace changed while Room verification was running, the stale result is rejected and the repository stays on DataStore authority.

## Guarded one-way promotion

`WorkspaceRepository.promoteRoomAuthority(expectedState)` exists as a guarded migration primitive but is not called by the current launcher runtime.

Promotion succeeds only when:

1. The workspace is initialized.
2. The persisted phase is `ROOM_VERIFIED`.
3. The locally stored verified fingerprint matches the expected workspace.
4. The workspace read inside the promotion transaction still has the same fingerprint.

Once `ROOM` is recorded, compatibility fallback helpers do not silently demote it. That one-way rule is deliberate: after a real cutover, rollback must be an explicit migration/recovery operation rather than an accidental background state change.

## Runtime acceptance coverage

The API 36 emulator test now covers the authority-state foundation in addition to the existing Room runtime mirror checks. It uses a file-backed Preferences DataStore and the production file-backed Room database to verify:

- Initial DataStore authority.
- Verified Room compatibility state after a successful mirror.
- Persistence of `ROOM_VERIFIED` after the DataStore instance is closed and reopened from the same file.
- Automatic invalidation to `DATASTORE` after a Favorite/Dock mutation.
- Rejection of stale promotion evidence.
- Re-verification of the changed snapshot.
- Guarded promotion to the terminal `ROOM` marker.
- Protection against silent fallback after that terminal marker is set.

This is controlled emulator evidence, not Android process-death acceptance. The app still does not promote itself to `ROOM`, and Home still reads Favorites/Dock from DataStore.

## Remaining cutover gates

Before Home can actually consume Room as authoritative workspace state, the project still needs:

- A production cutover coordinator that invokes promotion only after all required checks pass.
- Room-backed live workspace reads and writes.
- Process-death and cold-start recovery evidence.
- Representative physical-device storage validation.
- Multi-page and cell/span placement behavior.
- Schema-version migration acceptance for future Room versions.
- Explicit recovery tooling for a post-cutover Room-open or migration failure.
- Physical-device default-HOME acceptance and release acceptance.

Until those gates are completed, the presence of the `ROOM` enum and guarded promotion API is migration infrastructure only, not a claim that Room is the current launcher source of truth.
