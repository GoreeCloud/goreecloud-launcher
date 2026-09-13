# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application and the intended native home, application-navigation, personalization, and contextual-access experience for GoreeCloud devices. It is also a primary first-party Android entry point into **GoreeCloud Index**, the canonical GoreeCloud unified/universal search and indexing system. Launcher is not a fork or visual clone of another launcher.

## Status

**Development — not a signed production/Stable release.**

The repository contains a usable native daily-launcher foundation, rebuilt Home / Apps / Launcher Settings product shell, guarded Room-authority multi-page workspace foundations, a Development Launcher-to-Index universal-search handoff, and a repository-local **GLAZE UI V1.4 (`1.4.0`) migration** with System / Light / Dark / Deep Dark appearance support and bounded native Optical Intelligence.

Passing source, CI, unit, build, schema, or emulator checks does not establish complete rendered/native/accessibility, representative physical-device, platform-integration, signed-release, or Stable qualification.

The complete approved target capability inventory is maintained in [FEATURES.md](FEATURES.md). Target capabilities are not implementation claims unless separately identified as current Development behavior and supported by repository evidence.

## Product role

Launcher is intended to become the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together within one adaptive interface.

**GoreeCloud Index owns universal search orchestration and indexing.** Launcher owns launcher interaction/presentation and may expose Launcher-specific searchable context through explicit provider contracts. GoreeCloud Search remains the Internet/Web/current-information provider that Index may invoke when authorized.

Launcher intelligence and personalization should remain transparent and user-controlled.

## Product rules

- No ads, sponsorships, promoted apps, affiliate placement, or monetized search ranking.
- No behavioral advertising or mandatory analytics.
- Core Home and local application use remain offline-capable.
- No required GoreeCloud server or account for core launcher use.
- Minimal, documented permissions; current source has no Android `INTERNET` permission.
- No broad `QUERY_ALL_PACKAGES` access for ordinary launcher discovery.
- Universal search is delegated to GoreeCloud Index rather than duplicated as a hidden Launcher-owned index/ranking engine.
- All GoreeCloud logos/icons/artwork are canonical in `GoreeCloud/goreecloud-branding-assets`; this repository carries only traceable Android derivatives required by Launcher.
- **GLAZE UI V1.4 is the current design-system target for Launcher.**
- Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, and GoreeCloud Mesh govern their applicable platform boundaries.
- A platform integration is not considered implemented merely because it appears in approved scope or UI copy.

## Current rebuilt daily-launcher shell

Current Development source includes:

- Android HOME activity and user-controlled `ROLE_HOME` onboarding;
- lifecycle-aware default-HOME status;
- scoped Android package visibility for `MAIN` + `LAUNCHER` activities and `LauncherApps` discovery across available profiles;
- package/profile lifecycle refresh and stable launcher-item deduplication;
- wallpaper-backed **Home**, separate **Apps**, and scrollable **Launcher Settings** surfaces;
- locally persisted Home grid presets, Apps-grid columns, app-label visibility, icon-size preference, System / Light / Dark / Deep Dark appearance, Home layout-lock state, and GoreeCloud Index Home-entry mode;
- ordered persisted Home Favorites and Dock;
- long-press placement management with accessible earlier/later controls;
- a persisted **Lock Home screen layout** policy that blocks current placement/page mutations while preserving app launching, page selection, Apps navigation, and Settings access;
- terminal-Room multi-page HOME observation, page selection, protected-primary/secondary-page reordering, empty-page creation/deletion, and secondary application pages;
- secondary-page nearest-free-cell and guarded exact one-cell movement;
- GoreeCloud Index Home-entry choices for **Permanent on Home** and **Swipe down only**;
- a **Search GoreeCloud** Home affordance when Permanent mode is selected;
- a one-finger downward Home gesture wired to the same Index handoff;
- bounded package visibility for Index search without broad package access;
- a reachable Settings → Theme Manager path with saveable/fail-closed sub-destination restoration and direct persisted System/Light/Dark/Deep Dark selection;
- native GLAZE UI V1.4 spacing, geometry, interaction-target, structural appearance, and Optical Intelligence mappings; and
- Android system wallpaper presentation through the native window-wallpaper mechanism without requesting wallpaper/storage privileges.

## GLAZE UI V1.4 boundary

Launcher targets **GLAZE UI V1.4 — Optical Intelligence / `1.4.0`** at exact merged Stable Glaze source revision `84cb3db4884042f0fa25ed6d475a127fb110f596`.

The native mapping includes:

- inherited spacing, radius, and interaction-target behavior;
- System, Light, Dark, and Deep Dark structural appearances;
- bounded Deep Teal + Soft Amber non-semantic atmosphere;
- a deterministic native `GlazeOpticalV14` resolver;
- Content-Aware Frost based on already-derived background complexity/luminance inputs;
- Semantic Blur Protection;
- bounded chromatic depth and daypart warmth;
- Environmental Color Memory influence capped at 8%;
- Reduced Transparency and Forced Colors fail-closed to solid-accessible treatment; and
- Increased Contrast suppression of decorative warmth/tint.

The optical resolver does **not** collect telemetry, camera data, wallpaper pixels, analytics, or remote context. Any future context adapter remains separately governed by Launcher privacy/security authority, Privacy Shield, Wardveil Security, and Android permissions/capability boundaries.

This migration does **not** make Launcher Glaze-conformant or production-eligible. The Platform Contract remains `nonconformant` until Launcher-specific rendered, accessibility, adaptive, representative-device, platform-integration, release, and production acceptance is complete.

### V1.4.1 human-validation boundary

Shared Glaze UI human/manual/physical-device/subjective optical validation is assigned to the V1.4.1 hardening track and is not represented as passed V1.4.0 evidence. Launcher-specific product acceptance may remain stricter and is not waived by that shared deferral.

See [docs/glaze-ui-adoption.md](docs/glaze-ui-adoption.md) for the exact migration and acceptance boundary.

## Theme Manager

The reachable Theme Manager supports System, Light, Dark, and Deep Dark. Settings navigation remains saveable; unknown/stale destinations fail closed to Settings root. Only selecting a different appearance invokes caller-owned persistence.

Theme preview atmosphere is decorative only and cannot establish privacy, security, identity, recovery, availability, selection, or other authoritative state.

Still separate from the current bounded implementation: icon-pack discovery/application, icon masking, per-app icon replacement, user/wallpaper palette systems, expression controls, approved contextual optical adapters, and broader Theme Engine behavior.

## Complete app discovery boundary

Android 11+ package visibility requires launchers to declare which external activity class they need to discover. GoreeCloud Launcher declares a scoped `MAIN` + `LAUNCHER` visibility query and continues to use `LauncherApps` for actual launchable-activity discovery.

The Launcher-to-Index integration adds only a scoped visibility declaration for `com.goreecloud.index.action.SEARCH`. It does **not** add `QUERY_ALL_PACKAGES`, Internet access, analytics, or an installed-application export path.

## Home, Apps, and Settings

### Home

Home uses the system wallpaper behind the launcher-owned surface, renders the current app grid and Dock, and keeps placement management behind long-press. The protected primary compatibility Home remains rank zero while the separate primary-grid migration is still pending.

The one-finger downward Home gesture invokes GoreeCloud Index in both supported entry modes. **Permanent on Home** additionally keeps the Search GoreeCloud control visible; **Swipe down only** removes that permanent control without replacing Index or changing search authority. If Index is unavailable, Launcher reports that state instead of silently substituting a rival universal search engine.

### Apps

Apps presents the launchable application inventory in a configurable grid and supports a narrow local filter by label/package. This **Search apps** filter is a Launcher navigation feature, not GoreeCloud universal search and not an alternative Index provider/ranking pipeline.

### Launcher Settings

Current persisted settings include supported Home-grid presets, Apps columns, icon presentation, app-label visibility, System/Light/Dark/Deep Dark appearance, Home layout lock, and GoreeCloud Index Home-entry mode. The Index setting controls only Launcher-owned invocation presentation; it does not move provider/index/ranking authority out of GoreeCloud Index.

## Multi-page Room boundary

When terminal Room authority is active, Launcher can expose page selection, create empty pages, delete only revalidated empty non-primary pages, reorder secondary pages while keeping the protected primary page at rank zero, render secondary application pages, and request supported secondary spatial moves.

Room writes continue to verify protected placement invariants and re-read authoritative state so concurrent changes, malformed placement, collisions, invalid bounds, or unsupported primary/secondary moves fail closed.

The Home layout lock is an additional Launcher mutation policy over these authoritative operations. It does not create a second workspace persistence authority.

## GoreeCloud Index universal search integration

**GoreeCloud Index is the canonical unified/universal first-party search and indexing authority.** Launcher is an invocation/presentation surface and potential provider of Launcher-owned application/action/settings/folder/widget context. Launcher does not own a separate cross-provider index, result-normalization system, or universal ranking engine.

The current Development integration uses `com.goreecloud.index.action.SEARCH` and supports both production and Development Index package identities while testing.

**GoreeCloud Search is the Internet/Web/current-information provider**, reached through Index when enabled and authorized. Neither Launcher nor Index should upload unrelated local result payloads merely to obtain local search results.

Sensitive and permissioned providers remain governed by their own Android and GoreeCloud authority boundaries.

## Official Launcher identity

All GoreeCloud Launcher logos, icons, symbols, illustrations, and artwork are canonical only in **`GoreeCloud/goreecloud-branding-assets`**. The current canonical Launcher asset is `products/launcher/app-icon.svg`. This repository is a consumer and may carry only traceable synchronized/generated/packaged Android derivatives required to build Launcher.

`branding/provenance.json` pins the canonical repository, asset path, and source blob used for the current Android derivatives. Any future visual revision must be authored, reviewed, and approved in `goreecloud-branding-assets` first.

## Privacy and search architecture

Core Launcher behavior remains local-first. Universal provider participation is controlled by GoreeCloud Index and the applicable source authority rather than by Launcher scraping other applications' private storage. Contacts require explicit authorization. Media must use scoped Android APIs. Files/documents must use supported provider access. Work/private profiles must remain appropriately isolated.

There is no sponsored or paid ranking.

## Continuity and restore boundary

Launcher contains bounded Development workspace-placement and preference portability plus guarded restore/recovery work. This includes versioned formats, validation-before-write, review/apply consistency, recovery journaling, readback verification, and fail-closed handling of unsupported identity/rebinding states.

This is not complete product recovery qualification. Clean-target/cross-device application/profile/folder/shortcut/widget identity reconstruction, complete Launcher-owned backup scope, accepted Everkeep integration, production startup sequencing, representative-device failure injection, and production recovery acceptance remain outstanding.

## Current limitations

Still incomplete or separately gated:

- mature cross-page drag/drop and live cell/span editing;
- primary compatibility-page grid migration and primary↔secondary spatial movement;
- populated-page deletion with confirmation/recovery/undo;
- folders, shortcuts, widgets/AppWidgetHost, and richer workspace editing;
- complete Theme Engine behavior, icon-pack support, masking, palette systems, and expression controls;
- approved V1.4 contextual optical-signal adapters where beneficial;
- complete Reduced Motion / Reduced Transparency / Increased Contrast / native forced-color integration across all surfaces;
- 200% text/reflow, RTL/localization, Touch Assistance preference resolution, TalkBack, Switch Access, and keyboard/D-pad acceptance;
- representative phone/tablet/foldable and physical-device acceptance;
- V1.4.1 human optical/polish validation applicable to Launcher;
- embedded/polished Index result presentation beyond the current activity handoff;
- Index providers for broader first-party/local content and GoreeCloud Search current-information integration;
- accepted cross-device Sync/Mesh/Identity continuity;
- complete Privacy Shield, Wardveil Security, Everkeep, and Manager integration acceptance;
- Android process-death/schema-upgrade recovery acceptance;
- signed release packaging/distribution; and
- production/Stable Launcher qualification.

## Documentation

- [USER-MANUAL.md](USER-MANUAL.md) — current Development behavior and user guidance.
- [SPECIFICATIONS.md](SPECIFICATIONS.md) — architecture, scope, and authority boundaries.
- [FEATURES.md](FEATURES.md) — implemented Development behavior plus approved target scope.
- [BENEFITS.md](BENEFITS.md) — current and intended benefits.
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — product completeness objectives.
- [Glaze UI adoption](docs/glaze-ui-adoption.md) — exact current-Stable migration and application acceptance boundary.
- `docs/` — architecture, persistence, design-system, validation, and implementation records.

Canonical project specifications and acceptance/change records are maintained in the authorized GoreeCloud project documentation hierarchy.

## Build baseline

Current Android source uses Kotlin, Jetpack Compose, AndroidX Room/SQLite, DataStore, and Android SDK 36-era tooling. CI enforces privacy/manifest/identity/Glaze/Room guards, Android lint, JVM tests, debug assembly, Room schema validation, and an Android 16 runtime-emulator suite.

## License

GPL-3.0. See `LICENSE`.
