# Rendered Primary Home Migration Diagnostics — Development

This slice renders the existing primary Home grid-migration readiness presentation in a dedicated Android debug-only diagnostics activity.

The production `main` manifest is unchanged. `PrimaryHomeMigrationDiagnosticsActivity` exists only in the `debug` source set and is exposed as a separate debug launcher entry so Development builds can inspect the current evidence without changing the normal HOME surface.

The observation path waits for terminal Room workspace authority before reading `workspace_pages` and `workspace_items`. It evaluates only the canonical protected primary Home page (`home:0`) through the existing deterministic readiness evaluator and presenter. Missing Room access, a missing primary page, and read failures remain explicit non-favorable states.

The rendered surface can display only Waiting, Ready, Current / Not needed, Blocked, or Unknown evidence. It exposes no migration action. It cannot execute the migration planner, write Room, change item coordinates, change page rank, promote workspace authority, or otherwise perform migration.

This remains a Development diagnostic and does not establish migration execution acceptance, production migration UI, representative physical-device HOME acceptance, complete Glaze UI 2.1 consumer conformance, signed release packaging, or Stable qualification.
