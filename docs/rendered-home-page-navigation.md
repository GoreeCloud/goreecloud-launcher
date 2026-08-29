# Rendered HOME Page Navigation

## Purpose

This Development slice connects the already-established terminal Room multi-page workspace model to a rendered Home navigation surface without widening placement-editing authority.

## Authority boundary

`WorkspacePagedHomeObserver` emits paged HOME state only after durable terminal `WorkspaceAuthority.ROOM` is established. Before terminal Room authority, or when the Room workspace cannot be read safely, the observer does not fabricate authoritative secondary-page state.

The observer combines Room page and item streams and maps them into ordered rendered page state. Application items are exposed by `appKey`; unsupported item types are counted so folders, shortcuts, widgets, or malformed rows are not silently misrepresented as launchable applications.

## User-interface boundary

`MainActivity` observes the paged Room state alongside the existing authoritative placement path.

When multiple authoritative HOME pages exist:

- a page selector is rendered;
- the primary page keeps the existing Favorites/Dock UI and mutation callbacks;
- secondary pages render their launchable application items from Room; and
- secondary-page placement editing is intentionally disabled.

The selected page is reconciled against the latest authoritative page identity set. If the selected page disappears, selection falls back to the first available authoritative page rather than retaining stale identity.

## Mutation boundary

The repository already contains guarded Room page-order and cross-page item mutation foundations. This slice does not expose them from secondary-page UI. Page creation/deletion, page reordering, live grid cell/span editing, and cross-page drag/drop require explicit user-facing mutation routing and separate acceptance.

## Validation

Acceptance for this slice requires:

- JVM mapping tests for ordered page/item projection and unsupported-item accounting;
- Android file-backed Room observation tests proving updates are emitted from real database changes only under terminal Room authority;
- existing privacy, manifest, Glaze UI, Room schema/cutover, lint, unit, assembly, and instrumentation gates; and
- no promotion to production/Stable based on CI alone.

## Remaining boundaries

This milestone does not establish production Room cutover, full multi-page editing, folders, shortcuts, widgets/AppWidgetHost, representative physical-device HOME acceptance, complete accessibility acceptance, signed release packaging, or Stable qualification.
