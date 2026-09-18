# GoreeCloud Launcher — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Verified current stabilization base: `main` at `38f51d63124363bef65d202640fdadce8697fa59`, including the asynchronous shared icon cache and off-main-thread/scoped LauncherApps inventory refresh integrations.
- GitHub issue #80 is the current Android core-stabilization gate and records physical-device blockers for HOME behavior, drawer gestures/transitions, local app search, drawer presentation, app discovery, performance, and Quickstep/Recents compatibility.
- Glaze UI 1.5.1 and Compact drawer work remain separate stacked candidates unless and until clean current-main integration is verified. Do not treat their historical green checks as current `main` acceptance.

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
