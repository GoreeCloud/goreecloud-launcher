# GoreeCloud Launcher — Feature Roadmap

**Status:** Active roadmap control  
**As of:** 2026-09-20  
**Authoritative project record:** Project Specification — Launcher  
**Canonical repository:** GoreeCloud/launcher  
**Drive control:** `GoreeCloud/Feature Roadmap/GoreeCloud Launcher/FEATURE-ROADMAP.docx`

## Purpose

This file is the repository-side feature roadmap control for GoreeCloud Launcher. It records current planned and recommended feature work against the authoritative project record, verified repository state, the nine Integral Platform Systems, and current release gates. Historical branch or pull-request evidence is provenance only unless the corresponding behavior is verified on authoritative `main`.

## Current verified baseline

- Latest source-bearing runtime commit: `90db5f3c6fac192610872ca8adf5c353a45878de`. Documentation-only commits may advance the default branch; verify GitHub live whenever the exact current `main` SHA is material.
- PR #130 integrated persisted Grid, Compact, and List app-drawer presentation plus restored shared icon-cache use in drawer rendering.
- PR #130 accepted exact head `0c6ba267ae6aad01696767774fd959eea1eb555f`; Android CI `35495421670` succeeded before merge and Android CI `35496206431` succeeded after merge on the exact authoritative main.
- Official Stable GLAZE UI V1.6 / `1.6.0` source mapping is integrated through PR #128 at exact source `a7180679ea851389e0f3004515f9a25f420e716d`; application-specific acceptance remains blocked.
- Platform Contract 0.4 evaluates all nine Integral Platform Systems; overall conformance remains nonconformant while unresolved systems and application acceptance are open.
- GoreeCloud Launcher remains Development. Issue #80 remains the active representative-device, accessibility, performance, Quickstep/Recents, platform-system, signing, and release gate.

## Roadmap

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| LCH-P0-01 | HOME return and workspace gesture stabilization: preserve primary-Home return, reliable content-origin drawer gesture handling, and smooth state transitions without whole-root recreation. | P0 / High | Integrated Development source/automation evidence includes workspace-content drawer gesture handling from PR #120 and bounded Home/app-drawer transitions from PR #123. Representative physical-device/default-HOME gesture and transition acceptance remains pending under issue #80. |
| LCH-P0-02 | Local-first installed-app search with optional GoreeCloud Index augmentation. Launcher owns local LauncherApps filtering; Index retains universal provider/ranking authority. | P0 / High | Local installed-app label/package filtering and the Search apps surface are present on authoritative main. Issue #80 keeps complete app-discovery/search behavior and representative-device acceptance open. |
| LCH-P0-03 | App drawer experience: smooth transition quality plus configurable Grid, List, Compact, Category, and Search-first modes with density/column/icon/spacing/label controls. | P0 / High | Grid, Compact, and List are integrated through PR #130. Category, Search-first, optional paged presentation, broader one-handed controls, and representative physical-device/performance/accessibility acceptance remain open. |
| LCH-P0-04 | Complete app enumeration and visibility across supported profiles unless explicit user policy hides an application; handle package/profile state changes correctly. | P0 / High | Android LauncherApps remains inventory authority. Issue #80 keeps complete package/profile enumeration, work/private-profile behavior, and representative-device app-discovery acceptance open. |
| LCH-P0-05 | Performance and Android system integration: shared icon cache/preloading, reduced recomposition/bitmap churn, transition profiling, and Quickstep/Recents compatibility. | P0 / High | A shared process-local icon cache is present and PR #130 restores shared cache use in drawer rendering. Exact-main Android CI is green. Measured performance, representative physical-device/default-HOME acceptance, and Quickstep/Recents compatibility remain open. |
| LCH-P1-01 | Workspace editing: primary-grid migration, cross-page drag/drop, folders, smart folders, categories/tags/favorites/collections, page management, and safe destructive-edit recovery. | P1 / High | Workspace editing remains a separate stabilization area. Historical PR #98–#104 candidate evidence is provenance only unless the corresponding behavior is verified on authoritative main. Durable multi-step/process-death-safe history, direct group drag, folders, and broader organization remain open. |
| LCH-P1-02 | Android launcher capabilities: pinned/dynamic shortcuts, AppWidgetHost widget placement/resizing, work/private profile handling, package states, and launch animations. | P1 / High | Planned after the stable launcher core; no Release Candidate or Stable claim. |
| LCH-P1-03 | GLAZE UI V1.6 personalization and presentation: current theme authority, icon packs/masking/normalization, wallpaper palettes, drawer/folder/dock styling, density, motion, reduced-motion/transparency, contrast, scalable text, optical behavior, and touch accessibility. | P1 / High | Official Stable GLAZE UI V1.6 / `1.6.0` source mapping is integrated through PR #128. Application-specific rendered/accessibility/adaptive/device/performance/rollback/Human Visual Excellence acceptance remains blocked; source mapping is not consumer acceptance. |
| LCH-P1-04 | Portable backup, restore, and recovery: versioned export/import, clean-target rebinding, preference/workspace fidelity, process-death/schema-upgrade recovery, rollback, and Everkeep integration/acceptance. | P1 / High | Partial backup/restore and recovery foundations exist; complete versioned portability, OS process-death/schema-upgrade evidence, and Everkeep acceptance remain open. |
| LCH-P1-05 | Nine Integral Platform Systems: Manager, Privacy Shield, Wardveil Security, Everkeep, Glaze UI, Mesh, Identity, Sync, GoreeCloud Policy, and GoreeCloud Observability. | P1 / High | Platform Contract 0.4 evaluates all nine systems, with unresolved systems explicitly blocked/nonconformant rather than omitted. Overall platform integration remains incomplete. |
| LCH-M4-01 | Local intelligence after core stabilization: local suggestions, smart folders, contextual Glaze Cards, and optional configured-provider handoff without transferring universal search authority from Index. | Later / Medium | Planned; must remain local-first/private by default and separately governed before activation. |
| LCH-RC-01 | Release-candidate qualification: accessibility, representative physical-device tests, performance/latency profiling, privacy/security review, reproducible signing/provenance, rollback, and release approval. | RC gate / High | Open. Development remains the controlling lifecycle. Issue #80 still requires representative physical-device/default-HOME, accessibility, performance, Quickstep/Recents, platform-system runtime, recovery, signing/provenance, and release acceptance before later lifecycle promotion. |
| GOV-01 | Keep repository `FEATURE-ROADMAP.md` and Drive `FEATURE-ROADMAP.docx` materially synchronized with the authoritative project specification and verified repository state. | Governance / High | Ongoing control. |
| GOV-02 | Move actionable roadmap obligations into GoreeCloud Tasks Management when required, preserving priority, dependencies, lifecycle disposition, and verification state. | Governance / High | Ongoing control. |
| GOV-03 | Do not mark roadmap items implemented, complete, cancelled, superseded, RC, Stable, or production-approved without authoritative evidence and synchronized records. | Governance / High | Ongoing control. |

## Planned next capabilities

- Category, Search-first, and optional paged drawer presentation.
- Explicit custom ordering plus recent/frequent views only when locally derived, transparent, user-controlled, and free of behavioral tracking.
- Expanded one-handed reachability controls and transition tuning after current-main drawer/search behavior is separately verified.
- Complete Launcher-specific GLAZE UI V1.6 rendered, accessibility, adaptive phone/tablet/foldable, performance/power, rollback, Human Visual Excellence, and representative-device acceptance.
- Durable multi-step undo/redo and process-death-safe edit history after the current-main workspace-editing baseline is separately verified and accepted.
- Folders, smart folders, collections, categories, tags, locked/private organizational surfaces, and explicit user-approved smart reorganization.
- Android AppWidgetHost, searchable widget gallery, resize/configuration, widget stacks, first-party Glaze Cards, and provider crash containment.
- Work/private-profile correctness, phone/tablet/foldable/posture-aware layouts, keyboard/D-pad/Switch/TalkBack accessibility, and physical-device performance/power acceptance.
- Evidence-backed runtime integration for all applicable Integral Platform Systems.

## Verification boundary

The current verified integration state is Development evidence only. Source, unit, lint, build, and managed-emulator success do not establish representative physical-device/default-HOME acceptance, measured performance/jank, Quickstep/Recents compatibility, assistive-technology acceptance, complete platform-system runtime acceptance, production signing/distribution, Release Candidate, production, or Stable qualification.

## Maintenance and synchronization

This roadmap and the corresponding Drive `FEATURE-ROADMAP.docx` must remain materially synchronized with one another and with the authoritative project record. Update both copies whenever feature scope, priority, dependency, implementation status, cancellation, supersession, recommendation, or verification state materially changes.

## Reconciliation rule

At each material feature change, reconcile this roadmap against the current authoritative project record, repository implementation state, applicable platform-system requirements, and GoreeCloud Tasks Management. Missing obligations, stale status, duplicated work, roadmap drift, or undocumented disposition changes are defects to correct.
