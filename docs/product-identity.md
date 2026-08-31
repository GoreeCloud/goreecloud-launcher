# GoreeCloud Launcher Product Identity

## Status

**Official artwork pending — Development blocker for product-identity acceptance.**

This document defines the source-controlled identity requirements for GoreeCloud Launcher. It does not contain, generate, or approve artwork.

No canonical GoreeCloud Launcher product-specific artwork is currently committed to this repository. Framework/default Android imagery, the current Android-style placeholder, an upstream launcher identity, a generic GoreeCloud corporate/platform logo, and automatically generated/unreviewed artwork must not be represented as the official Launcher identity.

## Product-family role

GoreeCloud Launcher belongs to the GoreeCloud **System Utility** family. Its identity should communicate the product's role as the personalized front door to GoreeCloud and as the native access point for applications, activities, navigation, discovery, search, and contextual actions.

The mark should feel first-party and related to the GoreeCloud visual system without simply placing the corporate logo inside an app-icon container.

## Semantic direction

The approved identity should visually express one or more of these concepts without becoming literal or crowded:

- launch / open / access;
- application and activity navigation;
- a personalized home or entry point;
- discovery and movement through GoreeCloud;
- a coherent gateway into applications, information, and actions.

Avoid a generic Android robot, generic home glyph, generic app-grid glyph used without meaningful differentiation, copied platform-launcher imagery, or a mark that is legible only because it contains text.

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

## Canonical source

The approved canonical source artwork must live in `GoreeCloud/goreecloud-launcher`. Shared GoreeCloud branding repositories may contain references or reusable brand primitives, but they do not replace repository-local product authority.

Preferred canonical source should be vector-first where feasible. Any raster master must be sufficiently high resolution to generate all required derivatives without quality loss.

The committed source must record provenance and approval so derivatives can be traced back to one canonical artwork source.

## Android deliverables

Acceptance of the Android Launcher identity requires traceable derivatives for at least:

1. Adaptive icon foreground.
2. Adaptive icon background.
3. Monochrome/themed icon foreground for supported Android versions.
4. Legacy/mipmap launcher icon outputs required by the supported Android build configuration.
5. Round/masked behavior verified through adaptive-icon tooling rather than separate unrelated artwork.
6. Store/listing artwork when a distribution channel requires it.

Generated derivatives must be reproducible from the canonical source and must not become independent design authorities.

## Quality review

Before the mark is called official, review must cover:

- legibility at small sizes;
- visual distinction from other GoreeCloud applications installed on the same device;
- visual distinction from Android, Pixel Launcher, Samsung One UI Home, Nova Launcher, and other recognizable third-party/system launcher identities;
- themed/monochrome rendering;
- adaptive-mask cropping across representative mask shapes;
- Light/Dark wallpaper contrast contexts;
- home-screen, application-drawer, Android default-Home chooser, Settings, and package-installer presentation; and
- consistency with current GoreeCloud visual-identity and icon-system standards.

## Approval and release boundary

Artwork becomes the official GoreeCloud Launcher identity only after a reviewed canonical source is explicitly approved and committed with its required derivatives. Merely adding an image file, generated mockup, temporary placeholder, or corporate logo does not satisfy this requirement.

Until that occurs:

- the product remains Development;
- the current placeholder must be described as temporary;
- screenshots/builds must not imply that the placeholder is the final official icon; and
- product-identity acceptance remains a release blocker.

## Relationship to Launcher Unified Search

Launcher Unified Search may use a search-specific glyph or motion treatment inside the Launcher UI, but that treatment does not replace the application identity. Search imagery should remain subordinate to and visually compatible with the approved Launcher mark.
