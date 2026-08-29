# Rendered HOME Page Navigation

## Purpose

This Development surface connects the established terminal Room multi-page workspace model to rendered Home navigation and bounded user-facing page lifecycle controls without widening item-placement authority.

## Authority boundary

`WorkspacePagedHomeObserver` emits paged HOME state only after durable terminal `WorkspaceAuthority.ROOM` is established. Before terminal Room authority, or when the Room workspace cannot be read safely, the observer does not fabricate authoritative secondary-page state.

The observer combines Room page and item streams and maps them into ordered rendered page state. Application items are exposed by `appKey`; unsupported item types are counted so folders, shortcuts, widgets, or malformed rows are not silently misrepresented as launchable applications.

## User-interface boundary

`MainActivity` observes the paged Room state alongside the existing authoritative placement path.

When authoritative HOME pages exist:

- a page selector is rendered, including when only the primary page exists so a new page can be intentionally created;
- the primary page keeps the existing Favorites/Dock UI and mutation callbacks;
- secondary pages render their launchable application items from Room;
- selected-page ordering can be changed through the existing guarded Room page-order mutation; and
- `Add page` requests creation of a new empty page through the terminal-Room mutation repository.

A successful page creation returns the authoritative generated page ID/rank and the UI selects that page only after the Room transaction succeeds. The selected page is otherwise reconciled against the latest authoritative page identity set; stale selection falls back to an available authoritative page.

## Page-creation mutation boundary

`WorkspacePagedRoomMutationRepository.createHomePage` requires terminal Room authority. It reads the complete HOME page snapshot, requires contiguous current ranks, verifies the requested page ID is globally unused, and appends one empty page at the next rank through `WorkspaceDao.appendPageIfSnapshotMatches`.

The DAO transaction compares the complete observed HOME page snapshot again before writing. A concurrent page-set change fails closed rather than silently creating against stale rank state. The transaction creates no child workspace item and moves no existing item.

The UI generates opaque `home:user:<UUID>` page identities locally for the mutation request. Those IDs carry no authority by themselves; Room authority and the transaction remain decisive.

## Destructive-operation boundary

Page deletion is intentionally not exposed in this milestone. `workspace_items.pageId` has an `ON DELETE CASCADE` foreign key, so deletion is a destructive operation that requires a separately designed confirmation, non-empty-page policy, recovery/undo behavior, and acceptance evidence. This slice does not use cascade deletion as a shortcut for page lifecycle support.

Secondary-page item placement editing, live grid cell/span editing, and cross-page drag/drop also remain separate UI milestones even though guarded repository foundations exist.

## Validation

Acceptance for this slice requires:

- Android file-backed Room tests proving creation is reserved before terminal Room authority;
- proof that an accepted creation persists one empty page at the next authoritative rank;
- duplicate page IDs fail closed;
- existing child items survive subsequent page reordering;
- existing privacy, manifest, Glaze UI, Room schema/cutover, lint, unit, assembly, and instrumentation gates; and
- no promotion to production/Stable based on CI alone.

## Remaining boundaries

This milestone does not establish page deletion, secondary-page item editing, cross-page drag/drop UI, production Room cutover acceptance, folders, shortcuts, widgets/AppWidgetHost, representative physical-device HOME acceptance, complete accessibility acceptance, signed release packaging, or Stable qualification.
