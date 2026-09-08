# GoreeCloud Launcher Planned Capability Implementation Roadmap

## Status

**Planning document — Development roadmap, not an implementation or release claim.**

This repository document translates the approved GoreeCloud Launcher product scope into an implementation sequence. Canonical product requirements remain governed by the GoreeCloud project specification in Google Drive. Where this roadmap and canonical project documentation differ, the canonical project documentation controls product intent and the repository/source state controls implementation truth.

GoreeCloud Launcher remains Development and is not Stable-qualified. Passing source, CI, emulator, or repository contract checks for an individual slice does not establish complete product acceptance.

## Governing authority boundaries

The roadmap must preserve the following platform responsibilities:

- **GoreeCloud Launcher** owns Android HOME behavior, Launcher-owned workspace state, Launcher navigation, Launcher presentation, Launcher-specific settings, and Launcher invocation surfaces.
- **GoreeCloud Index** remains the canonical universal search/indexing authority, including provider discovery, query dispatch, normalization, ranking, grouping, deduplication, provenance, and universal result actions. Launcher must not create a rival universal index.
- **GoreeCloud Search** remains the Internet/Web/current-information provider reached through Index when implemented, enabled, and authorized.
- **GoreeCloud Identity** owns identity, authentication, roles, permissions, delegated authority, and applicable profile/account authority.
- **Wardveil Security** owns applicable trust, protection, verification, security policy, and security-state responsibilities.
- **Privacy Shield** owns consent, data minimization, purpose limitation, privacy controls, retention/deletion requirements, and privacy-state responsibilities.
- **Everkeep** owns continuity, preservation, portability, recovery, and recovery-verification responsibilities.
- **GoreeCloud Mesh** owns applicable cross-application/service capability discovery, coordination, events, and integration contracts without bypassing authorization.
- **GoreeCloud Manager** owns applicable administrative/control-plane visibility and authorized management workflows.
- **Glaze UI** owns the applicable presentation, interaction, responsiveness, accessibility, component, motion, and design-system contract.
- **GoreeCloud Branding Assets** remains the canonical source for approved GoreeCloud Launcher logos, icons, symbols, illustrations, and artwork.

No UI treatment, label, icon, badge, card, or placeholder may be treated as proof that a platform-system integration exists.

## Phase 0 — Current Development stabilization

Before broad feature expansion, continue closing the current launcher foundation gates:

- Complete the current applicable Stable Glaze UI migration and repository mapping without converting source mapping into a conformance claim.
- Preserve terminal Room authority and the protected primary Home compatibility boundary until a separately validated primary-grid migration exists.
- Continue production-hardening multi-page Home observation and mutation paths.
- Complete Android OS process-death/cold-start and schema-upgrade recovery evidence.
- Continue representative physical-device default-HOME, orientation, performance, and accessibility validation.
- Continue the bounded local backup/restore work toward a complete, versioned, validated Launcher-owned recovery format.
- Keep all current privacy, HOME-manifest, package-visibility, identity, Room, schema, and design-system guards fail closed.

### Exit criteria

Phase 0 is complete only when the current daily-launcher foundation has explicit recovery, accessibility, representative-device, and current-design-system evidence appropriate to the next expansion stage. It does not itself imply Stable qualification.

## Phase 1 — Complete workspace editing foundation

Build the remaining core workspace primitives before introducing higher-level dashboard profiles.

Planned work:

- Primary Home grid migration from the protected compatibility projection to an accepted spatial model.
- Mature same-page and cross-page drag-and-drop.
- Direct cell/span editing with collision, bounds, concurrency, and rollback protections.
- Folders and folder membership.
- Android application shortcuts and pinned/dynamic shortcut placement.
- AppWidgetHost integration, widget configuration, resize/rebind behavior, and safe failure recovery.
- Page-level layout density and supported per-page configuration.
- Dock expansion toward configurable size/pages where approved.
- Deterministic non-drag alternatives for important placement operations.
- Layout-lock coverage extended to each newly implemented placeable item type.

### Required validation

- JVM/domain tests for placement, folder, shortcut, and widget invariants.
- Android runtime tests using real Room state and AppWidgetHost behavior where practical.
- Accessibility paths for keyboard, TalkBack, Switch Access, and non-drag manipulation.
- Process recreation, process death, malformed-state, provider disappearance, and migration recovery.
- No broad package, storage, accessibility-service, or network privileges introduced merely to simplify Launcher behavior.

## Phase 2 — Application organization and advanced drawer

Expand Launcher-owned application navigation without violating GoreeCloud Index authority.

Planned capabilities:

- Grid, List, Compact, Category, and Search-first drawer presentation modes.
- Manual categories and custom sections.
- Custom folders and transparent smart-folder rules.
- Tags, favorites, custom collections, and application aliases.
- Recently installed and recently updated views.
- Optional local frequently-used and recent-app ranking.
- Hidden applications and permission-aware private visibility.
- Role/profile-aware drawer presentation where supported by Android and GoreeCloud Identity.

The drawer's narrow installed-application filter remains Launcher-owned. Cross-provider files, contacts, services, users, servers, commands, Web results, or other universal categories must remain in the GoreeCloud Index provider pipeline.

## Phase 3 — Workspace profiles and dashboard layouts

Introduce multiple Launcher environments only after the underlying workspace model is durable.

Initial profile targets:

- **Personal** — everyday applications, documents, communication, media, and personal contextual surfaces.
- **Administration** — infrastructure, monitoring, security, storage, backup, and management entry points.
- **Development** — code, Git, containers, testing, logs, deployment, and development tools.
- **Monitoring** — system/server health, metrics, alerts, resource state, and operational summaries.
- **Media** — media applications, controls, libraries, and approved contextual information.
- **Guest/User modes** — restricted or simplified experiences where supported by real identity/device policy.

Profile state may include page sets, visible collections, widgets/panels, shortcuts, and presentation preferences. Profile switching must not become an authorization bypass. A profile may change presentation only within the permissions already granted by Android, GoreeCloud Identity, managed-device policy, Privacy Shield, and Wardveil Security.

## Phase 4 — Smart widgets and first-party information panels

Create Launcher-owned Glaze UI surfaces backed by authoritative GoreeCloud sources.

Candidate panels include:

- Server health: CPU, memory, disk, reachability, and service health from an authorized source.
- Backup: last successful backup, health, recovery readiness, and failure state from the authoritative backup/continuity system.
- Security: accepted Wardveil Security state, alerts, update/security posture, and applicable audit outcomes.
- Storage: authorized capacity and health information.
- Network: authorized network state and relevant alerts.
- Recent activity: bounded, privacy-aware activity from explicit providers.

Launcher must not calculate or invent security, recovery, trust, privacy, or administrative state merely to populate a card. Each panel must identify its source, authorization boundary, stale/unavailable behavior, and privacy requirements.

## Phase 5 — Command Launcher

Add a keyboard-first command-palette experience for approved Launcher and GoreeCloud actions.

Planned command categories:

- Launcher navigation and settings.
- Application and shortcut actions.
- Workspace navigation and profile switching.
- GoreeCloud administrative actions exposed through approved contracts.
- Service operations such as view logs, restart service, create backup, deploy application, or check health only when the responsible service exposes an authorized action.
- Developer workflows and scripts through an explicit, permission-aware execution boundary.

Every command must carry clear provenance and authority. The command layer must distinguish between discovery/presentation and execution. Destructive, privileged, remote, or administrative actions require the responsible service's authorization and safety checks; Launcher must not silently gain equivalent authority merely because it can display the command.

Where natural-language command discovery is added, query understanding does not bypass the exact underlying action contract.

## Phase 6 — GoreeCloud Index presentation integration

Evolve beyond the current Index handoff while preserving Index authority.

Planned work:

- Native Glaze UI Index presentation surface or approved embedded contract.
- Immediate focus and keyboard-first navigation.
- Grouped results with provider/source provenance.
- Applications, actions, settings, files, documents, people, services, servers, containers, workflows, and other categories only through authorized Index providers.
- Filters, recent queries, suggestions, and direct result actions controlled by Index/provider contracts.
- Offline degradation that preserves local providers when available while remote providers fail independently.
- Explicit unavailable/error states rather than a hidden Launcher universal-search fallback.

Launcher-owned content should be exposed to Index through a versioned provider contract rather than Index reading private Launcher persistence directly.

## Phase 7 — Gestures and shortcut customization

Expand supported input methods across device classes:

- Swipe actions.
- Double-tap actions.
- Keyboard shortcuts.
- Mouse gestures where applicable.
- Touch gestures.
- Workspace navigation bindings.
- Approved command/workflow bindings.

Important actions require deterministic accessible alternatives. Gesture configuration must not depend on privileged accessibility services for ordinary Launcher behavior.

## Phase 8 — Themes, personalization, and icon presentation

Continue the first-party Theme Manager toward the approved Launcher personalization scope.

Planned areas:

- Current Stable Glaze UI appearance modes as they become applicable and accepted.
- Theme previews and safe reset/fallback behavior.
- Wallpaper-derived and user-selected palettes where supported.
- Icon-pack discovery/application.
- Icon masking, bounded scaling, shape/normalization controls, and fallback behavior.
- Layout density, typography scaling, transparency/blur controls, and animation intensity within Glaze UI accessibility requirements.
- Exportable theme/configuration data through validated non-executable formats.

Third-party application identities must remain recognizable. Theme/icon transformations must not misrepresent third-party branding or crop/distort important icon content.

## Phase 9 — Privacy, secure spaces, and managed visibility

Implement only real protection boundaries.

Planned concepts include:

- Hidden applications.
- Protected folders.
- Secure workspaces.
- Permission-aware visibility.
- Authentication-required areas where a real accepted authentication/authorization mechanism exists.
- User-controlled sensitive-content presentation.

A hidden item is not automatically secure. A protected area must be backed by an actual authorization/security boundary accepted through GoreeCloud Identity and Wardveil Security where applicable. Privacy-sensitive discovery, personalization, history, context, and recommendation signals remain governed by Privacy Shield.

## Phase 10 — Complete backup, restore, and continuity

Expand the existing partial portability/recovery work into a complete Launcher configuration lifecycle.

Target recoverable state includes, where implemented:

- Home pages and placements.
- Dock configuration.
- Folders and membership.
- Shortcuts.
- Widgets through safe provider rebinding/reconfiguration rather than stale AppWidget IDs.
- Theme Manager and icon-presentation settings.
- Gesture/shortcut mappings.
- Hidden-app and privacy-related Launcher preferences where appropriate.
- Layout-lock and Index Home-entry settings.
- Workspace profiles and profile-specific Launcher configuration.

Requirements:

- Versioned, documented formats.
- Strict validation before mutation.
- Forward migration for supported older versions.
- Safe interruption/crash recovery.
- Explicit conflict/rebinding policy.
- Local/offline-capable core backup and restore.
- Everkeep/Backup/Drive/Sync integration as optional authorized continuity/transport layers around the same validated Launcher-owned data model.

## Phase 11 — GoreeCloud platform integration

Evaluate and implement substantive integration with all applicable platform systems and products, including Manager, Metrics, Backup, storage services, Identity, Mesh, Everkeep, Wardveil Security, Privacy Shield, and approved administration services.

Each integration must document:

- The authoritative source system.
- The exact data/action contract.
- Identity and authorization requirements.
- Privacy purpose and data minimization.
- Security/trust requirements.
- Offline/unavailable behavior.
- Recovery/continuity implications.
- User-visible state and Glaze UI presentation.
- Validation evidence.

Shared authentication, notifications, search, settings, or cross-device experiences must use common platform contracts rather than ad-hoc Launcher-specific duplication when an approved shared capability exists.

## Phase 12 — Enterprise and administration

For managed GoreeCloud environments, implement organization policy only through explicit management and identity contracts.

Candidate capabilities:

- Organization policies.
- Role-based application visibility.
- User-specific/role-specific layouts.
- Managed applications.
- Central configuration.
- Deployment templates.
- Restricted Guest/User environments.

Managed policy must remain distinguishable from personal preferences and must be explainable in the UI. Launcher must not invent an independent enterprise authorization system.

## Phase 13 — Cross-platform Launcher family

The current repository remains the native Android implementation. Desktop, web, appliance, TV, foldable-specialized, or other Launcher experiences require separate platform architecture and acceptance rather than forcing Android implementation assumptions onto other systems.

Shared cross-platform concepts may include:

- Workspace/profile data models.
- Portable configuration schemas.
- Glaze UI interaction/design semantics.
- Index invocation/provider contracts.
- Identity, Mesh, Wardveil, Privacy Shield, Everkeep, and Manager integration contracts.

Platform-native interaction, accessibility, lifecycle, packaging, security, and system-integration rules remain controlling on each operating system.

## Phase 14 — Future AI-assisted features

AI-assisted Launcher behavior remains future scope.

Potential capabilities:

- Smart application suggestions.
- Automatic organization proposals.
- Workflow recommendations.
- Predictive query assistance through the appropriate search authority.
- Natural-language command discovery.
- Usage optimization suggestions.

AI behavior must be transparent, user-controlled, permission-aware, independently disableable where appropriate, and privacy-preserving. It must not silently upload sensitive Launcher behavior, installed-app inventory, workspace state, files, contacts, or usage history. AI suggestions must remain suggestions unless the user or an authorized policy explicitly approves an action.

## Release and Stable qualification boundary

No roadmap phase is complete solely because code has been merged. For applicable capabilities, completion requires evidence across:

- Source implementation.
- Automated validation.
- Android runtime validation.
- Representative physical-device validation.
- Process-death and recovery behavior.
- Security review and Wardveil integration where applicable.
- Privacy review and Privacy Shield integration where applicable.
- Identity/authorization validation where applicable.
- Everkeep continuity/recovery validation where applicable.
- Mesh/Manager integration validation where applicable.
- Current Stable Glaze UI and accessibility acceptance.
- Documentation and changelog reconciliation.
- Signed artifact/provenance verification for release stages.

Stable qualification remains a separately governed product-level decision and must not be inferred from completion of any individual roadmap phase.
