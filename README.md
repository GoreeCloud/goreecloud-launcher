# GoreeCloud Launcher

GoreeCloud Launcher is GoreeCloud's privacy-first, original Android HOME application and intended native home, application-navigation, personalization, and contextual-access experience for GoreeCloud devices. It is also a primary first-party Android entry point into **GoreeCloud Index**, the canonical GoreeCloud unified/universal search and indexing system. Launcher is not a fork or visual clone of another launcher.

## Status

**Development — advancing toward Release Candidate; not a signed production or Stable release.**

The repository contains a usable native daily-launcher foundation, rebuilt Home / Apps / Launcher Settings surfaces, guarded Room-authority multi-page workspace foundations, a Development Launcher-to-Index universal-search handoff, bounded portable restore/recovery work, and an active **GLAZE UI V1.3 (`1.3.0`) Adaptive Resonance migration candidate** using the inherited V1.2 Frosted Neutral material foundation. The current native Theme Manager supports System, Light, Dark, and Deep Dark. Passing source, CI, JVM, emulator, or repository contract checks does not establish rendered/native accessibility, representative physical-device acceptance, complete GoreeCloud platform integration, production signing, Release Candidate qualification, or Stable qualification.

The complete approved target capability inventory is maintained in [FEATURES.md](FEATURES.md). Target capabilities are not implementation claims unless separately identified as current Development behavior and supported by repository evidence.

## Product role

Launcher is intended to become the personalized front door to GoreeCloud: bringing applications, files, people, devices, search, information, services, privacy controls, security state, continuity features, and contextual actions together within one adaptive interface.

**GoreeCloud Index owns universal search orchestration and indexing.** Launcher owns launcher interaction/presentation and may expose Launcher-specific searchable context through explicit provider contracts. GoreeCloud Search remains the Internet/Web/current-information provider that Index may invoke when authorized.

Launcher intelligence and personalization must remain transparent and user-controlled.

## Product rules

- No ads, sponsorships, promoted apps, affiliate placement, or monetized search ranking.
- No behavioral advertising or mandatory analytics.
- Core Home and local application use remain offline-capable.
- No required GoreeCloud server or account for core launcher use.
- Minimal, documented permissions; current source has no Android `INTERNET` permission.
- No broad `QUERY_ALL_PACKAGES` access for ordinary launcher discovery.
- Universal search is delegated to GoreeCloud Index rather than duplicated as a hidden Launcher-owned index/ranking engine.
- All GoreeCloud logos/icons/artwork are canonical in `GoreeCloud/goreecloud-branding-assets`; this repository carries only traceable Android derivatives required by Launcher.
- GLAZE UI is the Design Center authority for applicable interface behavior.
- GoreeCloud Manager, Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Mesh, and GoreeCloud Identity retain their applicable platform authorities.
- A platform integration is not considered implemented merely because it appears in approved scope or UI copy.

## Current daily-launcher foundation

Current Development source includes:

- Android HOME activity and user-controlled `ROLE_HOME` onboarding;
- lifecycle-aware default-HOME status;
- scoped Android package visibility for `MAIN` + `LAUNCHER` activities and `LauncherApps` discovery across available profiles;
- package/profile lifecycle refresh and stable launcher-item deduplication;
- a wallpaper-backed **Home** surface;
- a separate **Apps** surface with local label/package filtering and launching;
- a separate scrollable **Launcher Settings** surface;
- persisted Home grid presets, Apps-grid columns, app-label visibility, icon-size preference, System / Light / Dark / Deep Dark appearance, Home layout-lock state, and GoreeCloud Index Home-entry mode;
- ordered persisted Home Favorites and a five-item Dock;
- long-press placement management with accessible earlier/later controls;
- a persisted **Lock Home screen layout** policy protecting current workspace mutations while leaving launching and page selection available;
- a visible locked-state Home control with intentional five-second hold-to-unlock feedback plus a deterministic Settings path;
- terminal-Room multi-page HOME observation, page selection, protected-primary/secondary-page reordering, empty-page creation/deletion, and secondary application pages;
- bounded secondary-to-secondary page movement, nearest-free-cell movement, and guarded exact one-cell movement;
- persisted GoreeCloud Index Home entry choices for **Permanent on Home** and **Swipe down only**;
- a **Search GoreeCloud** Home affordance in Permanent mode;
- one-finger downward Home gesture wired to the same Index handoff;
- bounded package visibility for the Index search action without broad package access;
- GLAZE UI V1.3 Stable integration provenance, inherited geometry/target mapping, inherited Frosted Neutral material primitives, Adaptive Resonance contract provenance, and native Theme Manager migration work;
- a reachable Settings → Theme Manager path with saveable/fail-closed sub-destination restoration, direct persisted four-mode theme selection, concise preview semantics, and a non-actionable selected-state surface;
- bounded local portable workspace/preference validation, persistence, and interruption-recovery foundations; and
- Android system wallpaper presentation through the native window-wallpaper mechanism without requesting wallpaper/storage privileges.

## GLAZE UI V1.3 design boundary

Launcher targets **GLAZE UI V1.3 / `1.3.0` Stable — Adaptive Resonance** at exact Stable integration revision `fc7cc91d2eace8da2371371c2855c24cbcb326a1`. **V1.2 / `1.2.0` is the rollback baseline.**

The current migration preserves the inherited V1.2 material rule:

> **Neutral glass is the material; color is an accent.**

The active native mapping:

- records the V1.3 Adaptive Resonance contract and Stable aggregate entrypoints;
- retains inherited structural spacing/radius semantics and 8/16/24/32 dp optical geometry references;
- retains the 48 dp ordinary interaction floor and 56 dp Touch Assistance/far-view target;
- supports System, Light, Dark, and Deep Dark structural appearances;
- uses translucent Frost White for light neutral material;
- uses neutral graphite/deep-neutral material for Dark and Deep Dark;
- uses neutral separator lines for degradation/effects-free presentation;
- keeps chromatic color out of the default material substrate;
- restricts current Theme Manager decorative color to a bounded Ice Blue accent; and
- does not derive adaptive material color or semantic state from wallpaper, application content, privacy/security state, or other producer truth.

GLAZE UI governs presentation and interaction only. It cannot manufacture privacy, security, identity, recovery, authorization, availability, or platform state owned by another GoreeCloud system or Android.

Current V1.3 source mapping remains **Development evidence**. Complete rendered visual review, Reduced Motion, Reduced Transparency, Increased Contrast, forced-color/native-equivalent behavior, large-text/reflow, RTL/localization, runtime Touch Assistance resolution, TalkBack/Switch Access, responsive phone/tablet/foldable composition and reachability, performance fallback, representative physical-device validation, verified V1.2 rollback, and Human Visual Excellence remain acceptance gates.

See [docs/glaze-ui-adoption.md](docs/glaze-ui-adoption.md).

## Home, Apps, and Settings

### Home

Home uses the system wallpaper behind the launcher-owned surface, renders the current app grid and Dock, and keeps placement management behind long-press. The protected primary compatibility Home remains rank zero while the separately gated primary-grid migration remains incomplete.

The one-finger downward Home gesture invokes GoreeCloud Index in both supported entry modes. **Permanent on Home** additionally keeps the Search GoreeCloud control visible; **Swipe down only** removes that permanent control without replacing Index or changing search authority. If Index is unavailable, Launcher reports that state rather than silently substituting a rival universal search engine.

### Apps

Apps presents the launchable application inventory in a configurable grid and supports a narrow local filter by label/package. This **Search apps** filter is a Launcher navigation feature, not GoreeCloud universal search and not an alternative Index provider/ranking pipeline.

### Launcher Settings and Theme Manager

Current persisted settings include supported Home-grid presets, Apps columns, icon presentation, app-label visibility, System/Light/Dark/Deep Dark appearance, Home layout lock, and GoreeCloud Index Home entry mode.

Launcher Settings routes Appearance into the native Theme Manager through a saveable destination model. Stale/unknown restored destination values fail closed to Settings root. The selected appearance is presented as status rather than another persistence action; only a different appearance choice can invoke caller-owned theme persistence.

Icon-pack discovery/application, masking, wallpaper-derived palettes, environmental sampling, expression controls, and broader Theme Engine behavior remain separate work.

## Multi-page Room boundary

When terminal Room authority is active, Launcher can expose page selection, create empty pages, delete only revalidated empty non-primary pages, reorder secondary pages while keeping the protected primary page at rank zero, render secondary application pages, and request supported secondary spatial moves.

Room writes verify the protected primary compatibility projection and re-read the complete HOME page/item snapshot so concurrent changes, malformed placement, collisions, invalid bounds, or attempts to use primary Home as a secondary spatial source/target fail closed.

The Home layout lock is an additional Launcher mutation policy over these authoritative operations. It does not create a second workspace persistence authority. Mature drag/drop, complete primary-grid migration, folders, shortcuts, and AppWidgetHost support remain separately gated.

## GoreeCloud Index integration

**GoreeCloud Index is the canonical unified/universal first-party search and indexing authority.** Launcher is a first-party invocation/presentation surface and a potential provider of Launcher-owned application/action/settings/folder/widget context. Launcher does not own a separate cross-provider index, result-normalization system, or universal-ranking engine.

The current Development integration uses the explicit action contract `com.goreecloud.index.action.SEARCH`. Launcher can resolve production and Development Index package identities while testing. Broader Index provider classes remain separate Index work.

**GoreeCloud Search is the Internet/Web/current-information provider**, reached through Index when enabled and authorized.

## Privacy and security architecture

Core Launcher behavior remains local-first. Universal provider participation is controlled by GoreeCloud Index and the applicable source authority rather than by Launcher scraping other applications' private storage.

Current portable-restore work is intentionally bounded and fail-closed. It does not convert device/profile-resolved application identities into portable cross-device identities, does not claim Room + DataStore as one crash-atomic transaction, and does not establish accepted Everkeep recovery.

Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, GoreeCloud Mesh, and GoreeCloud Manager integration and acceptance remain independently gated in `goreecloud.platform.yaml`.

## Official Launcher identity

All GoreeCloud Launcher logos, icons, symbols, illustrations, and artwork are canonical only in **`GoreeCloud/goreecloud-branding-assets`**. The canonical Launcher asset is `products/launcher/app-icon.svg`. This repository is a consumer and may carry only traceable synchronized/generated/packaged Android derivatives required to build Launcher.

`branding/provenance.json` pins the canonical repository, asset path, and source blob used for current Android derivatives.

## Current Release Candidate blockers

Release Candidate qualification still requires closure and evidence for applicable gates, including:

- complete V1.3 component/state/material/adaptive application mapping and rendered acceptance;
- representative phone/tablet/foldable visual and interaction validation;
- TalkBack, Switch Access, keyboard/D-pad, large-text/reflow, RTL/localization, Reduced Motion, Reduced Transparency, Increased Contrast, and Touch Assistance acceptance;
- mature core workspace editing and recovery behavior required by approved RC scope;
- representative physical-device default-HOME, lifecycle, universal-search gesture, restore/recovery, and performance acceptance;
- verified V1.2 rollback on the Launcher integration path;
- applicable Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, and Manager integration/acceptance;
- production signing/provenance and release packaging; and
- canonical Drive acceptance/changelog reconciliation.

A version label alone is not RC evidence.

## Documentation

- [USER-MANUAL.md](USER-MANUAL.md) — current Development behavior and user guidance.
- [SPECIFICATIONS.md](SPECIFICATIONS.md) — architecture, scope, and authority boundaries.
- [FEATURES.md](FEATURES.md) — implemented Development behavior plus approved target scope.
- [BENEFITS.md](BENEFITS.md) — current and intended benefits.
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — product completeness objectives.
- [Glaze UI adoption](docs/glaze-ui-adoption.md) — current V1.3 migration and acceptance boundary.
- `docs/` — architecture, persistence, design-system, validation, and implementation records.

Canonical project specifications and acceptance/change records are maintained in the authorized GoreeCloud Google Drive project hierarchy.

## Build baseline

Current Android source uses Kotlin, Jetpack Compose, AndroidX Room/SQLite, DataStore, and Android SDK 36-era tooling. CI enforces privacy/manifest/identity/Glaze/Room guards, Android lint, JVM tests, debug assembly, Room schema validation, and an Android 16 runtime-emulator suite.

## License

GPL-3.0. See `LICENSE`.
