# GoreeCloud Launcher Product Identity

## Status

**Development identity candidate committed — explicit approval still required before official product-identity acceptance.**

GoreeCloud Launcher now has a product-specific candidate identity generated directly in its authoritative Git repository. The candidate is wired into Development Android builds so the APK can be distinguished from other installed applications during device testing.

The candidate is **not yet the approved official GoreeCloud Launcher identity**. Automatically generated or newly authored artwork requires review and explicit approval before it may be represented as official under the GoreeCloud visual-identity standard.

## Candidate source and provenance

Canonical candidate review source:

`branding/source/goreecloud-launcher-icon.svg`

The source was authored directly in `GoreeCloud/goreecloud-launcher` rather than produced as a chat attachment or stored only in a temporary/export location. `branding/README.md` records the candidate Identity DNA, derivative relationship, and approval boundary.

Android derivatives are repository-local and traceable to the same geometry:

- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml`

The Android manifest references the adaptive and round resources. Android 13+ variants include the monochrome/themed-icon derivative.

## Product-family role

GoreeCloud Launcher belongs to the GoreeCloud **System Utility** family. Its identity communicates the product's role as the personalized front door to GoreeCloud and as the native access point for applications, activities, navigation, discovery, search, and contextual actions.

The candidate deliberately does not place the GoreeCloud corporate logo inside an app-icon container.

## Candidate Identity DNA

The dominant visual idea is **access through a launch portal**.

The candidate uses an open rounded portal with a deliberate upper-right opening. One diamond-shaped activity tile crosses that opening. The opening and crossing tile are intended to form the long-term Identity Lock if the candidate is approved.

The design deliberately avoids:

- Android robot imagery;
- a generic home glyph;
- a generic four-square application grid;
- a magnifying glass as the primary application symbol;
- copied Pixel/Samsung/Nova or other launcher imagery;
- a corporate-logo substitute; and
- text or initials as the primary identity.

The material personality is restrained and durable. A deep indigo foundation supports a cyan activity accent, but the geometry is designed to remain recognizable when color is removed.

## Form requirements

The identity must:

- remain recognizable at small launcher sizes;
- have a strong, distinctive silhouette;
- tolerate circular, rounded-square, squircle, and manufacturer mask shapes through Android adaptive-icon behavior;
- preserve meaningful negative space and avoid hairline detail that disappears at small sizes;
- remain understandable in monochrome/themed-icon rendering;
- work in Light and Dark surrounding UI contexts;
- avoid embedding essential text or a wordmark into the primary app icon; and
- remain one recognizable identity across Android and future supported platforms.

## Quality review required before approval

Before the candidate can be called official, review must cover:

- legibility at small sizes;
- visual distinction from other GoreeCloud applications installed on the same device;
- visual distinction from Android, Pixel Launcher, Samsung One UI Home, Nova Launcher, and other recognizable third-party/system launcher identities;
- themed/monochrome rendering;
- adaptive-mask cropping across representative mask shapes;
- Light/Dark wallpaper contrast contexts;
- home-screen, application-drawer, Android default-Home chooser, Settings, and package-installer presentation; and
- consistency with current GoreeCloud visual-identity and icon-system standards.

## Approval and release boundary

The current candidate may be used in Development builds for recognition and review. It becomes the official GoreeCloud Launcher identity only after explicit approval of the canonical source and its required derivatives.

Until that occurs:

- GoreeCloud Launcher remains Development;
- the candidate must be described as a candidate rather than an approved official mark;
- product-identity acceptance remains a release blocker; and
- a later approved revision may refine or replace the candidate without changing the current implementation-status claims.

## Relationship to Launcher Unified Search

Launcher Unified Search may use a search-specific glyph or motion treatment inside the Launcher UI, but that treatment does not replace the application identity. Search imagery remains subordinate to and visually compatible with the Launcher mark.
