# GoreeCloud Launcher — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Verified baseline for this notes change: `main` at `32967cf87e33ae33af89c3be412a42c33a6f93d1`.
- GitHub issue #80 is the current Android core-stabilization gate and records physical-device blockers for HOME behavior, drawer gestures/transitions, local app search, drawer presentation, app discovery, performance, and Quickstep/Recents compatibility.
- Draft PR #109 has exact-head green Development validation for Glaze UI 1.5.1 source adoption, and draft PR #110 has green Development validation for Compact drawer presentation. Both are stacked changes rather than verified `main` integration.

## Active stabilization observations

- Do not treat green stacked PR validation as `main` integration or physical-device acceptance.
- Android `LauncherApps` remains installed-application inventory authority; local installed-app search must remain useful when GoreeCloud Index/Search are unavailable.
- Workspace state, portable preference/restore work, and launcher presentation must preserve their existing authority boundaries.
- Representative-device performance, default-HOME behavior, gesture reliability, accessibility, and post-integration regression evidence remain required.

## Maintenance notes

Prefer clean, reviewable integration from the current authoritative base rather than merging stale or superseded stacked branches out of sequence. Keep release status truthful after every integration and re-run exact-head Android/contract validation when a source-bearing candidate changes.
