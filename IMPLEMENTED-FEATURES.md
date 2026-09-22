# GoreeCloud Launcher — Implemented Features

**Record type:** Repository implemented-feature inventory  
**Repository:** `GoreeCloud/launcher`  
**Lifecycle:** Development  
**Migration state:** **Authoritative on `main` after PR #201 merged as `009371938ac3cab041cfb0893ede68e66e211a4f` and default-branch readback verified this record.**  
**Repository authority baseline:** `main` at `009371938ac3cab041cfb0893ede68e66e211a4f` (PR #201 merged September 22, 2026).  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance, version 1.0, effective September 22, 2026.

## Interpretation

This file records capabilities that are implemented in the current GoreeCloud Launcher Development source. It does **not** claim Release Candidate, production, Stable, representative-device, or complete platform-integration acceptance unless those states are separately supported by authoritative evidence.

Partially implemented capabilities remain open obligations in `PLANNED-FEATURES.md`. The same capability may therefore be described here for the portion that exists and in `PLANNED-FEATURES.md` for the portion that remains incomplete.

## Current verified source baseline

The current repository `main` head is `009371938ac3cab041cfb0893ede68e66e211a4f`, the documentation/governance-only merge of PR #201. The latest source-bearing Launcher runtime remains `ec6640dda8522244d57a947db083aecb8b9cfe33`, the merge of PR #199, **Persist Universal Search provider preferences**. PR #199 exact head `5646ce68d998ec96797d29a8c470df96c6ceac57` passed Android CI run #620 / `35710385034` across validation, build, unit/schema checks, Development APK staging, Android 16 Room/runtime, and Android 16 transition-performance emulator lanes before merge. PR #201 exact head `bb9f5f7d2a91b771875b2aa4222d98b012bb7bda` passed Android CI run #635 across validation/build/unit/schema/APK staging plus both Android 16 emulator lanes before the repository-record migration was merged.

This is Development evidence. It does not establish physical-device performance, complete accessibility, personal/work/Shelter/private-space acceptance, Quickstep/Recents compatibility, complete Integral Platform System acceptance, protected production signing/distribution, Release Candidate, production, or Stable qualification.

## Implemented capabilities

### Native Android launcher foundation

- Native Android launcher implementation using Kotlin and Jetpack Compose, with platform-native Android contracts where required.
- Android HOME-role onboarding and lifecycle-aware default-HOME state handling.
- Scoped Android package visibility for `MAIN` + `LAUNCHER` activities instead of broad `QUERY_ALL_PACKAGES` access.
- `LauncherApps`-based launchable-application discovery across available profiles, package/profile refresh handling, and launchable-activity deduplication.
- Application launching from Launcher-owned Home, Apps, Search, and supported secondary-page surfaces.

### Home, workspace, pages, and Dock

- Wallpaper-backed primary Home surface using Android system wallpaper presentation without wallpaper or storage privileges.
- Persisted Favorites and a bounded five-item Dock.
- Persisted Home-grid presets and application presentation settings.
- Long-press placement management with accessible earlier/later controls.
- Direct primary-Home drag placement into configured grid cells, including guarded occupied-cell swaps and empty-cell placement.
- Persisted Home layout lock that gates implemented workspace mutation paths while ordinary launching and page selection remain usable.
- Five-second intentional Home hold path for unlocking with progressive feedback, with Settings retained as the deterministic non-gesture path.
- Room-backed authoritative workspace cutover/read foundations.
- Multi-page Home projection, page selection, empty secondary-page creation, guarded secondary-page reordering, and guarded deletion of eligible empty secondary pages while rank-zero primary Home remains protected.
- Compact/lazy page selector with accessibility context.
- Secondary-page app launch and guarded secondary-page movement/cell operations.
- Guarded primary-Home compatibility-to-spatial migration with persistent configured-grid placement.
- Development presentation of unsupported workspace-item counts instead of silently hiding their existence.

### Apps surface and profile-aware presentation

- Separate Apps surface with local application filtering and launching.
- Persisted Grid, Compact, List, and Category presentation modes and related density/label/spacing controls.
- User Apps / Work Apps projection from Android `LauncherApps` inventory when non-primary profile inventory exists.
- Search result identity labels that distinguish User and Work application matches where applicable.
- Removal of the duplicate rendered drawer-search control so the Launcher-owned Universal Search surface remains the primary general Search experience.

### Launcher settings and appearance

- Separate scrollable Launcher Settings surface.
- Persisted Home-grid, Apps-grid, icon-size, label-visibility, appearance, layout-lock, and Launcher Universal Search entry-mode preferences where currently implemented.
- System / Light / Dark appearance selection and a reachable native Theme Manager path.
- Repository-level GLAZE UI V1.6 source mapping, material/accessibility policy, and validation guard, subject to the still-open downstream acceptance boundaries recorded in `PLANNED-FEATURES.md`.

### Launcher-owned Universal Search foundation

GoreeCloud Launcher owns the user-facing Universal Search experience. Current implemented Development capabilities include:

- A distinct Launcher-owned Universal Search surface that remains usable without GoreeCloud Search or GoreeCloud Index.
- Installed-app search backed by Android `LauncherApps` inventory.
- Trusted local Launcher actions/settings destinations.
- Deterministic local ranking, aggregation, deduplication, and fail-soft provider behavior.
- Cooperative cancellable asynchronous provider execution under caller/provider lifecycle control.
- Profile-identifying User / Work subtitles for installed-app results.
- Provider contract v1.0 descriptive metadata covering provider identity, provenance, offline behavior, authorization requirement, remote-processing declaration, query-retention declaration, and fail-closed duplicate/version compatibility evaluation.
- Privacy-first provider policy that limits automatic typed-query fan-out to reviewed providers that are local-only, require no authorization, perform no remote processing, and retain no query; network, remote-processing, retaining, authorization-requiring, and third-party providers are classified for explicit user handoff instead.
- UI-facing result-presentation model that keeps already-ranked application matches separate from Launcher action/settings results and can expose enabled explicit-handoff providers descriptively without invoking them or attaching the query payload.
- Versioned fail-closed serialization contract for provider enablement and explicit provider order, preserving absent-versus-explicit-empty semantics while excluding typed queries, results, history, usage/frequency signals, credentials, grants, and provider payloads (PR #198).
- Launcher-local dedicated Preferences DataStore persistence for that provider enable/order snapshot, with explicit `read`, `set`, and `clear` boundaries; absence remains distinct from an explicitly empty enabled-provider selection and malformed/unsupported stored data fails closed (PR #199).

The PR #199 provider-control store remains deliberately separate from the strict portable-preference v1 backup/recovery contract. Portable adoption remains planned work.

### Privacy and local-first boundaries

- Core Launcher operation remains offline-capable and does not require a GoreeCloud server or cloud account.
- Current source has no Android `INTERNET` permission for core Launcher behavior.
- No advertising, sponsorship, promoted placement, affiliate ranking, mandatory analytics, attribution, or behavioral-tracking dependency is part of the documented Launcher product model.
- Search provider controls added through PRs #188, #198, and #199 do not persist typed queries, results, history, credentials, authorization grants, provider payloads, or usage/frequency signals.

### Branding and asset provenance

- Canonical Launcher visual assets are governed from the GoreeCloud branding-assets repository; the Launcher repository retains traceable Android derivatives required for the application.
- Repository provenance metadata and validation guard prevent Launcher-local derivatives from silently becoming a competing canonical branding source.

### Build and validation foundations

- Repository CI includes privacy, HOME-manifest, identity, GLAZE UI, Room/schema, lint, unit-test, debug-build, and Android 16 managed-emulator coverage for exercised Development paths.
- Evidence from source, CI, APK staging, and managed emulators remains exact-revision-bound and is not treated as physical-device, production, or Stable acceptance.

## Evidence highlights

| Change | Evidence | Implemented result |
| --- | --- | --- |
| PR #180 | Exact head `e2c8e613da320f020bc4f1960147eb03a6012685`; Android CI `35693613631`; merge `92e5c50fc82a2eb369a891e4afc3e1ec1a49dba7` | Launcher label/icon safe-area work, bounded icon warming, browse-order preservation, User/Work Search identity labels |
| PR #181 | Merge `a8d87cc7cd5a9c05bbb3d4bf699ad9ee36fcc15d`; exact-head CI `35695886062` | Rendered Universal Search moved to cancellable asynchronous provider execution |
| PR #182 | Exact head `58fb0430d8c0a6b6f06e01209ac4606a895459df`; CI `35697136234`; merge `78529be0917c072cd18846c2b8c213a1d3bc2ad1` | User/Work Apps projection and removal of duplicate drawer Search UI |
| PR #184 | Exact head `52bb20c19182b1a362561083949acaed3811cf6e`; CI `35699487607`; merge `e7221284f3eb7b9763923c469fb0ea9a555a7739` | Provider contract v1.0 metadata and fail-closed catalog evaluation |
| PR #188 | Exact head `cf2a608fcf6f590f5929ad69fa15279b0c082797`; CI `35700543213`; merge `0b54dffa1cb73d97c8d3a035807bf29b429e73e3` | Privacy-first automatic-local vs explicit-user-handoff provider policy |
| PR #195 | Exact head `f37fa1af628bf272946c79bccf57e3a846ae3ba6`; CI `35702386949`; merge `957f21a7a8904ee455c029e61d8a45466b725ed9` | UI-facing Search grouping and descriptive non-invoking explicit-handoff entries |
| PR #198 | Exact head `74cf28e2cc989e3e88d3cdd3e252dd69be45a71e`; CI `35707597053`; merge `d2600bc3f0b2fce6d3c8d524a8aef536e43cd1cb`; merged-main CI `35708430142` | Versioned provider enable/order serialization contract |
| PR #199 | Exact head `5646ce68d998ec96797d29a8c470df96c6ceac57`; CI `35710385034`; merge `ec6640dda8522244d57a947db083aecb8b9cfe33` | Dedicated DataStore persistence for provider enable/order controls |

## Material limitations

The following remain incomplete and therefore are **not** represented here as fully implemented: mature cross-page drag/drop; complete folders/smart folders; full AppWidgetHost/widget editing; pinned/dynamic shortcuts; complete work/private-profile behavior across all Launcher capabilities; complete Theme Manager/icon-pack behavior; complete portable backup/recovery; rendered provider management; external provider discovery and invocation; provider-specific consent flows; Search recents/history/context; complete Integral Platform System runtime acceptance; representative-device accessibility/performance/power validation; Quickstep/Recents acceptance; protected production signing/distribution; Release Candidate; production; and Stable qualification.

See `PLANNED-FEATURES.md` for the open obligations and acceptance gates.
