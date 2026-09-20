# GoreeCloud Launcher — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Latest source-bearing stabilization baseline: `0a12c06781841924a4b4eeb2494735d9d44b2811`, integrating PR #128 on top of the prior Launcher stabilization line. The source now maps to exact Official Stable GLAZE UI V1.6 / 1.6.0 while application acceptance remains blocked. Earlier documentation-only integrations changed documentation only and did not alter Launcher runtime authority.
- GitHub issue #80 is the current Android core-stabilization gate and records physical-device blockers for HOME behavior, drawer gestures/transitions, local app search, drawer presentation, app discovery, performance, and Quickstep/Recents compatibility.
- Historical Glaze UI 1.5.1 and Compact drawer stacks remain provenance/candidates unless cleanly restacked and verified against current `main`. Authoritative `main` now carries the integrated first-party V1.6 / 1.6.0 source mapping; no historical stack is treated as current source or acceptance.

## Active stabilization observations

- Do not treat green stacked PR validation as `main` integration or physical-device acceptance.
- Android `LauncherApps` remains installed-application inventory authority; local installed-app search must remain useful when GoreeCloud Index/Search are unavailable.
- Workspace state, portable preference/restore work, and launcher presentation must preserve their existing authority boundaries.
- Representative-device performance, default-HOME behavior, gesture reliability, accessibility, and post-integration regression evidence remain required.

## Maintenance notes

Prefer clean, reviewable integration from the current authoritative base rather than merging stale or superseded stacked branches out of sequence. Keep release status truthful after every integration and re-run exact-head Android/contract validation when a source-bearing candidate changes.


## September 18 performance and search stabilization

- PR #115 merged the shared asynchronous launcher-icon cache, removing drawable-to-bitmap decoding from Compose rendering and invalidating cache entries on LauncherApps package changes.
- PR #116 merged an IO-backed inventory refresh worker with package-scoped reconciliation for ordinary package lifecycle changes and full refreshes for availability/profile-topology changes.
- The current local-search candidate restacks installed-app search hardening onto that authoritative base. It uses only Android LauncherApps label/package inventory, performs no network/Index/account/telemetry ranking, normalizes punctuation/diacritics deterministically, supports multi-term local matching, and shows an explicit empty result.
- These source/CI improvements do not satisfy issue #80 physical-device performance, HOME, gesture/transition, Quickstep/Recents, or full P0 acceptance by themselves.


## CI supply-chain stabilization — September 18, 2026

- PR #118 is integrated on `main`; Android CI uses Ubuntu 24.04 and immutable full-SHA references for checkout, Java setup, Gradle setup, and artifact upload.
- The Android 16 Room runtime job now independently verifies the checked-out revision before executing source.
- These controls do not change Launcher runtime behavior or satisfy issue #80 physical-device P0 acceptance.


## Platform Contract stabilization — September 19, 2026

- PR #119 updated the root manifest from legacy Contract 0.2/seven-system structure to accepted Contract 0.4/all nine Integral Platform Systems and is integrated on `main`.
- GoreeCloud Policy and GoreeCloud Observability are explicitly evaluated as applicable-blocked rather than silently omitted.
- Integrated PR #128 updates the manifest to the GLAZE UI V1.6 / 1.6.0 source mapping while keeping Glaze applicable-blocked and overall conformance nonconformant until repository-local acceptance exists; historical V1.1/V1.5.1 evidence is not rebound as V1.6 acceptance.
- The reusable Platform Contract workflow is repinned to the accepted central Contract 0.4 validator revision.
- This is governance and validation hardening only; issue #80 physical-device/default-HOME, gesture/transition, Quickstep/Recents, performance, and full P0 acceptance remain open.


## Workspace drawer gesture regression coverage — September 19, 2026

- PR #120 is integrated on `main` and adds Android 16 runtime coverage for the issue #80 path where a swipe-up gesture begins over an actual workspace application tile rather than empty Home chrome.
- The test requires the gesture to transition from primary Home into the app drawer and render both the Apps heading and Search apps field.
- Exact-head runtime validation exposed that the primary Home `LazyVerticalGrid` still accepted vertical scrolling and won the gesture when a swipe began on app content. The integrated PR #120 change disables user scrolling on that fixed Home grid so the parent Home swipe detector can receive drawer gestures across app tiles; the app drawer grid remains scrollable.
- Superseded candidate heads also exposed two test-only defects (a missing Compose `swipeUp` import and a non-`Unit` JUnit test signature). Those failed heads are audit history and are not accepted as runtime evidence.
- This remains automated Development regression evidence only. It does not establish representative physical-device gesture smoothness, transition quality, Quickstep/Recents coexistence, measured performance, accessibility acceptance, or Stable qualification.

## Home and drawer transition stabilization — September 19, 2026

- PR #123 is integrated on `main` and replaces the abrupt top-level Home/app-drawer surface swap with a bounded Compose animated transition.
- Home-to-drawer uses a short upward spatial transition plus fade, while drawer-to-Home reverses that spatial direction; Settings transitions remain a short fade.
- The existing gesture authority is unchanged: the fixed Home grid remains non-scrollable so parent Home swipe handling retains the workspace-tile gesture path, and the app drawer itself remains scrollable.
- Existing Android 16 runtime acceptance must still prove that a swipe beginning on workspace app content reaches the drawer and exposes Apps and Search apps after the transition.
- This is source-level transition behavior only. It does not establish representative physical-device smoothness, frame timing, measured jank/performance, reduced-motion acceptance, Quickstep/Recents coexistence, Release Candidate, production, or Stable qualification.


## Current Glaze authority stabilization — September 19, 2026

- PR #125 is integrated on `main` and adds a first-party `GlazeCurrentAuthority` boundary that pins current required GLAZE UI V1.6 / 1.6.0 to exact Stable source `a7180679ea851389e0f3004515f9a25f420e716d`.
- The existing native implementation remains truthfully identified as the V1.1 / 1.1.0 baseline at `15cc76d2bcd4065552dc31c77145b63f34d9e7b2`; the candidate does not relabel old tokens/components as V1.6.
- Unit and repository guards require migration to remain true until current consumer conformance is separately implemented and accepted.
- Root README/specification/platform records are reconciled away from older active Glaze 2.2/V1.5.1 claims.
- V1.6 implementation, rendered/accessibility/device/performance acceptance, platform-system runtime acceptance, signing, Release Candidate, production, and Stable qualification remain open.

- Accepted exact-head evidence for PR #125: Platform Contract run `35490207850` and Android CI run `35490207574` both succeeded before squash merge to authoritative main commit `e64c60d24bd61926209d9feb3f93709d2a55a953`.


## GLAZE UI V1.6 source mapping integration — September 20, 2026

- PR #128 is integrated on authoritative `main` as source-bearing commit `0a12c06781841924a4b4eeb2494735d9d44b2811`, mapping the prior V1.1 implementation baseline to exact Official Stable GLAZE UI V1.6 / 1.6.0 release source `a7180679ea851389e0f3004515f9a25f420e716d`.
- A first-party V1.6 presentation policy models bounded material simplification, Reduced Motion, Reduced Transparency, performance levels, focus visibility, large-text density yielding, and conservative interaction targets without creating privacy, security, authorization, connectivity, recovery, or workflow truth.
- The inherited Stable V1.6 release sources expose a 44 px-equivalent coarse target floor and 32 px pointer-compact floor. Launcher deliberately retains a stricter 48 dp general touch target and 56 dp accessibility-oriented target; those are product choices, not relabeled Glaze tokens.
- Retained Launcher palettes, atmosphere, radii, and 20/40 dp composition conveniences are explicitly application-owned. Source continuity is preserved without representing legacy product values as canonical V1.6 values.
- Theme Manager consumes the V1.6 presentation context so future authoritative Reduced Transparency/performance inputs can simplify raised material. The branch does not fabricate system preference detection; neutral defaults remain neutral until caller/platform state is actually supplied.
- Accepted pre-merge evidence for exact PR #128 head `0a13fd5f40e09de877bbf51823e2747873c74914`: Platform Contract run `35491439181` and Android CI run `35491438765` succeeded, including Android 16 runtime acceptance. Post-merge Platform Contract run `35491732732` succeeded on exact source merge commit `0a12c06781841924a4b4eeb2494735d9d44b2811`; post-merge Android CI run `35491732451` is still pending final completion at this reconciliation draft revision.
- Source migration does not establish V1.6 consumer conformance. Rendered/accessibility/adaptive/representative-device/performance/rollback/Human Visual Excellence/platform-system/signing/release acceptance remain open, and issue #80 remains the product stabilization gate.
