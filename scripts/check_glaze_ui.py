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
PLATFORM = ROOT / "goreecloud.platform.yaml"

TARGET_VERSION = "1.0.0"
SOURCE_REVISION = "70909bbdccad378fb7281ae1842e2f5beed64c38"
CURRENT_REQUIRED_VERSION = "1.1.0"

EXPECTED_METRICS = {
    "space1": 4,
    "space2": 8,
    "space3": 12,
    "space4": 16,
    "space5": 20,
    "space6": 24,
    "space8": 32,
    "space12": 48,
    "space16": 64,
    "radiusSmall": 12,
    "radiusMedium": 20,
    "radiusControl": 12,
    "radiusLarge": 20,
    "radiusExtraLarge": 28,
    "radius2ExtraLarge": 28,
    "radiusPill": 999,
    "minimumTarget": 48,
    "comfortableTarget": 48,
    "touchAssistanceTarget": 56,
}

EXPECTED_THEME_MARKERS = [
    "primary = Color(0xFF3478F6)",
    "background = Color(0xFFF5F7FA)",
    "onBackground = Color(0xFF151A23)",
    "surface = Color(0xFFFFFFFF)",
    "onSurfaceVariant = Color(0xFF5D6675)",
    "primary = Color(0xFF8DB5FF)",
    "background = Color(0xFF0B0D11)",
    "onBackground = Color(0xFFF5F7FA)",
    "surface = Color(0xFF12151B)",
    "surfaceVariant = Color(0xDB181D26)",
    "onSurfaceVariant = Color(0xFFB0B7C3)",
    "Deep Dark remains an",
]


def fail(message: str) -> None:
    raise SystemExit(f"GLAZE UI V1.0 source / Platform Contract v0.2 boundary failed: {message}")


def read(path: Path, label: str) -> str:
    if not path.is_file():
        fail(f"missing {label}: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def main() -> None:
    metrics_text = read(METRICS, "native metric map")
    theme_text = read(THEME, "native theme map")
    repository_text = read(THEME_REPOSITORY, "theme persistence repository")
    manager_text = read(THEME_MANAGER, "native Theme Manager surface")
    catalog_text = read(THEME_CATALOG, "native Theme Manager catalog")
    settings_text = read(SETTINGS_SURFACE, "Launcher Settings composition")
    adoption_text = read(ADOPTION, "V1 adoption evidence")
    development_text = read(DEVELOPMENT, "Theme Manager development evidence")
    platform_text = read(PLATFORM, "Platform Contract declaration")

    for marker in (
        f'const val targetVersion = "{TARGET_VERSION}"',
        f'const val sourceRevision = "{SOURCE_REVISION}"',
    ):
        if marker not in metrics_text:
            fail(f"missing exact V1 provenance marker `{marker}`")

    for retired_anchor in (
        "stableVersion",
        "stablePromotionHead",
        "stableReleaseRevision",
        "fb5ecde4a8258503789ffde08ac46a2e524ef71e",
        "6731098b28dd0393faa878c70d989a221d714a20",
    ):
        if retired_anchor in metrics_text:
            fail(f"pre-reset active provenance remains in native metrics: `{retired_anchor}`")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics_text:
            fail(f"expected V1 metric mapping `{expected}`")

    if "val space10: Dp = 40.dp" not in metrics_text or "Launcher-owned 40 dp" not in metrics_text:
        fail("Launcher 40 dp layout convenience must remain explicitly non-canonical")

    for marker in EXPECTED_THEME_MARKERS:
        if marker not in theme_text:
            fail(f"missing V1 Light/Dark theme evidence `{marker}`")

    manager_combined = repository_text + "\n" + manager_text + "\n" + catalog_text + "\n" + settings_text
    for marker in (
        "fun setMode(mode: GlazeThemeMode)",
        "GlazeThemeManagerCatalog",
        "fun ThemeManagerSurface(",
        "fun LauncherSettingsSurface(",
        "LauncherSettingsDestinationHost",
        "Icon packs, masking, Deep Dark",
    ):
        if marker not in manager_combined:
            fail(f"missing bounded Theme Manager/Settings composition evidence `{marker}`")

    for evidence in (
        "# GLAZE UI V1.0 Migration — GoreeCloud Launcher",
        "Status: **Migration in progress / Development**",
        "Official target: **GLAZE UI V1.0 (`1.0.0`)**",
        f"Exact V1 source authority: `{SOURCE_REVISION}`",
        "Production eligible on the Glaze UI gate: **no**",
        "does **not** establish production acceptance",
        "No pre-reset Glaze UI acceptance is inherited as V1 evidence",
        "Launcher Home is a **Workspace** presentation surface",
        "Launcher Settings and Theme Manager are **Application** surfaces",
        "Deep Dark is a required V1 appearance mode",
        "Historical evidence boundary",
        "Representative physical-device Theme Manager navigation/persistence",
        "do not select a retired Glaze product version",
    ):
        if evidence not in adoption_text:
            fail(f"missing V1 adoption evidence `{evidence}`")

    for marker in (
        "Status: Development — GLAZE UI V1.0 migration in progress",
        f"`{SOURCE_REVISION}`",
        "48 dp normal interaction floor",
        "56 dp Touch Assistance / far-view target",
        "Theme Manager is **Application** settings content",
        "Pre-reset evidence",
        "representative-device Theme Manager navigation/persistence testing",
    ):
        if marker not in development_text:
            fail(f"Theme Manager Development evidence is not synchronized with V1: `{marker}`")

    platform_markers = (
        'schema_version: "0.2"',
        "  id: goreecloud-launcher",
        '  glaze_ui:\n    result: applicable-migration-required\n    version: "1.0.0"',
        "current Stable GLAZE UI V1.1 / 1.1.0",
        '  platform_contract: "0.2"',
        f'  glaze_ui_required: "{CURRENT_REQUIRED_VERSION}"',
        "goreecloud-platform-contract==0.2",
        "glaze-ui==1.1.0",
        "conformance:\n  status: nonconformant",
    )
    for marker in platform_markers:
        if marker not in platform_text:
            fail(f"Platform Contract v0.2 is missing the V1 source / V1.1 required boundary `{marker}`")

    for stale_platform_marker in (
        'schema_version: "0.1"',
        'schema_version: "1.0"',
        '  glaze_ui:\n    status: partial',
        "goreecloud-platform-contract>=0.1",
        "glaze-ui==1.0.0",
        'required_version: "1.0.0"',
        'implemented_version: "1.0.0"',
        'required_version: "2.2.0"',
        'implemented_version: "2.2.0"',
        "stable_eligible: true",
    ):
        if stale_platform_marker in platform_text:
            fail(f"Platform Contract retains superseded declaration `{stale_platform_marker}`")

    current_records = {
        "native metrics": metrics_text,
        "native theme": theme_text,
        "Theme Manager": manager_text,
        "V1 adoption record": adoption_text,
        "Theme Manager Development record": development_text,
        "Platform Contract": platform_text,
    }
    for name, content in current_records.items():
        for retired in ("Glaze UI 2.2.0 Stable", "Glaze UI 2.2 Adoption Candidate", "Required Stable baseline: **Glaze UI 2.2.0**"):
            if retired in content:
                fail(f"{name} retains a retired active Glaze target: `{retired}`")

    if "val minimumTarget: Dp = 44.dp" in metrics_text:
        fail("superseded 44 dp general target floor remains in active V1 mapping")

    print(
        "GLAZE UI V1.0 Launcher source mapping passed: "
        f"source target {TARGET_VERSION}, source {SOURCE_REVISION}; Platform Contract v0.2 requires {CURRENT_REQUIRED_VERSION} "
        "and remains migration-required/nonconformant; rendered/accessibility/device/release acceptance remains separate."
    )


if __name__ == "__main__":
    main()
