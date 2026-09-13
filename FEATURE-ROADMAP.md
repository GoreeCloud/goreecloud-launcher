# GoreeCloud Launcher Feature Roadmap

**Status:** Active Development roadmap control  
**As of:** September 12, 2026  
**Authoritative project record:** `Project Specification — Launcher` in GoreeCloud Drive  
**Canonical Drive counterpart:** `GoreeCloud/Feature Roadmap/GoreeCloud Launcher/FEATURE-ROADMAP.docx`

This roadmap records current planned and recommended GoreeCloud Launcher work against the authoritative project specification and verified repository evidence. It does not replace the project specification, release evidence, Platform Contract authority, GoreeCloud Tasks Management, review, or release approval.

The repository copy and Drive `FEATURE-ROADMAP.docx` must remain materially synchronized whenever feature scope, priority, dependency, implementation state, cancellation, supersession, recommendation, or verification state changes.

## Active roadmap

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| LCH-P0-01 | HOME return and workspace gesture stabilization: preserve primary-Home return, reliable content-origin drawer gesture handling, and smooth state transitions without whole-root recreation. | P0 / High | Automated Development evidence is green for HOME return (PR #84) and content-swipe reliability (PR #86); representative physical-device acceptance remains pending. |
| LCH-P0-02 | Local-first installed-app search with optional GoreeCloud Index augmentation. Launcher owns local `LauncherApps` filtering; Index retains universal provider/ranking authority. | P0 / High | PR #88 search implementation revision `07aa83f757aa5681c240ab65975b17b0a7845d92` passed Android CI run `34301121296`. The PR remains Draft; review/integration and physical-device acceptance remain pending. |
| LCH-P0-03 | App drawer experience: smooth transition quality plus configurable Grid, List, Compact, Category, and Search-first modes with density/column/icon/spacing/label controls. | P0 / High | Grid/List implementation has started in stacked Draft PR #96. The preference is persistent with Grid fail-safe default; List reuses local search, launch/manage paths, and the shared icon pipeline. Exact-head CI and representative physical-device/default-HOME acceptance remain required. Compact, Category, Search-first, paged presentation, sort controls, and search-position controls remain planned. |
| LCH-P0-04 | Complete app enumeration and visibility: reliably surface every valid `MAIN/LAUNCHER` activity, including Memos, across supported profiles unless explicit user policy hides it; handle package/profile state changes correctly. | P0 / High | Draft PR #92 current exact head `c95b3ec930b7f054e6a2f00a9bb0fe91a26062f3` passed Android CI run `34533834229`, including the Android 16 Room runtime job. It introduces package/profile-scoped incremental `LauncherApps` reconciliation for ordinary package changes plus registered profile-topology refresh triggers. Representative physical-device enumeration/profile acceptance remains pending; complete enumeration is not yet claimed. |
| LCH-P0-05 | Performance and Android system integration: shared icon cache/preloading, reduced recomposition/bitmap churn, transition profiling, and LineageOS Quickstep/Recents compatibility. | P0 / High | Draft PR #93 final exact head `003cf4622f226cc5bbe3618241fa5d9c20872b4e` passed Android CI run `34537425930` / #342 and provides the bounded 8 MiB process icon cache, shared 144 px asynchronous same-generation decode path, package/profile invalidation, and stale-generation rejection. Stacked Draft PR #94 final exact head `a27c3848097bfd4c151bddc64953abad8efc47d0` passed Android CI run `34635453084` / #344 and isolates shared decoding from transient UI-waiter cancellation. Stacked Draft PR #95 implementation head `b6bf79ebc44ded5bda9c6526ce132ddb93bf3d96` passed Android CI run `34706584005` / #345 and adds a bounded 24-icon background preload window that shares the same generation-stamped single-flight path without delaying authoritative inventory delivery. Source-level icon preloading is therefore implemented on the current Development stack; measured recomposition/transition performance, representative physical-device/default-HOME acceptance, Quickstep/Recents compatibility, and release acceptance remain open. |
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

## Launcher experience expansion — September 12, 2026

The detailed capability set is documented in `LAUNCHER-EXPERIENCE-EXPANSION.md`. It incorporates the new launcher-reference direction while keeping the distinction between current Development implementation and future planned scope.

### Active first tranche

- Persistent Grid/List app drawer presentation is being implemented in Draft PR #96, stacked on Draft PR #95.
- Grid remains the default and fail-safe mode.
- List mode is lazy-rendered and reuses existing local search, launch/manage behavior, icon cache, and icon-scale controls.
- The new drawer presentation key is deliberately outside portable backup/recovery v1; a future snapshot format must version it in explicitly rather than silently broadening the current recovery contract.

### Planned next capabilities

- Compact, Category, Search-first, and optional paged drawer presentation modes.
- Alphabetical/custom/recent/frequent views with local-first rules; no behavioral tracking.
- Top/bottom drawer search placement and one-handed reachability controls.
- Home overview/edit mode, page cleanup, multi-select, group movement, and safe undo/history.
- Folders, smart folders, collections, categories, tags, locked/private organizational surfaces, and explicit user-approved smart reorganization.
- Android `AppWidgetHost`, searchable widget gallery, resize/configuration, widget stacks, first-party Glaze Cards, and provider crash containment.
- Expanded long-press actions, Android shortcuts, app info/uninstall-disable handoff, privacy controls, and evidence-backed Wardveil status.
- Independent Home/Drawer/Folder/Dock density and styling, icon/label overrides, wallpaper palettes, motion/blur/transparency/accessibility controls.
- Launcher Profiles for Personal, Work, Travel, Focus, Minimal, and user-defined layouts.
- A future Glaze Shelf for user-pinned actions, media controls, temporary tasks, and compact first-party cards.
- Work profile/private-space correctness and no cross-profile leakage through search, suggestions, widgets, previews, or backup.
- Phone/tablet/foldable/posture-aware layouts, keyboard/D-pad/Switch/TalkBack accessibility, and physical-device performance/power acceptance.
- Privacy Shield, Wardveil Security, and Everkeep integrations that remain evidence-backed and fail closed where authorization or recovery proof is required.

GoreeCloud Index remains the canonical universal search/index/provider/ranking authority throughout this expansion. Launcher continues to own narrow local installed-app filtering and Launcher-specific presentation/organization only.

## Reconciliation rule

At each material feature change, reconcile this roadmap against the current authoritative project record, repository implementation state, the seven Platform Systems, and GoreeCloud Tasks Management. Missing obligations, stale status, duplicated work, roadmap drift, or undocumented disposition changes are defects to correct.
