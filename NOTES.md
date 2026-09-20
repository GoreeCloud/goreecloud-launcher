# GoreeCloud Launcher — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Latest source-bearing stabilization baseline: `f22d39d0801551470c0bb38c99d27e8de4172d62`, including the asynchronous shared icon cache, off-main-thread/scoped LauncherApps inventory refresh, hardened local installed-app search, immutable Android CI supply-chain integration, Platform Contract 0.4/all-nine-system reconciliation, the PR #120 workspace-tile drawer-gesture correction, and the integrated PR #123 bounded Home/app-drawer transition behavior. Earlier documentation-only PR #121/#122 changed documentation only and did not alter Launcher runtime behavior.
- GitHub issue #80 is the current Android core-stabilization gate and records physical-device blockers for HOME behavior, drawer gestures/transitions, local app search, drawer presentation, app discovery, performance, and Quickstep/Recents compatibility.
- Historical Glaze UI 1.5.1 and Compact drawer stacks remain provenance/candidates unless cleanly restacked and verified against current `main`. Authoritative source still maps GLAZE UI 1.1.0. Current Official Stable GLAZE UI V1.6 / 1.6.0 supersedes the earlier target; no historical stack is treated as current source.

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
- The manifest truthfully records the implemented GLAZE UI V1.1 / 1.1.0 mapping while declaring current Official Stable GLAZE UI V1.6 / 1.6.0 as the required migration target; historical V1.5.1 work is not treated as current conformance.
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
