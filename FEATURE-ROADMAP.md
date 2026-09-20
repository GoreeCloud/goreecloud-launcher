# GoreeCloud Launcher — Feature Roadmap

**Status:** Active roadmap control  
**As of:** 2026-09-20  
**Authoritative project record:** Project Specification — Launcher  
**Canonical repository:** GoreeCloud/launcher  
**Drive control:** `GoreeCloud/Feature Roadmap/GoreeCloud Launcher/FEATURE-ROADMAP.docx`

## Purpose

This file is the repository-side feature roadmap control for GoreeCloud Launcher. It records current planned and recommended feature work against the authoritative project record, verified repository state, the nine Integral Platform Systems, and current release gates. Historical branch or pull-request evidence is provenance only unless the corresponding behavior is verified on authoritative `main`.

## Current verified baseline

- Latest source-bearing runtime commit: `6a3a4ed090764f9fdff535a0baf4639cd9d2f715` (integrated PR #137).
- Owner representative-device review of the PR #135 exact-main Development APK did not accept the Home, app-drawer, or Settings experience, so PR #137 implements the second controlled Launcher experience correction rather than treating PR #135 as rendered application acceptance.
- PR #137 adds a first-party Home clock/date card with Clock/Compact/Off modes, Home quick actions, a guided empty-workspace surface, drawer Glass/Solid material control and optional app count, and expanded Home/App drawer/Icons/Appearance/Gestures/System Settings. The new launcher-owned visual preferences remain outside the strict seven-field portable backup/recovery v1 contract.
- PR #137 first candidate `f4c463862d9e17ba262dfa15c1089bd73cd54ba3` passed source/build validation but failed Android 16 runtime acceptance when the redesign changed the established drawer contract label from `Apps` to `All Apps`; the runtime test was preserved and the contract was corrected.
- Corrected PR #137 exact head `c315d15f228a1f38f3b222d2873b6eb03302a639` passed Android CI `35524733275` before merge, and exact-main Android CI `35525073047` passed after guarded squash merge, both including validate and the Android 16 room-runtime-emulator job.
- Exact-main artifact `10609785839` is bound to current main; archive digest is `sha256:984ef9108c7a00caedc903ed2f685a8165f46c6114321db082e21d70124e40e2` and extracted `GoreeCloud-Launcher-Dev.apk` SHA-256 is `abd58c89a40ea252b6815a45658b0881a57b9d26c774576f4ebb27a3a3f283c7`.
- PR #130 remains the source of persisted Grid, Compact, and List app-drawer modes plus restored shared icon-cache use in drawer rendering.
- Official Stable GLAZE UI V1.6 / `1.6.0` source mapping is integrated through PR #128 at exact source `a7180679ea851389e0f3004515f9a25f420e716d`; application-specific acceptance remains blocked.
- Platform Contract 0.4 evaluates all nine Integral Platform Systems; overall conformance remains nonconformant while unresolved systems and application acceptance are open.
- GoreeCloud Launcher remains Development. Issue #80 remains the active representative-device, accessibility, performance, Quickstep/Recents, platform-system, signing, and release gate.

## Roadmap

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| LCH-P0-01 | HOME return and workspace gesture stabilization: preserve primary-Home return, reliable content-origin drawer gesture handling, and smooth state transitions without whole-root recreation. | P0 / High | PR #137 is integrated on current main and adds a useful Home clock/date card, quick actions, and a guided empty-workspace surface while preserving existing HOME/drawer gesture contracts. Representative physical-device/default-HOME visual, gesture, and transition acceptance remains pending under issue #80. |
| LCH-P0-02 | Local-first installed-app search with optional GoreeCloud Index augmentation. Launcher owns local LauncherApps filtering; Index retains universal provider/ranking authority. | P0 / High | Local installed-app label/package filtering and the Search apps surface are present on the verified source-bearing runtime baseline. Issue #80 keeps complete app-discovery/search behavior and representative-device acceptance open. |
| LCH-P0-03 | App drawer experience: smooth transition quality plus configurable Grid, List, Compact, Category, and Search-first modes with density/column/icon/spacing/label controls. | P0 / High | Grid, Compact, and List remain integrated through PR #130. PR #137 refines the Glaze drawer material hierarchy, adds Glass/Solid background control and optional app count, and preserves the established `Apps` runtime contract. Category, Search-first, optional paged presentation, broader one-handed controls, and representative physical-device/performance/accessibility acceptance remain open. |
| LCH-P0-04 | Complete app enumeration and visibility across supported profiles unless explicit user policy hides an application; handle package/profile state changes correctly. | P0 / High | Android LauncherApps remains inventory authority. Issue #80 keeps complete package/profile enumeration, work/private-profile behavior, and representative-device app-discovery acceptance open. |
| LCH-P0-05 | Performance and Android system integration: shared icon cache/preloading, reduced recomposition/bitmap churn, transition profiling, and Quickstep/Recents compatibility. | P0 / High | A shared process-local icon cache is present and PR #130 restores shared cache use in drawer rendering. PR #137 exact-main Android CI is green, but prior representative-device testing reported slow/laggy behavior and the new V2 build still requires owner device review. Measured frame timing/jank, representative physical-device/default-HOME acceptance, and Quickstep/Recents compatibility remain open. |
| LCH-P1-01 | Workspace editing: primary-grid migration, cross-page drag/drop, folders, smart folders, categories/tags/favorites/collections, page management, and safe destructive-edit recovery. | P1 / High | Workspace editing remains a separate stabilization area. Historical PR #98–#104 candidate evidence is provenance only unless the corresponding behavior is separately verified on the current source-bearing runtime baseline. Durable multi-step/process-death-safe history, direct group drag, folders, and broader organization remain open. |
| LCH-P1-02 | Android launcher capabilities: pinned/dynamic shortcuts, AppWidgetHost widget placement/resizing, work/private profile handling, package states, and launch animations. | P1 / High | Planned after the stable launcher core; no Release Candidate or Stable claim. |
| LCH-P1-03 | GLAZE UI V1.6 personalization and presentation: current theme authority, icon packs/masking/normalization, wallpaper palettes, drawer/folder/dock styling, density, motion, reduced-motion/transparency, contrast, scalable text, optical behavior, and touch accessibility. | P1 / High | Official Stable GLAZE UI V1.6 / `1.6.0` source mapping is integrated through PR #128. PR #137 adds the second rendered Glaze experience tranche plus working local Home-card, quick-action, drawer-material, app-count, and expanded Settings controls while preserving direct Theme Manager selection. Application-specific accessibility/adaptive/device/performance/rollback/Human Visual Excellence acceptance remains blocked; this is still Development, not full consumer acceptance. |
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
- Representative-device review of the PR #137 V2 Home/drawer/settings experience, followed by measured frame timing/jank correction and transition tuning.
- Category, Search-first, broader density/spacing/label controls, and expanded one-handed reachability for the app drawer.
- Broader Launcher settings/personalization coverage beyond PR #137 for dock pages, folders, widgets, configurable gestures, wallpaper palettes, deeper translucency/motion controls, icon treatment, backup/recovery, and accessibility as the corresponding runtime capabilities become real.
- Complete Launcher-specific GLAZE UI V1.6 rendered, accessibility, adaptive phone/tablet/foldable, performance/power, rollback, Human Visual Excellence, and representative-device acceptance.
- Durable multi-step undo/redo and process-death-safe edit history after the source-bearing runtime workspace-editing baseline is separately verified and accepted.
- Folders, smart folders, collections, categories, tags, locked/private organizational surfaces, and explicit user-approved smart reorganization.
- Android AppWidgetHost, searchable widget gallery, resize/configuration, widget stacks, first-party Glaze Cards, and provider crash containment.
- Work/private-profile correctness, phone/tablet/foldable/posture-aware layouts, keyboard/D-pad/Switch/TalkBack accessibility, and physical-device performance/power acceptance.
- Evidence-backed runtime integration for all applicable Integral Platform Systems.

## Verification boundary

Current source-bearing main is `6a3a4ed090764f9fdff535a0baf4639cd9d2f715` from PR #137. Corrected exact-head Android CI `35524733275` and exact-main Android CI `35525073047` passed validate and Android 16 room-runtime-emulator. The exact-main artifact `10609785839` is bound to that source, with extracted APK SHA-256 `abd58c89a40ea252b6815a45658b0881a57b9d26c774576f4ebb27a3a3f283c7`. This remains Development evidence only: source, unit, lint, build, artifact, and managed-emulator success do not establish owner representative physical-device/default-HOME acceptance, measured performance/jank, Quickstep/Recents compatibility, assistive-technology acceptance, complete platform-system runtime acceptance, production signing/distribution, Release Candidate, production, or Stable qualification.

## Maintenance and synchronization

This roadmap and the corresponding Drive `FEATURE-ROADMAP.docx` must remain materially synchronized with one another and with the authoritative project record. Update both copies whenever feature scope, priority, dependency, implementation status, cancellation, supersession, recommendation, or verification state materially changes.

## Reconciliation rule

At each material feature change, reconcile this roadmap against the current authoritative project record, repository implementation state, applicable platform-system requirements, and GoreeCloud Tasks Management. Missing obligations, stale status, duplicated work, roadmap drift, or undocumented disposition changes are defects to correct.
