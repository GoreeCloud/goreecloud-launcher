# Saveable Theme Manager settings composition

Status: Development — Glaze UI 2.2 Adoption Candidate

`LauncherSettingsSurface` composes the validated saveable Settings destination model with `LauncherSettingsDestinationHost` while the repository-local native design authority now targets **Glaze UI 2.2.0 Stable**. The reviewed Stable promotion head is `fb5ecde4a8258503789ffde08ac46a2e524ef71e`, release merge `6731098b28dd0393faa878c70d989a221d714a20`, tag `v2.2.0`.

The surface owns only the Settings sub-destination string saved through Compose. Unknown or stale values are decoded through `LauncherSettingsNavigation` and therefore fail closed to Settings root. Root content receives a single bounded callback for opening Theme Manager, and Theme Manager returns through the same navigation model.

Theme persistence remains with the caller-provided `GlazeThemeRepository` path through `onSelectThemeMode`. This surface gains no Home/Apps navigation, workspace placement, launcher-role, wallpaper, icon-pack, account, or system-setting authority.

## Glaze UI 2.2 mapping

`GlazeMetrics` records the 2.2 Stable release identity, compatible spacing/radius vocabulary, the 48 dp normal interaction floor, the 56 dp Touch Assistance floor, and the ordinary System Glaze budget of one dominant panel plus at most three small floating controls with no nested backdrop blur. Unit coverage locks those values so a superseded or weakened geometry contract cannot silently replace them.

Theme Manager is **Application** settings content in the 2.2 System Shell hierarchy. It is not Control Center, Universal Search, or a Critical System surface. Its durable explanatory/settings content therefore stays on solid Material/Canvas/Surface equivalents. No Signature or Intelligence component is introduced merely because 2.2 defines those tiers.

The current System/Light/Dark palettes remain compatible with the canonical 2.2 Light/Dark foundation values. Deep Dark remains deliberately unimplemented rather than being approximated from an unreviewed palette.

## Root composition

`LauncherBetaRoot` routes `LauncherSurfaceMode.SETTINGS` through `LauncherSettingsSurface`, supplies the current theme mode and caller-owned theme callback, and returns to the Home surface through the existing root navigation state.

## Theme Manager interaction targets

The rendered Theme Manager applies `GlazeMetrics.touchAssistanceTarget` (56 dp) as the minimum height for its Done action and actionable System/Light/Dark appearance choices. This is a conservative accessible target for this Settings surface and exceeds the 48 dp normal 2.2 floor. It does **not** claim that Launcher has implemented or detected a platform Touch Assistance preference; broader accessibility-resolution wiring remains separate work.

The already-selected appearance renders as a non-actionable `Selected` status surface rather than another persistence button. This prevents a redundant `onSelectThemeMode` callback for the current mode while preserving clear selected-state presentation. Only a different appearance choice can invoke the caller-owned persistence path.

## Accessibility semantics hardening

Each visual theme preview is now exposed as one descriptive semantics node using stable catalog metadata such as `Dark appearance preview`. Decorative preview internals—including the sample GoreeCloud glyph, sample label, and color chip—are cleared from the accessibility tree for that preview so assistive technology receives one concise description instead of reading decorative fragments as independent content.

The non-actionable selected-state surface now exposes an explicit state description such as `Dark appearance selected` and uses a polite live region. A change to the selected appearance can therefore be announced as state rather than being misrepresented as another clickable control. The semantics metadata is derived from the same `GlazeThemeChoice` catalog as the visible title, and unit coverage locks the user-facing/accessibility strings for the current modes.

This is source-level semantic hardening only. It does not prove TalkBack, Switch Access, keyboard/D-pad, focus-order, spoken-announcement timing, or representative-device accessibility acceptance; those remain runtime gates.

These interaction and semantics changes do not introduce additional theme modes, icon-pack discovery, icon masking, wallpaper-derived palettes, Deep Dark, expression controls, Universal Search, Control Center behavior, or new system-setting authority.

## Remaining acceptance

This Adoption Candidate still requires complete repository/application 2.2 component mapping, state-priority review, Reduced Motion, Reduced Transparency, Increased Contrast, native accessibility-equivalent/forced-color handling where applicable, 200% text/reflow, RTL/localization expansion, platform Touch Assistance resolution, complete System Glaze budget review across Launcher, representative phone/tablet/foldable behavior, TalkBack/Switch Access, performance fallbacks, Human Visual Excellence, representative-device Theme Manager navigation/persistence testing, production signing/distribution, release, and Stable qualification.

This remains Development evidence only. A green build or correct token/semantics mapping does not establish full Glaze UI 2.2 conformance or production readiness.
