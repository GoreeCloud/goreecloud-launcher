# GoreeCloud Launcher visual identity

## Current status

The artwork under `branding/source/` is a **Development identity candidate**, generated directly in the authoritative `GoreeCloud/goreecloud-launcher` repository for review. It is not yet the approved official GoreeCloud Launcher identity.

The candidate must not be represented as final/official until it receives explicit product approval and passes the applicable GoreeCloud visual-identity review.

## Candidate Identity DNA

- **Family:** System Utility.
- **Primary idea:** access / launch / passage into applications and activities.
- **Identity Lock:** an open rounded portal with a deliberate upper-right opening, crossed by one diamond-shaped activity tile.
- **Negative-space signature:** the portal opening remains visibly distinct even in monochrome.
- **Material personality:** quiet, durable and precise rather than highly decorative.
- **Color family:** deep indigo foundation with a cyan activity accent; shape remains authoritative when color is removed.
- **Avoided motifs:** Android robot, generic house, generic four-square app grid, magnifying glass, copied launcher imagery, and the GoreeCloud corporate logo as the app symbol.

## Canonical candidate source

`source/goreecloud-launcher-icon.svg` is the review source for this candidate. Android vector/adaptive resources are hand-derived from the same geometry and are validated as repository-local derivatives.

The SVG is intentionally vector-first and text-free. The primary mark is designed to remain legible under circular, squircle, rounded-square and manufacturer adaptive-icon masks.

## Android derivatives

The Development APK uses:

- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml`

The full-color derivative uses the indigo background plus white portal and cyan activity tile. The Android 13+ monochrome derivative preserves the portal/tile silhouette as one themed foreground.

## Approval gate

Before promotion from candidate to official identity, review should verify small-size recognition, family/ecosystem distinction, monochrome clarity, adaptive-mask cropping, light/dark wallpaper contexts, default-Home chooser presentation, Settings/installer presentation, and distinction from neighboring GoreeCloud applications and recognizable third-party launchers.
