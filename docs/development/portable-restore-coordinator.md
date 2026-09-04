# Combined Launcher portable restore coordinator

Status: **Development source boundary only**

## Purpose

`LauncherPortableRestoreImport` coordinates the two currently approved bounded Launcher portability formats:

- `goreecloud-launcher-workspace-snapshot/1`
- `goreecloud-launcher-preferences/1`

The coordinator decodes and validates both complete inputs, then validates their shared Home-grid contract before it grants any persistence call. A writer receives the validated workspace snapshot and the validated seven-field preference record together through one `replacePortableState(...)` call.

This is intended to let a future concrete Room/DataStore adapter provide a real transaction boundary without giving the codec or coordinator broader Android authority.

## Pair compatibility

The workspace format is deliberately framework-independent and permits a broader bounded grid domain than the current Launcher preference format. Two payloads can therefore be individually valid while still describing contradictory Launcher state.

For a combined restore, the workspace grid columns and rows must exactly match the portable Home-grid columns and rows in the preference snapshot. A mismatch is rejected as `COMPATIBILITY` before any writer call. The same validation path is reused by the read-only restore preview so preview and apply cannot disagree about whether a pair is acceptable.

## Fail-closed behavior

The combined writer is not invoked when:

- the workspace snapshot is malformed, tampered, oversized, expanded, unsupported, or fails grid/page/placement validation;
- the preference snapshot is malformed, tampered, expanded, unsupported, or contains an out-of-range value; or
- both snapshots are individually valid but their Home-grid dimensions disagree.

A persistence exception is allowed to propagate. The coordinator therefore cannot label a failed storage commit as an applied restore.

## Authority boundary

This source does **not** implement a concrete Room or DataStore writer. It does not:

- discover or resolve installed Android packages;
- resolve or remap Android user/profile identities;
- recreate folders, shortcuts, widgets, or AppWidget IDs;
- rebind widget providers;
- restore dock/favorites state that is outside the current workspace snapshot;
- restore hidden-app state, wallpaper, icon packs, Theme Manager state, or system settings;
- synchronize across devices;
- establish artifact provenance or account/device ownership;
- create an Everkeep backup or restore path; or
- authorize production restore, deployment, release, or Stable qualification.

The current workspace format remains framework-independent placement state, and the current preference format remains the seven explicit Launcher settings already documented in their individual Development contracts.

## Recovery status

Pair-level compatibility validation closes one contradiction path before persistence, but the combined one-call seam is still not product recovery. A later recovery implementation must define the complete approved Launcher-owned state inventory, concrete transactional persistence, clean-target behavior, package/profile/widget rebinding policy, rollback after persistence failure, provenance, user control, Privacy Shield review, Everkeep integration, representative-device acceptance, and production recovery evidence.
