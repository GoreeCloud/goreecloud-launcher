# Rendered HOME Page Navigation

## Purpose

This Development surface connects the established terminal Room multi-page workspace model to rendered Home navigation and bounded user-facing page lifecycle/item-placement controls without creating a second workspace authority.

## Authority boundary

`WorkspacePagedHomeObserver` emits paged HOME state only after durable terminal `WorkspaceAuthority.ROOM` is established. Before terminal Room authority, or when the Room workspace cannot be read safely, the observer does not fabricate authoritative secondary-page state.

The observer combines Room page and item streams and maps them into ordered rendered page state. Application items are exposed by `appKey`; unsupported item types are counted so folders, shortcuts, widgets, or malformed rows are not silently misrepresented as launchable applications.

## User-interface boundary

`MainActivity` observes the paged Room state alongside the existing authoritative placement path.

When authoritative HOME pages exist:

- a page selector is rendered, including when only the primary page exists so a new page can be intentionally created;
- the primary page keeps the existing Favorites/Dock UI and mutation callbacks;
- secondary pages render their launchable application items from Room;
- selected-page ordering can be changed through the existing guarded Room page-order mutation;
- `Add page` requests creation of a new empty page through the terminal-Room mutation repository;
- `Delete empty page` is exposed only when the selected page is non-primary and the authoritative rendered snapshot contains no application or unsupported items; and
- application tiles on a secondary page can request a move to another existing Home page through a bounded `Move` menu.

A successful page creation returns the authoritative generated page ID/rank and the UI selects that page only after the Room transaction succeeds. A successful deletion returns the authoritative remaining page identity order; if the selected page was removed, the UI returns to the protected primary page. A successful application-page move returns `UpdatedItem`, and the UI selects the authoritative target page only after that write succeeds. Selection is otherwise reconciled against the latest authoritative page identity set.

## Page-creation mutation boundary

`WorkspacePagedRoomMutationRepository.createHomePage` requires terminal Room authority. It reads the complete HOME page snapshot, requires contiguous current ranks, verifies the requested page ID is globally unused, and appends one empty page at the next rank through `WorkspaceDao.appendPageIfSnapshotMatches`.

The DAO transaction compares the complete observed HOME page snapshot again before writing. A concurrent page-set change fails closed rather than silently creating against stale rank state. The transaction creates no child workspace item and moves no existing item.

The UI generates opaque `home:user:<UUID>` page identities locally for the mutation request. Those IDs carry no authority by themselves; Room authority and the transaction remain decisive.

## Empty-page deletion boundary

`WorkspacePagedRoomMutationRepository.deleteEmptyHomePage` requires terminal Room authority and refuses the protected primary HOME page, an unknown page, the last remaining page, or any page that contains an item in the observed authoritative snapshot.

`WorkspaceDao.deleteEmptyPageIfSnapshotMatches` then repeats the complete HOME page and item snapshot comparison inside the Room transaction and re-checks that the target page is empty before deletion. This is the critical cascade-safety boundary: if a child item appears after the caller's initial read, the transaction fails closed instead of allowing the `ON DELETE CASCADE` foreign key to remove that item. Only after those checks pass is the empty page removed and remaining page ranks compacted transactionally.

This is deliberately not general destructive page deletion. Populated pages cannot be deleted from the UI or repository through this path. Moving/removing their contents, confirmation UX for destructive operations, recovery/undo semantics, and broader page management remain separate milestones.

## Bounded cross-page application move boundary

`WorkspaceHomeItemPageMover` adds a narrow UI-facing adapter around the already-existing guarded `WorkspacePagedRoomMutationRepository.moveHomeItem` mutation.

The adapter:

- requires initialized terminal Room authority before reading page-placement state;
- requires nonblank and distinct source/target page identities and a nonblank application key;
- resolves exactly one APP item matching the source page and application key, rejecting ambiguous or missing state;
- refuses to proceed when any authoritative HOME item still has null grid coordinates;
- derives a deterministic grid envelope from the current persisted coordinates with a four-column minimum matching the rendered secondary grid;
- scans target-page placements in row-major order and selects the first collision-free cell preserving the source item's span; and
- delegates the final write to `moveHomeItem` rather than writing directly.

The preflight free-cell selection is intentionally not write authority. `moveHomeItem` re-reads the complete HOME pages/items, validates the complete multi-page grid, and calls `WorkspaceDao.replaceItemPlacementIfSnapshotMatches`. If a concurrent mutation changes the snapshot, if the chosen cell becomes invalid, or if any source/target identity no longer matches, the write fails closed.

This milestone does not expose arbitrary cell/span editing or cross-page drag/drop. It also does not yet route primary-page Favorites into the secondary-page move menu; the bounded UI is available from rendered secondary pages.

## Validation

Acceptance for this slice requires:

- Android file-backed Room tests proving page lifecycle and app-page moves are reserved before terminal Room authority;
- proof that an accepted page creation persists one empty page at the next authoritative rank;
- duplicate page IDs fail closed;
- the protected primary page cannot be deleted;
- populated pages fail with `PageNotEmpty` and retain their child items;
- an accepted empty-page deletion removes only the target page and compacts remaining ranks;
- existing child items survive subsequent page reordering;
- an accepted secondary-page app move chooses the first free target cell, preserves item identity, persists on the target page, and removes the source-page placement;
- same-page or missing-page app-move requests fail closed;
- existing privacy, manifest, Glaze UI, Room schema/cutover, lint, unit, assembly, and Android 16 instrumentation gates; and
- no promotion to production/Stable based on CI alone.

## Remaining boundaries

This milestone does not establish populated-page deletion, destructive recovery/undo, arbitrary live cell/span editing, cross-page drag/drop UI, primary-Favorite-to-secondary placement UI, production Room cutover acceptance, folders, shortcuts, widgets/AppWidgetHost, representative physical-device HOME acceptance, complete accessibility acceptance, signed release packaging, or Stable qualification.
