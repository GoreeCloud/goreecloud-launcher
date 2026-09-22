# GoreeCloud Launcher — Changelogs

**Record type:** Repository change history  
**Repository:** `GoreeCloud/launcher`  
**Lifecycle:** Development  
**Migration state:** **Authoritative on `main` after PR #201 merged as `009371938ac3cab041cfb0893ede68e66e211a4f` and default-branch readback verified this record and its imported history. PR #203 reconciled the post-migration authority records, and legacy Launcher Drive roadmap/changelog retirement was subsequently verified.**  
**Repository authority baseline:** `main` at `439943d7918e4d04e9f7bf62707dc55d4a2898cd` (PR #205). Latest source-bearing Launcher runtime is the same commit.  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance, version 1.0, effective September 22, 2026.

## Migration control

This file is the authoritative repository-local human-readable change history for GoreeCloud Launcher. The retired historical Drive migration source was `GoreeCloud/Changelogs/Change Log — Launcher.docx`.

The legacy Drive changelog was migrated into seven linked repository-local historical segments covering the retained chronology from August 21 through September 22, 2026. The migration is a normalized evidence-preserving Markdown import, not a byte-for-byte transcription. Event dates, material implementation state, PR/commit/CI/artifact evidence, lifecycle boundaries, corrections, and material architecture/privacy/security/governance context were preserved where available. Obsolete Drive-as-canonical maintenance instructions were not carried forward as current authority.

Historical entries preserve their contemporaneous claims. Later architecture, terminology, or lifecycle state does not rewrite what an earlier entry established at its exact revision.

After PR #203 reconciled the repository-native records and authoritative `main` readback was complete, the legacy Launcher Drive changelog and roadmap files were deleted. Both former file IDs now return not found, no Launcher changelog remains in the GoreeCloud Changelogs folder, and the dedicated `Feature Roadmap/GoreeCloud Launcher` folder is empty. Git history and the repository-local records are now the durable feature/changelog recovery and authority path.

## Imported historical chronology

The migrated historical record is stored in these repository-local segments:

1. [August 21–22, 2026 — project establishment through Room mirror verification](docs/changelog-history/2026-08-21-to-2026-08-22-a.md)
2. [August 22, 2026 — Room runtime, authority, reconciliation, and promotion rehearsal](docs/changelog-history/2026-08-22-b.md)
3. [August 22–24, 2026 — production authority activation through grid-placement foundation](docs/changelog-history/2026-08-22-to-2026-08-24.md)
4. [August 24–31, 2026 — Glaze adoption, multi-page workspace, placement, and page-navigation work](docs/changelog-history/2026-08-24-to-2026-08-31.md)
5. [August 31–September 2, 2026 — primary Home protections, beta shell, branding, layout lock, and Theme Manager](docs/changelog-history/2026-08-31-to-2026-09-02-a.md)
6. [September 2–9, 2026 — accessibility, portability, HOME stabilization, privacy, and local-first Search](docs/changelog-history/2026-09-02-to-2026-09-09.md)
7. [September 16–22, 2026 — Glaze/Platform stabilization through Universal Search presentation structure](docs/changelog-history/2026-09-16-to-2026-09-22.md)

The source parser identified 71 meaningful dated or titled historical sections/entries in the legacy changelog material. Those sections were accounted for through the seven normalized segments, including historical roadmap-synchronization events as provenance rather than current governance. PR #198 and later source/governance changes that extend the imported retained chronology are recorded directly below.

## September 22, 2026 — PR #205 cancelled superseded Launcher icon preload work

**Change type:** Performance stabilization; application inventory; icon caching; Development implementation.

PR #205, **Cancel superseded Launcher icon preload work**, was guarded-squash merged to `main` as `439943d7918e4d04e9f7bf62707dc55d4a2898cd`.

Implemented:

- added a single replaceable background preload runner for Launcher icon warming;
- a new authoritative `LauncherApps` inventory warm request now cancels the superseded preload tail before it can continue scheduling lower-priority icon loads;
- complete icon-cache/profile-topology invalidation now cancels background preload work before the cache generation is advanced and entries are evicted;
- preserved the existing 12 MiB LRU cache, 128-candidate warm bound, batches-of-three parallelism, package/profile invalidation stamps, single-flight decode sharing, LauncherApps inventory authority, and visible-icon lazy fallback;
- preserved safe reuse of already-started single-flight decodes by newer callers while stale stamp checks continue to prevent invalidated results from entering the cache;
- added focused JVM regression coverage proving replacement preload work cancels the superseded tail; and
- added the directly affected icon-cache source and regression test to `SOURCE_MANIFEST.txt`.

Validation:

- exact PR head `f6696f4933b073e355c08e3871bd1f7365b4ceac` passed Android CI run #643 / `35758277303` across validate/build/unit/schema/APK staging, Android 16 Room/runtime, and Android 16 transition-performance emulator lanes;
- guarded squash merge commit: `439943d7918e4d04e9f7bf62707dc55d4a2898cd`;
- exact merged `main` passed push Android CI run #644 / `35759394496` across the same configured validation and Android 16 emulator lanes.

**Lifecycle boundary:** Development only. This tranche reduces superseded background preload scheduling but does not itself establish representative physical-device/default-HOME latency, jank, memory, power, package/profile churn acceptance, Quickstep/Recents compatibility, Release Candidate, production, or Stable qualification. Issue #80 remains open.

## September 22, 2026 — PR #203 finalized repository authority and Drive retirement was verified

**Change type:** Governance; migration completion; source-of-truth retirement; documentation correction.

PR #203, **Finalize repository record authority after migration**, reconciled the three repository-native governance records against the verified post-PR #201 state before Drive retirement.

Repository reconciliation:

- confirmed `IMPLEMENTED-FEATURES.md`, `PLANNED-FEATURES.md`, and `CHANGELOGS.md` as authoritative repository records after PR #201 merge/readback;
- distinguished repository authority state from the latest source-bearing Launcher runtime so the documentation-only migration could not be mistaken for a runtime promotion;
- preserved Launcher **Development** lifecycle status and all remaining acceptance gates; and
- recorded PR #201 as the repository-side migration event without rewriting historical evidence.

Validation and promotion:

- exact PR #203 head `3bc23b85629b51cef1a7762a93d6191907e59f3a` passed Android CI run #638 / `35721409203` across the repository's validate/build/unit/schema/APK-staging and Android 16 emulator lanes;
- PR #203 merged to `main` as `25d53b5b213aa6ddaf99bb09e4ec9edeebbec7e9`;
- authoritative readback confirmed the three repository-native records, README navigation, seven imported history segments, and absence of root `FEATURE-ROADMAP.md`.

Drive retirement verification after authoritative readback:

- legacy `GoreeCloud/Changelogs/Change Log — Launcher.docx` file ID `1NInGthOUuofym6TbA1ffAVONT0_BRG3i` returns not found;
- legacy `GoreeCloud/Feature Roadmap/GoreeCloud Launcher/FEATURE-ROADMAP.docx` file ID `1Y9eFLv1583ffP1k3ra_smZZ0UpFMfRau` returns not found;
- no remaining Launcher-named changelog exists in the canonical GoreeCloud Changelogs folder; and
- the dedicated `GoreeCloud Launcher` feature-roadmap folder remains present but empty pending the broader estate-wide retirement of obsolete roadmap/changelog directory structures.

**Lifecycle boundary:** This completed the Launcher repository-native feature/changelog migration only. It did not promote Launcher beyond Development and did not complete the broader GoreeCloud estate migration.

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

**Remaining migration cleanup at that point:** The former Drive `FEATURE-ROADMAP.docx` and `Change Log — Launcher.docx` records still required deletion and removal verification after repository authority was established. PR #203 and the subsequent Drive audit completed that cleanup; this sentence preserves the PR #201 contemporaneous boundary rather than implying the files had already been retired at that earlier stage.

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