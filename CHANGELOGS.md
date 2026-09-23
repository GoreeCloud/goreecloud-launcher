# GoreeCloud Launcher — Changelogs

**Record type:** Repository change history  
**Repository:** `GoreeCloud/launcher`  
**Lifecycle:** Development  
**Migration state:** **Authoritative on `main` after PR #201 merged as `009371938ac3cab041cfb0893ede68e66e211a4f` and default-branch readback verified this record and its imported history. PR #203 reconciled the post-migration authority records, and legacy Launcher Drive roadmap/changelog retirement was subsequently verified.**  
**Repository authority baseline:** `main` at `b68d1e56443a6e8365cf1d440e4ac5c53c868a02` (PR #235). Latest source-bearing Launcher runtime is the same commit.  
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

## September 23, 2026 — PR #235 reserved Launcher Settings for Edit Home

**Change type:** Settings navigation; Home gesture authority; compatibility routing; Development implementation.

PR #235, **Keep Launcher Settings in the Home editor**, was guarded-squash merged to `main` as `b68d1e56443a6e8365cf1d440e4ac5c53c868a02`.

Implemented:

- removed the Home quick-action **Customize** shortcut that directly opened Launcher Settings while retaining Apps and Search quick actions;
- reserved empty-space Home long-press for **Edit Home** so the Settings entry path cannot be remapped away;
- removed Tap and hold from the configurable gesture list and presents it as the read-only **Edit Home** action;
- removed direct **Launcher settings** from the configurable gesture picker;
- retained historical stored `LAUNCHER_SETTINGS` and `TAP_AND_HOLD` values for compatibility while routing legacy direct-Settings gestures to Edit Home and ignoring stored tap-and-hold remaps for the reserved long-press behavior;
- routes stale/internal Universal Search Settings destinations to Edit Home rather than directly entering Settings; and
- leaves the Launcher-owned Settings surface reachable through **long-press empty Home → Edit Home → Settings**.

Validation:

- candidate head `6447ee1ddfbfa3c5983512bfb8a958badb1e13a0` passed source/build validation but its Android 16 runtime lane exposed an overly strict test assertion that expected one Settings semantics node where the Home editor rendered two matches; the same run also contained a separate Compose-hierarchy emulator flake;
- no runtime implementation change was required for that assertion issue; final exact head `75581e01cdfb584b5f4af59377002ca835d0b064` relaxed only the test assertion;
- final Android CI run #723 / `35820108208` passed validate/build/unit/schema/APK staging, Android 16 Room/runtime emulator, and Android 16 transition-performance emulator lanes; and
- guarded squash merge commit: `b68d1e56443a6e8365cf1d440e4ac5c53c868a02`.

**Lifecycle boundary:** Development only. Representative physical-device long-press discoverability, gesture ergonomics, accessibility, one-handed behavior, sustained performance, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #229 added opt-in local Universal Search sources

**Change type:** Universal Search; local sensitive sources; application shortcuts; permission gating; Development implementation.

PR #229, **Add opt-in local Universal Search sources on current main**, was guarded-squash merged to `main` as `0d6ccf955ae0884d4b06d9ed1839b377ab2a1af2`.

Implemented:

- added Android application shortcuts as a local/default Launcher Search source and launches them through `LauncherApps.startShortcut`;
- added Contacts, Call history, and Messages as explicit opt-in local Search sources;
- keeps those sensitive sources disabled by default and requests the matching Android permission only when the user enables the source;
- persists source enablement only after permission grant;
- separates automatic-local, opt-in-local, and explicit-handoff provider modes;
- opens contact/dialer/messaging results through Android intent actions;
- removed the direct **Launcher settings** Universal Search result so Settings remains behind the Home editor policy; and
- preserves local query processing with no INTERNET permission, telemetry, query-history persistence, or automatic third-party query fan-out.

Permissions/trust boundary:

- added only `READ_CONTACTS`, `READ_CALL_LOG`, and `READ_SMS` for their explicitly enabled local sources;
- telephony hardware is declared optional; and
- Android/Play distribution-policy eligibility for sensitive permissions remains a separate release obligation.

Validation:

- exact PR head `740f967339de2adf0a4d70c26c9aeef2c2ecff7e` passed Android CI run #714 / `35818607895`;
- guarded squash merge commit: `0d6ccf955ae0884d4b06d9ed1839b377ab2a1af2`.

**Lifecycle boundary:** Development only. Representative-device permission behavior, profile isolation, accessibility, latency, distribution-policy review, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #228 connected persisted Universal Search source controls

**Change type:** Universal Search provider controls; rendered Sources management; Development implementation.

PR #228, **Connect persisted Universal Search source controls on current main**, was guarded-squash merged to `main` as `1e1f72c6a994e8381c0eecf3bd9fc3db17a44dde`.

Implemented:

- loads the existing versioned Search-provider preference store into activity-owned runtime state;
- keeps automatic Search providers disabled until persisted control state has loaded;
- adds an isolated Glaze Search surface with a **Sources** view for provider identity, enable/disable state, privacy summary, deterministic Earlier/Later ordering, and safe-default reset;
- limits automatic execution to providers classified by the existing privacy policy as automatic-local; and
- continues to exclude typed queries, results, history, credentials, usage signals, and provider payloads from the persisted provider-control store.

Validation:

- exact PR head `851bf9007ea57209981b61c1ec11bc746ad0a009` passed Android CI run #703 / `35816326844`;
- guarded squash merge commit: `1e1f72c6a994e8381c0eecf3bd9fc3db17a44dde`.

**Lifecycle boundary:** Development only. Portable provider-control recovery, file/connected-source Search, provider-specific external consent/handoff, representative-device Search latency/accessibility/profile behavior, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #226 added four Launcher-owned Glaze wallpapers

**Change type:** Wallpaper personalization; local rendering; Android system integration; Development implementation.

PR #226, **Restack four built-in GoreeCloud wallpapers on current main**, was guarded-squash merged to `main` as `29badaae9e4b1ce0c6a697501ed5cc969cc79eaa`.

Implemented:

- added four Launcher-owned Glaze wallpaper designs: Glaze Aurora, Glaze Horizon, Glaze Nocturne, and Glaze Cascade;
- renders the wallpaper images locally from inspectable GoreeCloud source instead of downloading artwork;
- reuses the existing Home long-press **Wallpaper** action and Universal Search Wallpaper action;
- presents the four built-ins with names/descriptions and retains an explicit Android system-wallpaper-picker fallback;
- applies selected built-ins through Android `WallpaperManager`; and
- added focused catalog coverage and source-manifest tracking.

Privacy/security boundary:

- added only `android.permission.SET_WALLPAPER`, guarded by the repository manifest allowlist;
- no INTERNET, storage, advertising, analytics, remote asset, or executable remote-code dependency was added.

Validation:

- exact PR head `3d535cfee09a6995790cbc46371f7a55f8d4cdd8` passed Android CI run #698 / `35815073478` across validate, Android 16 Room/runtime emulator, and Android 16 transition-performance emulator lanes;
- guarded squash merge commit: `29badaae9e4b1ce0c6a697501ed5cc969cc79eaa`.

**Lifecycle boundary:** Development only. Representative-device visual quality, resolution/orientation rendering, picker accessibility, sustained performance/power, Human Visual Excellence, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #223 removed persistent app-drawer header actions

**Change type:** App drawer interaction; navigation cleanup; Development implementation.

PR #223, **Use gesture-only app drawer dismissal**, was guarded-squash merged to `main` as `c09e2d581f300271f762422eef206631769f73b1`.

Implemented:

- removed the persistent Settings action from the app drawer header;
- removed the explicit app drawer close action;
- retained profile/layout context in the drawer header without a right-side action cluster;
- preserved downward swipe as the drawer's explicit in-surface dismissal gesture;
- preserved Android HOME-button return as normal system navigation rather than a rendered drawer control;
- retained Launcher Settings in the Home long-press editor sheet through its dedicated **Settings** action; and
- added a stable `launcher-app-drawer` test tag plus Android runtime coverage for the revised drawer interaction.

Validation:

- initial candidate head `5a908d8b5f0801cbe706cb297e1f50ab7eb13107` passed source/build and transition-performance validation but failed the Room/runtime lane because the newly added test injected dismissal through a parent semantics node; that failed head remains audit provenance and no source acceptance was taken from it;
- final exact PR head `2fe8ed2046daca56202ef07873a806a9b09a7c30` passed Android CI run #693 / `35813157287` across validate/build/unit/schema/APK staging, Android 16 Room/runtime emulator, and Android 16 transition-performance emulator lanes; and
- guarded squash merge commit: `c09e2d581f300271f762422eef206631769f73b1`.

**Lifecycle boundary:** Development only. Representative physical-device/default-HOME gesture ergonomics, accessibility, one-handed behavior, sustained performance, profile behavior, Quickstep/Recents, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #221 established the 5×6 starter Home and five-app Dock defaults

**Change type:** Home defaults; Dock defaults; local-only usage ranking; install-to-Home behavior; Development implementation.

PR #221, **Restack 5x6 starter Home and five-app Dock defaults**, was guarded-squash merged to `main` as `384568dda7abcf0a7e2c0942773efa540b70af2c`.

Implemented:

- changed a new Launcher preference store's default Home grid to 5 × 6 while retaining supported configurable grid presets;
- expanded the one-time starter Dock policy to prefer Phone, Messages, Email/Mail, Browser, and Camera when matching launchable applications are available;
- expanded the starter Home policy to up to 10 applications and places them in the bottom two Home rows directly above the Dock after Room spatial activation;
- added minimal Launcher-local aggregate launch-count ranking for starter suggestions when such local history already exists, with deterministic common/GoreeCloud role fallback when it does not;
- added Settings controls to disable local usage-based suggestions, clear the local aggregate counts, and enable **Add new apps to Home**;
- kept automatic new-app Home placement disabled by default;
- added a persisted local primary-profile launchable-app inventory baseline so installs that occur while the Launcher process is not running can be detected on the next inventory refresh;
- made the first inventory observation initialization-only so enabling the feature never treats the whole existing application inventory as newly installed; and
- preserved user layout authority after the one-time starter rather than continuously re-ranking or reshuffling Home.

Privacy/trust boundary:

- no Android Usage Access, manifest package receiver, new Android permission, INTERNET authority, analytics SDK, or remote ranking service was added;
- the local usage store keeps only an application workspace key and aggregate Launcher launch count, with no timestamps, dwell time, search queries, destinations, or network data; and
- the install baseline stores only currently visible primary-profile launchable-application workspace keys needed for local new-install detection.

Validation:

- exact PR head `d464224bbd8d975dcad8e6320a0adf117bafa318` passed Android CI run #688 / `35811191027`, including repository/privacy/identity/GLAZE/Room-cutover guards, lint/build/unit/schema checks, Development APK staging, Android 16 Room/runtime emulator, and Android 16 transition-performance emulator lanes;
- guarded squash merge commit: `384568dda7abcf0a7e2c0942773efa540b70af2c`.

**Lifecycle boundary:** Development only. Representative-device first-install behavior, package-inventory timing, accessibility, performance, work/private-profile automatic-placement policy, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #219 added GoreeCloud and Android Home widgets

**Change type:** Home widgets; AppWidgetHost; Room workspace; Development implementation.

PR #219, **Restack GoreeCloud and Android Home widgets**, was guarded-squash merged to `main` as `42513fb80ec2caea8431437664a96abb1a605f1f`.

Implemented:

- added Launcher-owned GoreeCloud Clock and Launcher Status Home widgets;
- added Android third-party widget selection through the platform AppWidget picker and provider configuration activities;
- added AppWidgetHost/AppWidgetHostView lifecycle handling, including host-ID cleanup after canceled/failed selection and successful removal;
- persisted widget identity/provider binding, Home cells, and spans in the Room workspace;
- added span-aware primary-Home rendering, resize/remove controls, and widget-covered-cell exclusion from application drop targets;
- preserved widget rows across ordinary Home/Dock application placement writes while keeping the legacy favorites compatibility projection application-only; and
- added focused JVM and Android runtime regression coverage for widget key/spatial validation, paged rendering, authoritative compatibility reads, and widget preservation across application placement writes.

Privacy/trust boundary:

- no privileged `BIND_APPWIDGET` authority, network permission, advertising, analytics, or remote widget service was added by Launcher;
- Android widget provider behavior remains governed by the selected provider; and
- portable widget backup/restore is not claimed because Android `appWidgetId` bindings are not portable across restore targets.

Validation:

- exact PR head `c188884d22a5a90bed9074a4695ecfb2a30c99a8` passed Android CI run #679 / `35809355554`;
- guarded squash merge commit: `42513fb80ec2caea8431437664a96abb1a605f1f`.

**Lifecycle boundary:** Development only. Representative-device widget picker/configuration/provider behavior, accessibility, rotation/form-factor behavior, performance, portable widget recovery, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #215 unified Home, Dock, and App Drawer drag/edit interactions

**Change type:** Workspace interaction; drag/drop; edit mode; Development implementation.

PR #215, **Restack unified Launcher drag and edit interactions**, was guarded-squash merged to `main` as `12634e7388f6c997debb255874136f80cc720830`.

Implemented:

- added long-press edit mode with visible Home grid/edit affordances;
- added Home-to-Dock and Dock-to-Home drag movement;
- added Dock reordering and primary-Home cell reordering by drag;
- added App Drawer copy-to-Home and copy-to-Dock placement without removing the application from Drawer inventory;
- hid icon context actions during active drag;
- added Home rename and Android App info alongside existing uninstall and placement controls;
- preserved layout lock, Home capacity, and five-item Dock limits; and
- retained accessible non-drag placement/order controls.

Validation:

- exact PR head `c5ec82a5850b75d3442b5a457a8eac9d0ec44d7d` passed Android CI run #671 / `35803617800`;
- guarded squash merge commit: `12634e7388f6c997debb255874136f80cc720830`.

**Lifecycle boundary:** Development only. Representative-device drag ergonomics, accessibility, sustained performance, release qualification, production, and Stable acceptance remain open under issue #80.

## September 22, 2026 — PR #212 reconciled the Launcher source manifest

**Change type:** Repository integrity; source tracking; Development governance.

PR #212, **Reconcile Launcher source manifest**, was guarded-squash merged to `main` as `53e79befaf7f76b1abf27acecf8a3c838515f34b`.

Implemented:

- reconciled `SOURCE_MANIFEST.txt` with the authoritative tracked repository blobs after earlier source growth; and
- restored exact source-manifest parity required by Launcher validation before subsequent interaction/widget/default-layout tranches were integrated.

Validation:

- exact PR head `d333cfb69a3064bad2cded99b29af28a2576f496` passed Android CI run #660 / `35802293870`;
- post-merge readback verified 213 manifest entries matched the 213 intended tracked blobs at that checkpoint.

**Lifecycle boundary:** This was repository-integrity work only and did not change the Launcher Development lifecycle or release-acceptance state.

## September 22, 2026 — PR #207 stabilized persisted Universal Search provider controls

**Change type:** Universal Search; privacy controls; local persistence reconciliation; Development implementation.

PR #207, **Fail closed on unreadable Universal Search provider preferences**, was guarded-squash merged to `main` as `0af5d6753d1dca98de432f24f8703fe5bae85e2c`.

Implemented:

- added policy-level reconciliation from the persisted provider-preference decode result into executable provider-control state;
- preserved privacy-safe automatic-local defaults only when provider-control storage is genuinely absent;
- preserved explicit loaded enablement and provider order;
- made malformed or unsupported persisted state fail closed to no enabled automatic providers instead of silently restoring defaults;
- added bounded provider enable/disable and ordering mutation helpers that emit the existing versioned provider-control snapshot;
- ignored stale or unknown provider IDs during mutation and preserved enablement independently from ordering; and
- added focused JVM regression coverage for absent, loaded, invalid, unsupported, stale-ID, enable/disable, and ordering semantics.

Privacy/trust boundary:

- no typed queries, results, history, usage/frequency signals, credentials, authorization grants, or provider payloads were added to persistence;
- no networking, external provider discovery/invocation, Android permission, telemetry, or cross-profile authority was added; and
- rendered provider management, runtime Compose/DataStore collection, external handoff execution, provider-specific consent, and portable backup adoption remain separate open work.

Validation:

- exact PR head `f823ab9c1cb406116b68fe1f7c20cefef1ad6575` passed Android CI run #647 / `35794625569` across validate/build/unit/schema/APK staging, Android 16 Room/runtime emulator, and Android 16 transition-performance emulator lanes;
- guarded squash merge commit: `0af5d6753d1dca98de432f24f8703fe5bae85e2c`.

**Lifecycle boundary:** Development only. This tranche hardens persisted provider-control semantics but does not render the provider manager, connect the persisted state to live Search UI/provider fan-out, establish representative-device Search acceptance, or satisfy Release Candidate, production, or Stable gates. Issue #80 remains open.

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