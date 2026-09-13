# Atomic Home Group Movement

**Status:** Development implementation

## Purpose

GoreeCloud Launcher multi-select Home editing must not leave a user's selected application group partially moved when the workspace changes or a requested destination can no longer accept the complete operation.

The atomic group-movement path replaces the earlier sequential best-effort behavior for moving multiple applications between secondary Home pages.

## Authority

Room remains the only post-cutover workspace mutation authority.

The group-move planner may read the current Home page and item snapshot and calculate deterministic target placements, but that preflight read carries no write authority. The actual mutation is accepted only by a Room `@Transaction` that re-reads and compares the complete expected Home page/item snapshot immediately before committing the planned placement set.

If the expected snapshot no longer matches, the transaction returns false and writes none of the selected items.

No compensating rollback loop or secondary batch state store is used.

## Accepted operation

A group move can succeed only when all of the following are true:

- terminal Room workspace authority is active;
- source and target page identifiers are non-empty and distinct;
- source and target are secondary Home pages;
- the protected primary compatibility Home page is not used as a source or target;
- the selection is non-empty;
- every application key is non-empty and unique;
- every selected application resolves exactly once on the source page;
- the stored secondary Home geometry is spatially valid;
- deterministic non-overlapping target placements can be produced for the complete selection;
- every planned item identity still exists at commit time;
- the complete Home page and item snapshots still exactly match the snapshots observed by the planner.

Only after every condition passes does Room bulk-upsert the complete set of updated item rows in one transaction.

## User-visible behavior

The edit-mode multi-select action now has two outcomes:

1. **Complete success:** every selected application moves to the target page and Launcher navigates to that page.
2. **Rejected:** no selected application moves. Launcher reports that the workspace changed or the destination could not accept the complete selection.

The previous partial-success state (`Moved X of Y apps`) is intentionally removed.

## Preserved boundaries

This implementation does not add or claim:

- primary Home grid mutation;
- undo or history;
- compensating rollback journals;
- group drag/drop;
- folders or widgets;
- shortcut group movement;
- cloud synchronization;
- telemetry or behavioral profiling;
- new package visibility;
- new network authority.

## Validation

Automated runtime acceptance covers:

- complete two-app movement into a secondary target page;
- deterministic target ordering and ranks;
- stale expected-snapshot rejection after a concurrent item change;
- verification that stale rejection leaves all planned selected rows unapplied;
- duplicate selection rejection;
- missing application rejection;
- primary-page protection;
- no mutation after invalid-selection rejection.

The existing Launcher Android CI remains authoritative for source policy checks, JVM tests, lint/build, Room schema checks, APK staging, and Android runtime tests.

## Release boundary

This development implementation does not establish Stable or production acceptance. Representative physical-device/default-HOME validation, TalkBack/Switch Access, large-text and landscape validation, one-handed ergonomics, performance acceptance, and the broader GoreeCloud application release requirements remain open gates.
