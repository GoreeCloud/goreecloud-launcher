#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
METRICS = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt"
THEME = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeTheme.kt"
THEME_REPOSITORY = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeThemeRepository.kt"
THEME_MANAGER = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/ThemeManagerSurface.kt"
THEME_CATALOG = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeThemeManagerCatalog.kt"
SETTINGS_SURFACE = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/LauncherSettingsSurface.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"
DEVELOPMENT = ROOT / "docs/development/saveable-theme-manager-settings-composition.md"

TARGET_VERSION = "2.2.0"
STABLE_PROMOTION_HEAD = "fb5ecde4a8258503789ffde08ac46a2e524ef71e"
STABLE_RELEASE_REVISION = "6731098b28dd0393faa878c70d989a221d714a20"

EXPECTED_METRICS = {
    "space1": 4,
    "space2": 8,
    "space3": 12,
    "space4": 16,
    "space5": 20,
    "space6": 24,
    "space8": 32,
    "space10": 40,
    "space12": 48,
    "space16": 64,
    "radiusSmall": 10,
    "radiusMedium": 14,
    "radiusControl": 16,
    "radiusLarge": 22,
    "radiusExtraLarge": 28,
    "radius2ExtraLarge": 32,
    "radiusPill": 999,
    "minimumTarget": 48,
    "comfortableTarget": 48,
    "touchAssistanceTarget": 56,
}

EXPECTED_THEME_MARKERS = [
    "primary = Color(0xFF366CF6)",
    "background = Color(0xFFEEF3F9)",
    "onBackground = Color(0xFF172033)",
    "surface = Color(0xC2FFFFFF)",
    "onSurfaceVariant = Color(0xFF67748A)",
    "primary = Color(0xFF7AA2FF)",
    "background = Color(0xFF0D1119)",
    "onBackground = Color(0xFFF3F6FB)",
    "surface = Color(0xC719202D)",
    "onSurfaceVariant = Color(0xFFA1AEC0)",
    "Deep Dark remains an explicit application acceptance gap",
]


def fail(message: str) -> None:
    raise SystemExit(f"Glaze UI contract failed: {message}")


def main() -> None:
    required_files = (
        (METRICS, "native metric map"),
        (THEME, "native theme map"),
        (THEME_REPOSITORY, "theme persistence repository"),
        (THEME_MANAGER, "native Theme Manager surface"),
        (THEME_CATALOG, "native Theme Manager catalog"),
        (SETTINGS_SURFACE, "Launcher Settings composition"),
        (ADOPTION, "adoption evidence"),
        (DEVELOPMENT, "Theme Manager development evidence"),
    )
    for path, label in required_files:
        if not path.is_file():
            fail(f"missing {label}: {path.relative_to(ROOT)}")

    metrics_text = METRICS.read_text(encoding="utf-8")
    theme_text = THEME.read_text(encoding="utf-8")
    repository_text = THEME_REPOSITORY.read_text(encoding="utf-8")
    manager_text = THEME_MANAGER.read_text(encoding="utf-8")
    catalog_text = THEME_CATALOG.read_text(encoding="utf-8")
    settings_text = SETTINGS_SURFACE.read_text(encoding="utf-8")
    adoption_text = ADOPTION.read_text(encoding="utf-8")
    development_text = DEVELOPMENT.read_text(encoding="utf-8")

    stable_markers = [
        f'const val stableVersion = "{TARGET_VERSION}"',
        f'const val stablePromotionHead = "{STABLE_PROMOTION_HEAD}"',
        f'const val stableReleaseRevision = "{STABLE_RELEASE_REVISION}"',
        "const val systemGlazeDominantPanelMax = 1",
        "const val systemGlazeSmallFloatingControlsMax = 3",
        "const val nestedBackdropBlurAllowed = false",
    ]
    for marker in stable_markers:
        if marker not in metrics_text:
            fail(f"missing Glaze UI 2.2 Stable metric/governance marker `{marker}`")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics_text:
            fail(f"expected Glaze UI 2.2 metric mapping `{expected}`")

    for invented_alias in ("val space7:", "val space9:"):
        if invented_alias in metrics_text:
            fail(f"non-canonical sequential spacing alias remains active: `{invented_alias}`")

    for marker in EXPECTED_THEME_MARKERS:
        if marker not in theme_text:
            fail(f"missing Glaze UI 2.2 Light/Dark theme evidence `{marker}`")

    manager_markers = [
        "fun setMode(mode: GlazeThemeMode)",
        "GlazeThemeManagerCatalog",
        "fun ThemeManagerSurface(",
        "fun LauncherSettingsSurface(",
        "LauncherSettingsDestinationHost",
        "Icon packs, masking, Deep Dark",
    ]
    manager_combined = repository_text + "\n" + manager_text + "\n" + catalog_text + "\n" + settings_text
    for marker in manager_markers:
        if marker not in manager_combined:
            fail(f"missing bounded Theme Manager/Settings composition evidence `{marker}`")

    required_evidence = [
        "# Glaze UI 2.2 Adoption Candidate — GoreeCloud Launcher",
        "Status: **Adoption Candidate**",
        "Required Stable baseline: **Glaze UI 2.2.0**",
        f"Reviewed exact Stable promotion head: `{STABLE_PROMOTION_HEAD}`",
        f"Reviewed Stable release merge: `{STABLE_RELEASE_REVISION}`",
        "Reviewed Stable tag: `v2.2.0`",
        "Production eligible on the Glaze UI gate: **no**",
        "Automated contract: `scripts/check_glaze_ui.py`",
        "Glaze UI 2.2.0 Stable is the production design-system authority.",
        "Implemented 2.2 mapping",
        "canonical spacing keys",
        "48 dp `currentContract.touchMinimum` interaction floor",
        "56 dp `currentContract.touchAssistanceMinimum` target",
        "compatibility token `target.minimum=44`",
        "System Glaze budget",
        "Launcher Home is treated as a **Workspace** presentation surface",
        "Launcher Settings and Theme Manager are **Application** surfaces",
        "Deep Dark is not approximated with an invented palette",
        "Motion quarantine",
        "Historical migration evidence",
        "TalkBack, Switch Access",
        "Representative physical-device Theme Manager navigation/persistence",
    ]
    for evidence in required_evidence:
        if evidence not in adoption_text:
            fail(f"missing adoption evidence `{evidence}`")

    for marker in (
        "Status: Development — Glaze UI 2.2 Adoption Candidate",
        "Glaze UI 2.2.0 Stable",
        f"{STABLE_PROMOTION_HEAD}",
        f"{STABLE_RELEASE_REVISION}",
        "Theme Manager is **Application** settings content",
        "representative-device Theme Manager navigation/persistence testing",
    ):
        if marker not in development_text:
            fail(f"Theme Manager Development evidence is not synchronized with current 2.2 mapping: `{marker}`")

    stale_active_markers = [
        "Required Stable baseline: **Glaze UI 2.1.0**",
        "Target: Glaze UI 2.1 Stable",
        "Canonical token version reviewed: 2.1.0",
        "Glaze UI 2.1 Stable is the production design-system authority.",
        "Required Stable baseline: **Glaze UI 2.0.0**",
        "Target: Glaze UI 2.0 Stable",
        "Canonical token version reviewed: 2.0.0",
        "Target: Glaze UI 1.6 Stable",
        "Canonical token version reviewed: 1.6.0",
        "Target: Glaze UI 1.5 Stable",
        "Canonical token version reviewed: 1.5.0",
        "Target: Glaze UI 1.4 Stable",
        "Canonical token version reviewed: 1.4.0",
    ]
    combined = adoption_text + "\n" + metrics_text + "\n" + theme_text + "\n" + development_text
    for marker in stale_active_markers:
        if marker in combined:
            fail(f"stale historical target remains active: `{marker}`")

    if "val minimumTarget: Dp = 44.dp" in combined:
        fail("superseded 44 dp general target floor remains in active Glaze mapping")

    print(
        "Glaze UI 2.2 Launcher Adoption Candidate contract passed: "
        f"{len(EXPECTED_METRICS)} native metrics + exact Stable anchors + System Glaze budget + "
        "Light/Dark mapping + reachable Theme Manager Settings composition + explicit production block validated."
    )


if __name__ == "__main__":
    main()
