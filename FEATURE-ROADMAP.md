# GoreeCloud Launcher Feature Roadmap

**Status:** Active Development roadmap control  
**As of:** September 10, 2026  
**Authoritative project record:** `Project Specification — Launcher` in GoreeCloud Drive  
**Canonical Drive counterpart:** `GoreeCloud/Feature Roadmap/GoreeCloud Launcher/FEATURE-ROADMAP.docx`

This roadmap records current planned and recommended GoreeCloud Launcher work against the authoritative project specification and verified repository evidence. It does not replace the project specification, release evidence, Platform Contract authority, GoreeCloud Tasks Management, review, or release approval.

The repository copy and Drive `FEATURE-ROADMAP.docx` must remain materially synchronized whenever feature scope, priority, dependency, implementation state, cancellation, supersession, recommendation, or verification state changes.

## Active roadmap

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| LCH-P0-01 | HOME return and workspace gesture stabilization: preserve primary-Home return, reliable content-origin drawer gesture handling, and smooth state transitions without whole-root recreation. | P0 / High | Automated Development evidence is green for HOME return (PR #84) and content-swipe reliability (PR #86); representative physical-device acceptance remains pending. |
| LCH-P0-02 | Local-first installed-app search with optional GoreeCloud Index augmentation. Launcher owns local `LauncherApps` filtering; Index retains universal provider/ranking authority. | P0 / High | PR #88 search implementation revision `07aa83f757aa5681c240ab65975b17b0a7845d92` passed Android CI run `34301121296`. The PR remains Draft; review/integration and physical-device acceptance remain pending. |
| LCH-P0-03 | App drawer experience: smooth transition quality plus configurable Grid, List, Compact, Category, and Search-first modes with density/column/icon/spacing/label controls. | P0 / High | Partially implemented drawer/search foundation; advanced modes and transition acceptance remain planned/incomplete. |
| LCH-P0-04 | Complete app enumeration and visibility: reliably surface every valid `MAIN/LAUNCHER` activity, including Memos, across supported profiles unless explicit user policy hides it; handle package/profile state changes correctly. | P0 / High | Draft PR #92 introduces package/profile-scoped incremental `LauncherApps` reconciliation for ordinary package changes plus registered profile-topology refresh triggers. Source/build validation and representative physical-device enumeration/profile acceptance remain pending; complete enumeration is not yet claimed. |
| LCH-P0-05 | Performance and Android system integration: shared icon cache/preloading, reduced recomposition/bitmap churn, transition profiling, and LineageOS Quickstep/Recents compatibility. | P0 / High | Draft PR #92 advances the incremental app-index refresh portion by avoiding full cross-profile rescans for ordinary package changes. Exact-head validation is pending; shared icon cache/preloading, recomposition/bitmap profiling, Quickstep/Recents, and representative-device acceptance remain open. |
| LCH-P1-01 | Workspace editing: complete primary-grid migration, mature cross-page drag/drop, folders, smart folders, categories/tags/favorites/collections, page management, and safe destructive-edit recovery. | P1 / High | Workspace persistence/page movement are advanced but incomplete; folders, mature cross-page editing, and broader organization remain planned. |
| LCH-P1-02 | Android launcher capabilities: pinned/dynamic shortcuts, `AppWidgetHost` widget placement/resizing, work/private profile handling, package states, and launch animations. | P1 / High | Planned after the stable launcher core; no RC/Stable claim. |
| LCH-P1-03 | Glaze UI V1.3 personalization and presentation: current theme authority, icon packs/masking/normalization, wallpaper palettes, drawer/folder/dock styling, density, motion, reduced-motion/transparency, contrast, scalable text, and touch accessibility. | P1 / High | Current shared Glaze authority is V1.3 / `1.3.0`. Source migration work exists, but complete rendered/accessibility/device acceptance remains open. |
| LCH-P1-04 | Portable backup, restore, and recovery: complete versioned export/import, clean-target rebinding, preference/workspace fidelity, process-death and schema-upgrade recovery, rollback, and Everkeep integration/acceptance. | P1 / High | Partial backup/restore and recovery foundations exist; complete versioned portability, OS process-death/schema-upgrade evidence, and Everkeep acceptance remain open. |
| LCH-P1-05 | Seven Platform Systems: Manager, Privacy Shield, Wardveil Security, Everkeep, Glaze UI, Mesh, and Identity must have explicit applicable/non-applicable declarations, runtime boundaries, evidence, and accepted dependencies. | P1 / High | Development integration remains incomplete; no overall Platform conformance/Stable claim until required systems are accepted. |
| LCH-M4-01 | Local intelligence after core stabilization: local suggestions, smart folders, contextual Glaze Cards, and optional configured-provider handoff without transferring universal search authority from Index. | Later / Medium | Planned; must remain local-first/private by default and separately governed before activation. |
| LCH-RC-01 | Release-candidate qualification: TalkBack/Switch/keyboard/touch accessibility, phone/tablet/foldable and representative physical-device tests, performance/latency profiling, privacy/security review, reproducible signing/provenance, rollback, and release approval. | RC gate / High | Open. Development remains controlling until required automated, manual, physical-device, platform, signing, and release gates pass. |
| GOV-01 | Keep this file and the Drive `FEATURE-ROADMAP.docx` materially synchronized with the authoritative project specification and verified repository state. | Governance / High | Ongoing control. |
| GOV-02 | Move actionable roadmap obligations into GoreeCloud Tasks Management when required, preserving priority, dependencies, lifecycle disposition, and verification state. | Governance / High | Ongoing control. |
| GOV-03 | Do not mark roadmap items implemented, complete, cancelled, superseded, RC, Stable, or production-approved without authoritative evidence and synchronized records. | Governance / High | Ongoing control. |

## Reconciliation rule

At each material feature change, reconcile this roadmap against the current authoritative project record, repository implementation state, the seven Platform Systems, and GoreeCloud Tasks Management. Missing obligations, stale status, duplicated work, roadmap drift, or undocumented disposition changes are defects to correct.
