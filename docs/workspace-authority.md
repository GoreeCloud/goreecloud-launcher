# Workspace authority transition

GoreeCloud Launcher uses an explicit persisted authority state while it moves from the accepted Preferences DataStore Favorites/Dock representation toward the Room relational workspace.

This document describes the Milestone 1 authority-state and dual-read foundation. It does **not** declare the Room cutover complete.

## Persisted phases

`WorkspaceAuthority` has three durable phases:

- `DATASTORE` — Preferences DataStore is the live workspace authority. This is the current production source consumed by Home.
- `ROOM_VERIFIED` — the exact current DataStore-shaped Favorites/Dock snapshot has been mirrored to Room, verified by DAO readback, and is eligible for an independent dual-read check.
- `ROOM` — a guarded terminal authority marker reserved for the future cutover where Home will consume Room placement as the source of truth.

Unknown or malformed persisted authority values fail closed to `DATASTORE`.

## Snapshot binding

A `ROOM_VERIFIED` marker is bound to the exact initialized Favorites/Dock content with a local SHA-256 fingerprint. The fingerprint is deterministic and order-sensitive, includes the initialization state, separates Favorites from Dock, and is stored only as local migration metadata.

The fingerprint is not telemetry, is not exported, and does not contain a reversible copy of the installed-application inventory.

Every legacy DataStore workspace mutation invalidates `ROOM_VERIFIED` back to `DATASTORE` before the changed workspace can be treated as verified. This prevents a stale verification marker from surviving a Favorite/Dock change.

## Mirror reconciliation

`MainActivity` mirrors initialized workspace state through `WorkspaceRelationalMirror` only while `DATASTORE` is authoritative.

- `Verified` calls `WorkspaceRepository.markRoomVerified(expectedState)`.
- `Mismatch` or sanitized `Failed` keeps or returns the launcher to `DATASTORE` authority.
- `Skipped` leaves the current not-yet-initialized state alone.
- `ROOM_VERIFIED` no longer causes another mirror write. It enters the independent dual-read phase instead.
- A future `ROOM` authority state stops the compatibility mirror entirely so DataStore cannot overwrite relational authority after cutover.

`markRoomVerified` rechecks the exact current DataStore snapshot inside the same DataStore edit that writes the authority metadata. If the workspace changed while Room verification was running, the stale result is rejected and the repository stays on DataStore authority.

## Independent dual-read reconciliation

`WorkspaceRelationalReader` reconstructs the current Favorites and Dock ordering from the two Room compatibility pages instead of trusting the mirror result alone. `WorkspaceRelationalReadMapper` requires the persisted rows to remain canonical: both compatibility pages must exist, ranks and item identities must match the deterministic legacy mapping, app records must remain structurally valid, and query-return ordering itself is not treated as semantic ordering.

The dual-read phase runs only for `ROOM_VERIFIED` state and produces categorical results:

- `Match` — reconstructed Room Favorites/Dock exactly equal the still-authoritative DataStore Favorites/Dock.
- `Mismatch` — Room is readable but its canonical reconstructed state does not match DataStore.
- `Failed` — Room could not be read. Only the exception type is retained; application keys are not placed into the result.
- `Skipped` — the workspace is not initialized or is not in `ROOM_VERIFIED`.

A mismatch, read failure, or inability to create the Room DAO causes a bounded fallback to `DATASTORE`. That fallback preserves the user's existing DataStore Favorites and Dock instead of clearing or replacing them. Returning to `DATASTORE` allows the existing mirror path to attempt a fresh verified compatibility snapshot later. The current application runtime still never calls `promoteRoomAuthority()`.

## Guarded one-way promotion

`WorkspaceRepository.promoteRoomAuthority(expectedState)` exists as a guarded migration primitive but is not called by the current launcher runtime.

Promotion succeeds only when:

1. The workspace is initialized.
2. The persisted phase is `ROOM_VERIFIED`.
3. The locally stored verified fingerprint matches the expected workspace.
4. The workspace read inside the promotion transaction still has the same fingerprint.

Once `ROOM` is recorded, compatibility fallback helpers do not silently demote it. That one-way rule is deliberate: after a real cutover, rollback must be an explicit migration/recovery operation rather than an accidental background state change.

## Runtime acceptance coverage

The API 36 emulator test covers the authority-state foundation, the real Room mirror path, and the new dual-read stage. It uses a file-backed Preferences DataStore and the production file-backed Room database to verify:

- Initial DataStore authority.
- Verified Room compatibility state after a successful mirror.
- Persistence of `ROOM_VERIFIED` after the DataStore instance is closed and reopened from the same file.
- Automatic invalidation to `DATASTORE` after a Favorite/Dock mutation.
- Rejection of stale promotion evidence.
- Re-verification of the changed snapshot.
- Guarded promotion to the terminal `ROOM` marker inside the focused migration test only.
- Protection against silent fallback after that terminal marker is set.
- Independent Room dual-read `Match` after verification.
- Detection of deliberate relational divergence as `Mismatch` without changing the authoritative DataStore lists.
- DataStore fallback after the mismatch.
- Sanitized `Failed` behavior when a previously obtained Room DAO can no longer read from a closed database.

This is controlled emulator evidence, not Android process-death acceptance. The production app still does not promote itself to `ROOM`, and Home still reads Favorites/Dock from DataStore.

## Remaining cutover gates

Before Home can actually consume Room as authoritative workspace state, the project still needs:

- A production cutover coordinator that invokes promotion only after all required checks pass.
- Room-backed live workspace reads and writes after promotion.
- Process-death and cold-start recovery evidence for the dual-read and cutover path.
- Representative physical-device storage validation.
- Multi-page and cell/span placement behavior.
- Schema-version migration acceptance for future Room versions.
- Explicit recovery tooling for a post-cutover Room-open or migration failure.
- Physical-device default-HOME acceptance and release acceptance.

Until those gates are completed, the presence of the `ROOM` enum, guarded promotion API, and dual-read reconciler are migration infrastructure only, not a claim that Room is the current launcher source of truth.
