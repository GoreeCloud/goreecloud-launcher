# GoreeCloud Launcher — Changelogs

**Record type:** Repository change history  
**Repository:** `GoreeCloud/launcher`  
**Lifecycle:** Development  
**Migration state:** **In progress on `migration/repository-feature-records`. This file is not yet a complete authoritative replacement for the legacy Drive changelog.**  
**Current source baseline:** `main` at `ec6640dda8522244d57a947db083aecb8b9cfe33` (PR #199).  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance, version 1.0, effective September 22, 2026.

## Migration control

This file is the candidate repository-local destination for the historical `GoreeCloud/Changelogs/Change Log — Launcher.docx` record.

The existing Drive changelog remains a **migration source only** until all meaningful historical entries are imported, compared, and verified here. It must not be deleted before that verification gate is satisfied. Historical entries will be preserved as historical facts even when later architecture, terminology, or lifecycle state differs.

The imported history must preserve, where available:

- event dates and chronological ordering;
- implementation and lifecycle state;
- pull-request, commit, CI, build, emulator, artifact, release, deployment, rollback, and verification evidence;
- limitations and unresolved acceptance boundaries;
- corrections and superseded interpretations; and
- material privacy, security, architecture, migration, documentation, and governance changes.

Until that import is complete and this candidate is merged and read back from the authoritative branch, no deletion of the legacy Drive changelog is authorized.

## Current migration reconciliation — September 22, 2026

### Repository feature/changelog governance migration started

**Change type:** Governance; documentation architecture; source-of-truth migration.

Migration work began under **Standard — Repository Feature Tracking and Changelog Governance v1.0**.

Actions established on migration branch `migration/repository-feature-records`:

- created candidate root-level `IMPLEMENTED-FEATURES.md`;
- created candidate root-level `PLANNED-FEATURES.md`;
- created candidate root-level `CHANGELOGS.md`;
- reconciled current Launcher feature state against repository `FEATURE-ROADMAP.md`, the Drive `FEATURE-ROADMAP.docx`, current `FEATURES.md`, and live GitHub evidence;
- identified the Drive roadmap as stale relative to newer repository implementation evidence;
- preserved the former Drive-synchronization obligation as a superseded migration disposition instead of silently deleting it; and
- retained both the repository `FEATURE-ROADMAP.md` and Drive roadmap/changelog sources because the deletion gates are not yet satisfied.

**Verification boundary:** This migration is in progress. The new records do not become authoritative merely because they exist on a migration branch. Repository default-branch merge, readback, content-completeness verification, reference reconciliation, and Drive-source deletion gates remain pending.

## September 22, 2026 — PR #199 persisted Universal Search provider preferences

**Change type:** Universal Search; local persistence; privacy controls; Development implementation.

PR #199, **Persist Universal Search provider preferences**, was merged to `main` as `ec6640dda8522244d57a947db083aecb8b9cfe33`.

Implemented:

- added `LauncherSearchProviderPreferencesRepository` backed by a dedicated Launcher-owned Preferences DataStore;
- persisted only the versioned provider enable/order snapshot introduced by PR #198;
- preserved the semantic distinction between no stored provider preference and an explicitly stored empty enabled-provider set;
- surfaced malformed or unsupported persisted values through the existing fail-closed decode result instead of silently manufacturing defaults;
- exposed explicit `read`, `set`, and `clear` boundaries; and
- added focused JVM coverage for absent-vs-explicit-empty behavior, enable/order round trips, malformed persisted data, and the single-key storage boundary.

Privacy/recovery boundary:

- no typed queries, results, history, usage/frequency signals, credentials, authorization grants, or provider payloads are stored by this persistence layer;
- no networking, external provider discovery/invocation, Android permission, telemetry, or cross-profile authority was added; and
- the provider-control DataStore remains deliberately separate from the strict seven-field portable-preference v1 backup/recovery contract.

Validation:

- exact PR head `5646ce68d998ec96797d29a8c470df96c6ceac57` passed Android CI run #620 / `35710385034` across validation, build, unit/schema checks, Development APK staging, Android 16 transition-performance emulator, and Android 16 Room/runtime emulator lanes;
- merge commit: `ec6640dda8522244d57a947db083aecb8b9cfe33`.

Lifecycle boundary: Development only. Rendered provider management, provider-specific consent, explicit `Search with` invocation, external provider discovery/registration, portable-backup adoption/migration, representative-device Search acceptance, release, production, and Stable gates remain open.

## September 22, 2026 — PR #198 added versioned provider preference serialization

**Change type:** Universal Search; provider controls; serialization; privacy architecture; Development implementation.

PR #198 established the versioned, fail-closed serialization contract used by later provider-control persistence.

Implemented:

- versioned serialization of provider enablement and explicit provider order;
- preservation of absent-versus-explicit-empty semantics; and
- exclusion of typed queries, results, history, usage/frequency signals, credentials, authorization grants, and provider payloads from the serialized control boundary.

Validation:

- exact PR head `74cf28e2cc989e3e88d3cdd3e252dd69be45a71e` passed Android CI run #618 / `35707597053` across validation, build, unit/schema checks, Development APK staging, Android 16 Room/runtime, and Android 16 transition-performance emulator lanes;
- guarded merge commit: `d2600bc3f0b2fce6d3c8d524a8aef536e43cd1cb`;
- exact merged `main` then passed Android CI run #619 / `35708430142`.

Lifecycle boundary at the time of PR #198: the serialization contract did not yet write its payload to DataStore or portable backup/recovery state. PR #199 subsequently added Launcher-local DataStore persistence, while portable backup/recovery adoption remains open.

## Historical import status

The legacy Drive changelog contains the earlier Launcher chronology beginning with project establishment and native Android architecture approval on August 21, 2026 and continuing through the subsequent repository, CI, workspace, Glaze UI, search, profile, privacy, and stabilization work.

**Status:** Historical import is still pending on this migration branch. The source document has been inventoried and preserved. The migration must not be declared complete and the Drive source must not be deleted until the historical content has been transferred and compared for completeness.

## Changelog maintenance rule

After this migration is accepted, meaningful Launcher changes must be recorded in this repository-local `CHANGELOGS.md`. Google Drive must not receive a synchronized, mirrored, backup, convenience, or canonical changelog copy.

A repository commit, pull request, CI run, or artifact alone is not proof of production deployment or runtime acceptance. Each entry must describe the evidence-backed lifecycle state actually established.