# GoreeCloud Launcher — Planned Features

**Record type:** Repository planned/open feature inventory  
**Repository:** `GoreeCloud/launcher`  
**Lifecycle:** Development  
**Migration state:** **Authoritative on `main` after PR #201 merged as `009371938ac3cab041cfb0893ede68e66e211a4f` and default-branch readback verified this record. Legacy Drive roadmap retirement was subsequently verified after PR #203.**  
**Repository authority baseline:** `main` at `0af5d6753d1dca98de432f24f8703fe5bae85e2c` (PR #207 merged September 22, 2026).  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance, version 1.0, effective September 22, 2026.

## Purpose

This file carries forward every material open, planned, partial, deferred, blocked, or acceptance-gated feature obligation from the retired roadmap model. A partially implemented feature remains here until its defined implementation and acceptance scope is complete.

Source/build/unit/schema/managed-emulator evidence does not by itself establish representative physical-device acceptance, Release Candidate, production, or Stable status. GoreeCloud Launcher remains **Development**. Issue #80 remains the active stabilization and release-gate record.

## Migration reconciliation

This inventory was migrated from:

- retired repository `FEATURE-ROADMAP.md`;
- retired `GoreeCloud/Feature Roadmap/GoreeCloud Launcher/FEATURE-ROADMAP.docx`; and
- current authoritative repository/PR evidence.

PR #201 merged the replacement feature records and retired `FEATURE-ROADMAP.md` from authoritative `main`. PR #203 then reconciled the repository-native authority records against the verified post-migration state. After authoritative `main` readback, the legacy Launcher Drive roadmap was deleted and its former file ID no longer resolves. Where lifecycle state had conflicted during migration, verified repository evidence controlled. Drive-only historical context was preserved where material rather than being used to downgrade newer verified implementation state.

The former roadmap synchronization obligation (`GOV-01`) is superseded by the September 22, 2026 repository-native standard. Google Drive is no longer a permitted active, mirrored, backup, convenience, or historical-shadow roadmap authority.

## Open feature and acceptance obligations

| ID | Feature / obligation | Priority | Current disposition |
| --- | --- | --- | --- |
| `LCH-P0-01` | HOME return and workspace gesture stabilization: preserve primary-Home return, reliable content-origin drawer gesture handling, direct Home interactions, and smooth state transitions without whole-root recreation. | P0 / High | **Partial / acceptance open.** Primary-Home handling, configurable Home gestures, Room-authoritative primary spatial placement, wallpaper-backed Home, page management, Dock/Search controls, and transition diagnostics are implemented in Development. Representative physical-device/default-HOME visual, gesture, accessibility, and transition acceptance remains open under issue #80. |
| `LCH-P0-02` | Native Launcher Universal Search and action layer: Launcher-owned UI, provider execution, local indexing foundations, aggregation/ranking, permission-aware categories, commands, shortcuts, recents/history, contextual actions, standardized provider APIs, and privacy-first provider controls. | P0 / High | **Partial.** PRs #173/#175/#179/#181/#180/#184/#188/#195/#198 established the Launcher-owned surface, trusted local actions, async provider execution, User/Work identity, provider metadata/catalog evaluation, privacy-first invocation policy, UI-facing grouping model, and versioned provider enable/order serialization. PR #199 adds dedicated DataStore persistence for provider enable/order controls, and PR #207 adds fail-closed persisted-state reconciliation plus bounded enable/order mutation helpers. Still open: collecting that persisted state in runtime UI, wiring controls into the rendered provider/source manager and automatic provider selection, portable backup/recovery adoption/migration, provider-specific consent/permission flows, explicit external handoff execution, external discovery/registration transport, richer local sources, partial/streaming presentation if justified, recents/history/context, optional Search/Index providers, and representative-device acceptance. |
| `LCH-P0-03` | App drawer experience with smooth transitions, configurable Grid/List/Compact/Category presentation, profile-aware organization, and density/column/icon/spacing/label controls without duplicating Universal Search. | P0 / High | **Partial / acceptance open.** Core layouts and controls plus User Apps / Work Apps presentation are implemented. Open: representative personal/work/Shelter/private-space acceptance, user-defined categories/tags/collections, broader organization/custom ordering, one-handed refinements, and representative performance/accessibility acceptance. |
| `LCH-P0-04` | Complete app enumeration and visibility across supported Android profiles unless explicit user policy hides an application; handle package/profile state changes correctly and prevent cross-profile leakage. | P0 / High | **Partial.** Android `LauncherApps` remains inventory authority, and current source distinguishes User/Work results and drawer inventory. Open: representative personal/work/Shelter/private-space lifecycle behavior, package/profile install/update/remove/suspend transitions, same-label app handling, launch correctness, isolation, and no-cross-profile-leakage acceptance. |
| `LCH-P0-05` | Performance and Android system integration: shared icon cache/preloading, reduced recomposition/bitmap churn, transition profiling, responsive Home/Apps/Search entry, and Quickstep/Recents compatibility. | P0 / High | **Partial.** Shared caching, single-flight loading, bounded warming, invalidation guards, transition diagnostics, async Search paths, and PR #205 latest-snapshot preload ownership are implemented. PR #205 cancels superseded background preload tails and cache-clear preloads while preserving the 12 MiB / 128-candidate / batches-of-three bounds and exact-stamp invalidation. Exact-head Android CI #643 and exact merged-main Android CI #644 both passed validation plus Android 16 Room/runtime and transition-performance emulator lanes. Open: representative physical-device/default-HOME frame-time/jank/memory/power acceptance and Quickstep/Recents compatibility on supported Android/LineageOS boundaries. |
| `LCH-P1-01` | Workspace editing: mature cross-page drag/drop, folders, smart folders, categories/tags/favorites/collections, page management, group operations, and safe destructive-edit recovery. | P1 / High | **Partial.** Room-backed primary spatial placement and guarded primary/secondary transfer foundations exist. Open: mature live transfer targets, arbitrary cross-page drag/drop, folders/smart folders, user collections/tags, group movement, durable multi-step undo/redo, populated-page deletion recovery, and process-death-safe edit history. |
| `LCH-P1-02` | Android launcher capabilities: pinned/dynamic shortcuts, AppWidgetHost widget placement/resizing/configuration, richer package states, work/private profile behavior, and launch animations. | P1 / High | **Planned.** Some profile-aware drawer foundations exist, but complete capability support and acceptance remain open. |
| `LCH-P1-03` | GLAZE UI V1.6 personalization and presentation: theme authority, icon packs/masking/normalization, wallpaper palettes, drawer/folder/Dock styling, density, motion, reduced-motion/transparency, contrast, scalable text, optical behavior, and touch accessibility. | P1 / High | **Partial / blocked by acceptance.** Current Stable source mapping and substantial Development presentation controls exist. Open: complete Theme Manager/icon-pack behavior, rendered accessibility, adaptive phone/tablet/foldable behavior, reduced-effects and contrast/scalable-text acceptance, representative-device performance/power, rollback, Human Visual Excellence, and final consumer acceptance. |
| `LCH-P1-04` | Portable backup, restore, and recovery: versioned export/import, clean-target rebinding, preference/workspace fidelity, process-death/schema-upgrade recovery, rollback, and Everkeep integration/acceptance. | P1 / High | **Partial.** Existing portability/recovery foundations remain. PR #199 persists provider controls only in a dedicated local DataStore and deliberately does **not** add them to strict portable-preference v1. Open: portable adoption/migration of provider controls, broader current preference/workspace coverage, restore preview/rollback, process-death/schema-upgrade evidence, clean-target recovery, and Everkeep acceptance. |
| `LCH-P1-05` | Nine Integral Platform Systems: GoreeCloud Manager, Privacy Shield, Wardveil Security, Everkeep, Glaze UI, GoreeCloud Mesh, GoreeCloud Identity, GoreeCloud Policy, and GoreeCloud Observability. GoreeCloud Sync remains separately governed. | P1 / High | **Partial / unresolved.** Platform Contract evaluation exists, but complete evidence-backed runtime integration and application acceptance remain incomplete. Unsupported or unverified integration states must remain blocked/nonconformant rather than being omitted or promoted. |
| `LCH-M4-01` | Advanced intelligence after core Universal Search stabilization: local suggestions, smart folders, contextual Glaze Cards, natural-language assistance, and optional GoreeCloud Search / GoreeCloud Index provider integration without transferring Launcher Universal Search ownership. | Later / Medium | **Planned / deferred until prerequisites.** Requires separate authority, privacy, security, offline, failure-isolation, user-control, and acceptance contracts. |
| `LCH-RC-01` | Release-candidate qualification: accessibility, representative physical-device testing, performance/latency profiling, privacy/security review, recovery/rollback, reproducible signing/provenance, and release approval. | RC gate / High | **Open.** Phase A remains open/not passed. No Release Candidate, production, or Stable state is claimed. |
| `GOV-01` | Maintain repository and Drive roadmap synchronization. | Governance | **Superseded September 22, 2026; legacy Drive source retired.** Replaced by the repository-native governance standard. No ongoing Drive roadmap synchronization is authorized, and the successfully migrated Launcher Drive roadmap has been deleted after authoritative verification. |
| `GOV-02` | Ensure actionable open obligations are represented in GoreeCloud Tasks Management where required. | Governance / High | **Ongoing.** Issue #80 and the centralized GitHub Improvement Task List carry applicable active obligations. Avoid duplicate task records. |
| `GOV-03` | Preserve evidence-backed lifecycle state and do not mark features complete, cancelled, superseded, Release Candidate, Stable, or production-approved without authoritative evidence. | Governance / High | **Ongoing.** Launcher remains Development. |

## Planned next capabilities

### Universal Search

- Render the PR #195 presentation grouping in the Glaze-native Search surface and implement the provider/source manager with source identity, enable/disable state, deterministic order, invocation mode, and privacy information.
- Connect PR #199's persisted provider-control state and PR #207's fail-closed reconciliation/mutation policy to the rendered management experience and automatic local provider selection without broadening the data boundary.
- Adopt provider-control state into a separately versioned portable backup/recovery migration only after compatibility, restore, explicit-empty semantics, and rollback behavior are defined and tested.
- Add explicit `Search with` handoff execution only after provider-specific trust, authorization, privacy, retention, and failure contracts are accepted. Network, remote-processing, retaining, authorization-requiring, and third-party providers must remain user-invoked and must not receive automatic typed-query fan-out.
- Add external provider discovery/registration only after trust boundaries, compatibility/versioning, revocation behavior, package/profile isolation, and user-facing provider information are accepted.
- Add richer local sources, settings/actions, shortcuts, commands, and permission-aware result categories before optional GoreeCloud Search / GoreeCloud Index integrations.
- Define privacy-preserving recents/history/context semantics before storing or using query/activity history; core Search must remain useful with those features disabled.
- Evaluate partial/streaming result presentation only if measured evidence shows that it improves interaction quality.

### Profile and Android-system acceptance

- Validate User Apps / Work Apps on representative personal/work/Shelter/private-space configurations, including profile lifecycle and package-state transitions.
- Verify same-label apps across profiles, correct launch identity, isolation, and no cross-profile leakage.
- Collect representative physical-device/default-HOME latency, frame-time/jank, memory, power, first-open/warm-open, and OEM-specific evidence, including the PR #205 latest-snapshot icon-preload behavior under realistic package/profile churn.
- Verify Quickstep/Recents compatibility on supported Android/LineageOS boundaries.

### Workspace and Android launcher capabilities

- Mature cross-page drag/drop and primary↔secondary movement.
- Add folders and smart folders with recovery-safe editing.
- Add AppWidgetHost widget placement, searchable widget gallery, resize/configuration, widget crash containment, and supported Glaze Cards.
- Add pinned/dynamic shortcuts, richer package states, and launch animations.
- Add categories, tags, collections, custom ordering, and transparent user-controlled local recent/frequent views.
- Add durable multi-step undo/redo and process-death-safe edit recovery for destructive operations.

### Presentation and accessibility

- Complete Theme Manager behavior, icon-pack discovery/application, icon masking/normalization, wallpaper-derived palettes, expression controls, and broader gesture bindings.
- Complete Launcher-specific GLAZE UI V1.6 rendered/accessibility/adaptive/performance/power/rollback/Human Visual Excellence acceptance.
- Validate keyboard, D-pad, Switch Access, TalkBack, large text, RTL/localization, reduced motion/transparency, contrast, and touch-target behavior across Home, Search, Apps, Settings, and editing surfaces.

### Portability, platform integration, and release

- Complete versioned local export/import and clean-target restore for current preferences and workspace state.
- Integrate portable recovery with Everkeep and other authorized continuity systems only after their contracts are verified.
- Complete evidence-backed runtime integration for applicable Integral Platform Systems while preserving authority boundaries.
- Complete protected signing, reproducible packaging, provenance, rollback/recovery, Release Candidate qualification, and production approval gates.

## Historical migration context

The retired Drive roadmap included a September 12–13, 2026 Launcher experience-expansion reference to `LAUNCHER-EXPERIENCE-EXPANSION.md`. That historical reference is preserved here for traceability but does not override current repository implementation or lifecycle evidence.

Earlier roadmap revisions that assigned Universal Search ownership or provider/ranking authority differently remain historical provenance for their exact revisions. The current architecture assigns core Universal Search ownership to GoreeCloud Launcher; GoreeCloud Search and GoreeCloud Index may later integrate only as optional providers/backends.

## Completion rule

When an obligation becomes complete enough to be represented as implemented:

1. add or reconcile the evidence-backed capability in `IMPLEMENTED-FEATURES.md`;
2. remove it from active planned/open work or record its completed disposition here when historical traceability requires it; and
3. record the meaningful implementation event in `CHANGELOGS.md`.

Do not promote a partial feature merely because one tranche, pull request, CI run, or emulator path succeeded.