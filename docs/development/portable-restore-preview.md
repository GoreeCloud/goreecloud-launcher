# Launcher Portable Restore Preview

Status: Development source candidate.

`LauncherPortableRestorePreview` is a read-only preflight for the two currently reviewed Launcher portability formats: the workspace placement snapshot and the seven-field Launcher preference snapshot.

Both complete payloads must pass their existing strict decoders before a preview is returned. Invalid, tampered, expanded, unsupported, or noncanonical input is rejected with the same workspace/preferences source distinction used by the combined restore coordinator.

The preview exposes only:

- workspace grid columns and rows;
- page count and total placement count; and
- home columns, home rows, drawer columns, label visibility, icon scale, layout-lock state, and GoreeCloud Index Home mode.

It intentionally does not expose page IDs, item IDs, package names, Android profile identities, widget bindings, folder/dock contents, shortcuts, wallpaper/theme state, hidden applications, broader search state, or any unreviewed Launcher preference.

The preview has no `LauncherPortableRestoreWriter`, Room, DataStore, package/profile discovery, widget, HOME-role, or persistence authority. A successful preview is therefore not a restore, backup, Everkeep recovery, migration, or proof that the target device can materialize every referenced application item.

A later user-facing restore workflow must still establish explicit user confirmation, a concrete atomic writer, failure/rollback behavior, target-device compatibility, provenance/recovery lineage where required, and representative acceptance before product restore can be claimed.
