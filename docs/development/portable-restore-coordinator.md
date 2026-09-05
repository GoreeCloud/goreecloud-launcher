# Combined Launcher portable restore coordinator

Status: **Development persistence candidate**

## Purpose

`LauncherPortableRestoreImport` coordinates the two currently approved bounded Launcher portability formats:

- `goreecloud-launcher-workspace-snapshot/1`
- `goreecloud-launcher-preferences/1`

The coordinator decodes and validates both complete inputs, then validates their shared Home-grid contract before it grants any persistence call. A writer receives the validated workspace snapshot and the validated seven-field preference record together through one `replacePortableState(...)` call.

The current Development branch now provides `LauncherTransactionalPortableRestoreWriter`, backed by the existing Room workspace DAO plus Launcher Preferences DataStore. This is a concrete persistence implementation for the currently approved bounded pair only; it does not widen either portable format or establish product-wide recovery.

## Pair compatibility and reviewed apply

The workspace format is deliberately framework-independent and permits a broader bounded grid domain than the current Launcher preference format. Two payloads can therefore be individually valid while still describing contradictory Launcher state.

For a combined restore, the workspace grid columns and rows must exactly match the portable Home-grid columns and rows in the preference snapshot. A mismatch is rejected as `COMPATIBILITY` before any writer call. The same validation path is reused by the read-only restore preview so preview and apply cannot disagree about whether a pair is acceptable.

`LauncherPortableRestorePreview` also returns an opaque SHA-256 review token over the exact validated byte pair. `applyReviewed(...)` fails closed when either otherwise-valid payload changes after review. The token is only a local review/apply consistency guard; it is not a signature, ownership proof, artifact provenance, Everkeep lineage, or authorization grant.

## Concrete persistence behavior

`LauncherTransactionalPortableRestoreWriter` applies the currently supported pair in the following bounded order:

1. defensively revalidates the workspace and preference values plus their shared Home-grid dimensions;
2. reads the previous seven-field portable preference state;
3. replaces the supported HOME placement subset through one Room transaction;
4. writes the seven portable preferences in one DataStore edit;
5. verifies DataStore readback; and
6. if the DataStore stage fails, performs guarded compensating rollback of both stores.

The Room stage preserves non-HOME state and verifies the applied HOME pages/items before returning. Rollback is allowed only while the current HOME state still exactly matches the state written by the restore. DataStore rollback similarly refuses to overwrite a third state that indicates a concurrent preference change.

Room and Preferences DataStore cannot share one crash-atomic transaction. The current writer therefore provides a real Room transaction plus guarded cross-store compensation, **not** crash atomicity. A process or power loss between stores still requires a higher-level recovery protocol before production recovery can be claimed.

## Clean-target and identity policy

The current `goreecloud-launcher-workspace-snapshot/1` format carries framework-independent page/grid placement plus opaque item IDs. It does not carry the Android package, user/profile, shortcut, folder, widget-provider, AppWidget ID, or equivalent identity metadata needed to materialize arbitrary items on a different or clean target.

For that reason the current writer follows this fail-closed policy:

- it never invents an application/package/profile binding;
- every imported item ID must exactly match the current HOME item identity set;
- the current HOME item set must consist only of already-resolved APP rows with nonblank existing `appKey` values;
- shortcut, folder, widget, unresolved-app, and other unsupported identity types are rejected;
- non-empty portable HOME content cannot be materialized on a clean target that has no corresponding resolved items;
- an empty portable item set may be applied only when the target HOME item set is also empty;
- imported HOME page IDs must not collide with non-HOME page identities; and
- DOCK and other non-HOME state are not modified by this writer.

This is intentionally a **same-resolved-identity placement restore**, not cross-device rebinding. Clean-device reconstruction requires a separately approved, versioned format and policy that can express package/profile/folder/shortcut/widget identity safely.

## Fail-closed behavior

Persistence is not invoked when:

- the workspace snapshot is malformed, tampered, oversized, expanded, unsupported, or fails grid/page/placement validation;
- the preference snapshot is malformed, tampered, expanded, unsupported, or contains an out-of-range value;
- both snapshots are individually valid but their Home-grid dimensions disagree; or
- reviewed apply detects that the exact validated input pair changed after preview.

The concrete Room writer additionally rejects incompatible target identity state, unsupported HOME item types, duplicate identities, and HOME page collisions with non-HOME state before replacing HOME placement state.

A persistence failure is propagated. The writer reports a dedicated apply failure only after the previous bounded state has been restored, and reports a rollback failure when compensating rollback cannot be fully verified. Neither outcome may be represented as an applied restore.

## Authority boundary

This source still does not:

- discover or resolve new installed Android packages for import;
- resolve or remap Android user/profile identities;
- recreate folders or shortcuts;
- recreate widgets or rebind widget providers/AppWidget IDs;
- restore dock/favorites state outside the current workspace snapshot;
- restore hidden-app state, wallpaper, icon packs, Theme Manager state, or broader system settings;
- synchronize across devices;
- establish artifact provenance or account/device ownership;
- create an accepted Everkeep backup or recovery path;
- provide crash-atomic Room/DataStore recovery; or
- authorize production restore, deployment, Release Candidate promotion, or Stable qualification.

The current workspace format remains framework-independent placement state, and the current preference format remains the seven explicit Launcher settings documented in their individual Development contracts.

## Recovery status

Concrete bounded persistence, pair-level compatibility, review/apply consistency, readback verification, guarded rollback, and a fail-closed same-identity target policy now exist in Development source. This is meaningful recovery implementation evidence, but it is still **partial** rather than product recovery.

Before Launcher recovery can be accepted, the remaining work includes the complete approved Launcher-owned state inventory, safe clean-target reconstruction/rebinding for package/profile/folder/shortcut/widget identities, crash/interruption recovery across stores, provenance and user-control requirements, applicable Privacy Shield/Wardveil/Everkeep integration, representative physical-device and HOME-role acceptance, signed-candidate evidence, and production recovery validation.
