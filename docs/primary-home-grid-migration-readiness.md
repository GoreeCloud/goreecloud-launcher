# Primary Home Grid Migration Readiness — Development

## Purpose

This slice exposes a read-only readiness projection over the existing deterministic primary Home grid migration planner.

## Readiness states

- `Ready`: the current canonical `home:0` compatibility projection can be mapped into the planner's deterministic grid, including item count and target grid dimensions.
- `NotNeededEmpty`: the canonical primary page has no items to migrate.
- `NotNeededAlreadySpatial`: the primary page is already in the exact deterministic spatial state accepted by the planner.
- `Blocked`: the primary page identity or item shape does not satisfy the migration planner's strict contract.

## Authority boundary

The evaluator performs no Room writes, schema changes, canonical-reader transition, workspace cutover, or UI action. It cannot promote or execute a migration. It exists so future diagnostics and migration review can consume a small, explicit state without duplicating planner logic or weakening the primary Home protection boundary.

Automated tests cover canonical readiness, empty/already-spatial states, and blocked invalid page/item shapes.
