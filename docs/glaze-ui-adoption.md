# GLAZE UI V1.4 Migration — GoreeCloud Launcher

Status: **Migration in progress / Development**  
Official target: **GLAZE UI V1.4 (`1.4.0`)**  
Canonical repository: `GoreeCloud/goreecloud-glaze-ui`  
Exact Stable merged source authority: `84cb3db4884042f0fa25ed6d475a127fb110f596`  
Production eligible on the Glaze UI gate: **no**  
Adoption mode: native Android semantic/structural mapping with bounded local Optical Intelligence  
Automated contract: `scripts/check_glaze_ui.py`

GLAZE UI V1.4 is the current GoreeCloud design-system adoption target for Launcher. This record defines repository-local V1.4 Development implementation. It does **not** establish complete V1.4 consumer conformance, rendered/accessibility/device acceptance, product production approval, release approval, or Stable Launcher qualification. No V1.3, V1.2, V1.1, V1.0, or pre-reset acceptance is inherited as V1.4 application acceptance.

## Authority boundary

Launcher consumes the current Stable V1.4 contract as a native Jetpack Compose mapping. It does not embed a remote UI runtime and does not create a competing Glaze authority.

The native source pins V1.4 through `GlazeMetrics.targetVersion`, `GlazeOpticalV14.targetVersion`, and the exact merged Stable Glaze source revision. `GlazeTheme` retains the established Light, Dark, and Deep Dark structural appearances while `GlazeOpticalV14` adds the bounded V1.4 optical resolver.

## Implemented V1.4 source mapping

The current Development implementation:

- pins machine version `1.4.0` and exact Stable merged source revision `84cb3db4884042f0fa25ed6d475a127fb110f596`;
- preserves the existing 4/8/12/16/20/24/32/48/64 dp spacing mapping and established structural radii used by Launcher;
- preserves the 48 dp normal touch-oriented interaction floor and 56 dp Touch Assistance / far-view target;
- preserves System, Light, Dark, and Deep Dark through the existing device-local Theme Manager persistence path;
- keeps Deep Teal + Soft Amber atmosphere non-semantic;
- adds a deterministic native Content-Aware Frost resolver using already-derived background complexity/luminance inputs;
- adds Semantic Blur Protection where higher semantic importance reduces allowed blur;
- adds bounded chromatic depth and daypart warmth;
- caps Environmental Color Memory influence at 8%;
- forces Reduced Transparency and Forced Colors into a solid-accessible mode;
- suppresses decorative tint and warmth under Increased Contrast; and
- requires no telemetry, camera access, wallpaper-pixel collection, analytics, or remote context.

`space10` remains a Launcher-owned 40 dp layout convenience and is not claimed as a canonical Glaze token.

## Context and privacy boundary

`GlazeOpticalV14` is signal-source agnostic. It accepts already-derived local inputs but performs no collection itself. Any future adapter that derives wallpaper, environment, time, or other context remains separately subject to Launcher privacy/security authority, Privacy Shield, Wardveil Security, and Android permissions/capability boundaries.

Glaze UI does not grant authority to inspect content or collect user data for visual effects.

## Accessibility precedence

V1.4 optical adaptation remains subordinate to accessibility and meaning:

1. Forced Colors or platform equivalent.
2. Reduced Transparency.
3. Increased Contrast / show-boundaries behavior.
4. Semantic state and task clarity.
5. Material/optical expression.

In the current resolver, Forced Colors and Reduced Transparency fail closed to `SOLID_ACCESSIBLE`; Increased Contrast suppresses decorative warmth and memory tint while increasing optical protection.

## System Shell classification

Launcher Home is a **Workspace** presentation surface. Launcher Settings and Theme Manager are **Application** surfaces. These classifications affect presentation and interaction only; they grant no operating-system, window-manager, Control Center, notification, authentication, search-indexing, or other system authority.

GoreeCloud Index remains the universal-search/indexing authority.

## Presentation rule

Launcher preserves the Glaze rule: **Solid where users read or make explicit critical decisions. Glazed where users interact with transient navigation, command, search, control, or feedback chrome.**

Durable settings, explanatory content, and destructive or security-sensitive decisions remain certainty-first. Optical atmosphere must never manufacture authorization, privacy, security, backup, recovery, identity, or trust state owned by another GoreeCloud system or Android.

## V1.4.1 human-validation boundary

By the V1.4 release decision, human validation and human verification are assigned to Glaze UI V1.4.1 rather than blocking the shared V1.4.0 lifecycle. Launcher therefore does not represent the following as passed V1.4.0 evidence:

- subjective optical-quality and GoreeCloud-identity review;
- manual assistive-technology verification;
- representative physical-device optical qualification;
- real-device performance, thermal, and power review; or
- qualitative animation/touch/visual polish review.

Launcher-specific release acceptance may remain stricter than the shared Glaze lifecycle and is not waived by this deferral.

## Acceptance still required

- Rendered V1.4 review across Home, Apps, Settings, Theme Manager, dialogs, and workspace editing.
- Wiring and validation of approved local optical context sources where useful.
- Reduced Motion, Reduced Transparency, Increased Contrast, native forced-color equivalents, and degradation-order validation.
- 200% text/reflow, RTL/localization, and Touch Assistance resolution.
- TalkBack, Switch Access, keyboard/D-pad/focus-order, and spoken-announcement acceptance.
- Representative phone/tablet/foldable adaptive composition.
- Representative-device Theme Manager navigation/persistence and Home/Apps/Settings acceptance.
- Performance/power fallback evidence and the V1.4.1 human-validation work applicable to Launcher.
- Required Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, Manager, and Index integration acceptance.
- Exact-head CI, production signing/distribution, release approval, and Stable Launcher qualification.

Passing source, unit, build, schema, emulator, or registry checks remains Development evidence only.

## Rollback

V1.3 remains the immediate shared Glaze rollback baseline. If the Launcher V1.4 migration regresses, revert the Launcher migration commit(s) while preserving the canonical Glaze V1.4 Stable authority. A local rollback does not authorize relabeling an older Glaze release as current.
