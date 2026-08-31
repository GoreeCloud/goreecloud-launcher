# Primary Home Grid Migration Planning — Development

## Purpose

This Development slice establishes a pure planning contract for a future migration of the protected primary HOME compatibility projection into explicit grid coordinates. It does not perform that migration.

The current authoritative primary page remains `home:0`, rank 0, and continues to represent ordered Favorites through the accepted compatibility model. Canonical primary application rows intentionally retain null `cellX` and `cellY` values. Existing secondary pages remain the only active spatial editing domain.

## Deterministic planning contract

`WorkspacePrimaryHomeGridMigrationPlanner` accepts only the exact current primary compatibility shape:

- page identity must be `home:0`;
- the page must be a HOME container at rank 0;
- primary application ranks must be contiguous from zero;
- every row must be a canonical legacy primary APP item with a nonblank application key and matching `legacy:home:<appKey>` identity;
- spans must remain `1x1` for this compatibility migration foundation;
- item identities and application keys must be unique; and
- coordinates must either all be null, or all already match the deterministic target exactly.

For a canonical null-coordinate primary page, ranks are mapped row-major into the current four-column primary Home presentation:

- `cellX = rank % 4`
- `cellY = rank / 4`
- grid rows are the minimum number needed to contain the ordered Favorites, with at least one row.

The resulting candidate placements are validated through the existing framework-independent `WorkspaceGridPlacement` contract before a plan is returned.

An empty canonical primary page returns `Empty`. A primary page that already has the exact deterministic coordinates returns `AlreadySpatial`. Mixed null/spatial coordinates, half-null coordinates, changed identities, non-contiguous ranks, noncanonical spans, or spatial coordinates that differ from the deterministic plan fail closed as invalid input.

## Authority boundary

This planner is intentionally pure. It:

- does not open or write Room;
- does not change `WorkspaceLegacyImportMapper`;
- does not change `WorkspaceCanonicalRoomPlacementReader` or the current canonical compatibility definition;
- does not change workspace authority state;
- does not change `WorkspaceProductionRuntimeCoordinator`;
- does not expose a new Home UI action;
- does not alter page ordering or cross-page movement policy; and
- does not change the Room schema.

Therefore, the accepted primary compatibility projection remains null-coordinate and protected at rank 0 even when a valid migration plan can be derived.

## Required later activation gate

A later, separately reviewed migration-application slice is required before primary spatial editing can become authoritative. That gate must define and validate, at minimum:

- the exact transactional compare-and-set/snapshot boundary for converting the current primary rows;
- the canonical-reader transition from null-coordinate compatibility rows to accepted spatial primary rows;
- startup, recreation, recovery, and concurrency behavior during and after the transition;
- compatibility with current Favorite/Dock mutation semantics;
- preservation of primary rank-zero protection unless a later product decision explicitly changes it;
- Android 16 file-backed runtime evidence for the real migration transaction; and
- user-facing movement/editing only after the migration is authoritative and healthy.

## Acceptance boundary

This is planning evidence only. It does not implement primary-to-secondary or secondary-to-primary movement, primary drag/drop, arbitrary coordinate editing, folders, shortcuts, widgets, physical-device acceptance, signed release packaging, production release acceptance, or Stable qualification.

No Android permission, `INTERNET` permission, network behavior, dependency, telemetry, analytics, advertising, sponsorship, attribution, tracking capability, or Room schema change is introduced.
