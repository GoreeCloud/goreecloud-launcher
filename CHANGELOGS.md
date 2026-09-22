# GoreeCloud Launcher — Changelogs

**Record type:** Repository change history  
**Repository:** `GoreeCloud/launcher`  
**Lifecycle:** Development  
**Migration state:** **Authoritative on `main` after PR #201 merged as `009371938ac3cab041cfb0893ede68e66e211a4f` and default-branch readback verified this record and its imported history. Drive-source retirement remains the final migration cleanup gate.**  
**Repository authority baseline:** `main` at `009371938ac3cab041cfb0893ede68e66e211a4f` (PR #201). Latest source-bearing Launcher runtime remains `ec6640dda8522244d57a947db083aecb8b9cfe33` (PR #199).  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance, version 1.0, effective September 22, 2026.

## Migration control

This file is the authoritative repository-local human-readable change history for GoreeCloud Launcher. The historical Drive migration source was `GoreeCloud/Changelogs/Change Log — Launcher.docx`.

The legacy Drive changelog was migrated into seven linked repository-local historical segments covering the retained chronology from August 21 through September 22, 2026. The migration is a normalized evidence-preserving Markdown import, not a byte-for-byte transcription. Event dates, material implementation state, PR/commit/CI/artifact evidence, lifecycle boundaries, corrections, and material architecture/privacy/security/governance context were preserved where available. Obsolete Drive-as-canonical maintenance instructions were not carried forward as current authority.

Historical entries preserve their contemporaneous claims. Later architecture, terminology, or lifecycle state does not rewrite what an earlier entry established at its exact revision.

The Drive source is now **migration-source-only** pending its verified deletion under the final retirement gate. Its temporary continued existence does not create parallel authority; this repository-local record is authoritative after PR #201 merge and default-branch readback.

## Imported historical chronology

The migrated historical record is stored in these repository-local segments:

1. [August 21–22, 2026 — project establishment through Room mirror verification](docs/changelog-history/2026-08-21-to-2026-08-22-a.md)
2. [August 22, 2026 — Room runtime, authority, reconciliation, and promotion rehearsal](docs/changelog-history/2026-08-22-b.md)
3. [August 22–24, 2026 — production authority activation through grid-placement foundation](docs/changelog-history/2026-08-22-to-2026-08-24.md)
4. [August 24–31, 2026 — Glaze adoption, multi-page workspace, placement, and page-navigation work](docs/changelog-history/2026-08-24-to-2026-08-31.md)
5. [August 31–September 2, 2026 — primary Home protections, beta shell, branding, layout lock, and Theme Manager](docs/changelog-history/2026-08-31-to-2026-09-02-a.md)
6. [September 2–9, 2026 — accessibility, portability, HOME stabilization, privacy, and local-first Search](docs/changelog-history/2026-09-02-to-2026-09-09.md)
7. [September 16–22, 2026 — Glaze/Platform stabilization through Universal Search presentation structure](docs/changelog-history/2026-09-16-to-2026-09-22.md)

The source parser identified 71 meaningful dated or titled historical sections/entries in the legacy changelog material. Those sections were accounted for through the seven normalized segments, including historical roadmap-synchronization events as provenance rather than current governance. PR #198 and PR #199, which post-date or extend the imported retained chronology, are recorded directly below.

## September 22, 2026 — PR #201 established repository-native feature and changelog authority

**Change type:** Governance; documentation architecture; source-of-truth migration.

PR #201, **Migrate feature tracking and changelog governance**, completed the repository-side migration required by **Standard — Repository Feature Tracking and Changelog Governance v1.0**.

Implemented and reconciled:

- added root-level `IMPLEMENTED-FEATURES.md`, `PLANNED-FEATURES.md`, and `CHANGELOGS.md`;
- migrated and dispositioned legacy roadmap obligations without promoting partial work to complete status;
- imported the meaningful historical Launcher changelog chronology into the seven repository-local history segments above;
- classified the former Drive-synchronization obligation as superseded rather than silently dropping it;
- updated README authority to the repository-native records and prohibited feature/changelog synchronization back to Drive;
- recorded the governance supersession on issue #80 without rewriting its historical evidence; and
- retired root `FEATURE-ROADMAP.md` from authoritative `main` after its replacement records were verified on the migration branch.

Validation and promotion:

- exact PR head `bb9f5f7d2a91b771875b2aa4222d98b012bb7bda` passed Android CI run #635 / `35719798536` across validate/build/unit/schema/APK staging, Android 16 Room/runtime emulator, and Android 16 transition-performance emulator lanes;
- PR #201 was squash-merged to `main` as `009371938ac3cab041cfb0893ede68e66e211a4f`;
- default-branch readback verified the three required root records and imported changelog history; and
- `FEATURE-ROADMAP.md` is retired from the reviewed migration result.

**Remaining migration cleanup:** The former Drive `FEATURE-ROADMAP.docx` and `Change Log — Launcher.docx` records remain migration-source-only until their deletion and removal are verified. Their continued temporary presence does not supersede repository authority.

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

- exact PR head `74cf28e2cc989e3e88d3cdd3e252dd69be45a71e` passed Android CI run #618 / `35707597053` across validate/build/unit/schema/APK staging, Android 16 Room/runtime, and Android 16 transition-performance emulator lanes;
- guarded merge commit: `d2600bc3f0b2fce6d3c8d524a8aef536e43cd1cb`;
- exact merged `main` then passed Android CI run #619 / `35708430142`.

Lifecycle boundary at the time of PR #198: the serialization contract did not yet write its payload to DataStore or portable backup/recovery state. PR #199 subsequently added Launcher-local DataStore persistence, while portable backup/recovery adoption remains open.

## Historical integrity rule

Historical sections may retain terminology, authority assumptions, or governance practices that were true for their exact revision but later superseded. For example, older entries referring to Drive roadmap synchronization or GoreeCloud Index-oriented Search authority remain historical provenance; they do not override the current repository-native feature/changelog standard or the current Launcher-owned Universal Search architecture.

Corrections must be additive and traceable. Do not silently rewrite older evidence to resemble current state.

## Changelog maintenance rule

Meaningful Launcher changes must be recorded in this repository-local `CHANGELOGS.md`, with supporting history under `docs/changelog-history/` when needed for volume or historical preservation. Google Drive must not receive a synchronized, mirrored, backup, convenience, or canonical changelog copy.

A repository commit, pull request, CI run, or artifact alone is not proof of production deployment or runtime acceptance. Each entry must describe the evidence-backed lifecycle state actually established.
