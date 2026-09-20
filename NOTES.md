# GoreeCloud Launcher — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Verified current stabilization base: `main` at `5343869c1dc5f31a10196f6b1992a848b47337db`, including the asynchronous shared icon cache, off-main-thread/scoped LauncherApps inventory refresh, hardened local installed-app search, and immutable Android CI supply-chain integration.
- GitHub issue #80 is the current Android core-stabilization gate and records physical-device blockers for HOME behavior, drawer gestures/transitions, local app search, drawer presentation, app discovery, performance, and Quickstep/Recents compatibility.
- Historical Glaze UI 1.5.1 and Compact drawer stacks remain provenance/candidates unless cleanly restacked and verified against current `main`. The root Platform Contract declaration was stale at 0.2/seven systems and is being reconciled in this stabilization branch to Contract 0.4/all nine systems without claiming runtime acceptance.

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

- This branch updates the root manifest from legacy Contract 0.2/seven-system structure to accepted Contract 0.4/all nine Integral Platform Systems.
- GoreeCloud Policy and GoreeCloud Observability are explicitly evaluated as applicable-blocked rather than silently omitted.
- Glaze UI is declared against current Official Stable V1.5 / 1.5.1 while keeping Launcher application acceptance blocked until rendered, accessibility, representative-device, performance/power, rollback, localization/RTL, and production evidence exists.
- The reusable Platform Contract workflow is repinned to the accepted central Contract 0.4 validator revision.
- This is governance and validation hardening only; issue #80 physical-device/default-HOME, gesture/transition, Quickstep/Recents, performance, and full P0 acceptance remain open.
