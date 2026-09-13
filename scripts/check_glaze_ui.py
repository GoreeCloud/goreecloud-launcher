#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
THEME_ROOT = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme"
METRICS = THEME_ROOT / "GlazeMetrics.kt"
ATMOSPHERE = THEME_ROOT / "GlazeAtmosphere.kt"
THEME = THEME_ROOT / "GlazeTheme.kt"
OPTICAL = THEME_ROOT / "GlazeOpticalV14.kt"
OPTICAL_TEST = ROOT / "app/src/test/java/com/goreecloud/launcher/ui/theme/GlazeOpticalV14Test.kt"
THEME_REPOSITORY = THEME_ROOT / "GlazeThemeRepository.kt"
THEME_MANAGER = THEME_ROOT / "ThemeManagerSurface.kt"
THEME_CATALOG = THEME_ROOT / "GlazeThemeManagerCatalog.kt"
SETTINGS_SURFACE = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/LauncherSettingsSurface.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"
DEVELOPMENT = ROOT / "docs/development/saveable-theme-manager-settings-composition.md"
PLATFORM = ROOT / "goreecloud.platform.yaml"

TARGET_VERSION = "1.4.0"
SOURCE_REVISION = "84cb3db4884042f0fa25ed6d475a127fb110f596"

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
    "opticalMicro": 8,
    "opticalControl": 16,
    "opticalContainer": 24,
    "opticalHero": 32,
    "opticalCapsule": 999,
    "minimumTarget": 48,
    "comfortableTarget": 48,
    "touchAssistanceTarget": 56,
}


def fail(message: str) -> None:
    raise SystemExit(f"GLAZE UI V1.4 Launcher boundary failed: {message}")


def read(path: Path, label: str) -> str:
    if not path.is_file():
        fail(f"missing {label}: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def main() -> None:
    metrics = read(METRICS, "native metric map")
    atmosphere = read(ATMOSPHERE, "native atmospheric map")
    theme = read(THEME, "native theme map")
    optical = read(OPTICAL, "native V1.4 optical resolver")
    optical_test = read(OPTICAL_TEST, "native V1.4 optical unit tests")
    repository = read(THEME_REPOSITORY, "theme persistence repository")
    manager = read(THEME_MANAGER, "Theme Manager surface")
    catalog = read(THEME_CATALOG, "Theme Manager catalog")
    settings = read(SETTINGS_SURFACE, "Launcher Settings surface")
    adoption = read(ADOPTION, "V1.4 adoption record")
    development = read(DEVELOPMENT, "Theme Manager Development evidence")
    platform = read(PLATFORM, "Platform Contract declaration")

    for marker in (
        f'const val targetVersion = "{TARGET_VERSION}"',
        f'const val sourceRevision = "{SOURCE_REVISION}"',
    ):
        if marker not in metrics:
            fail(f"missing exact V1.4 metric provenance `{marker}`")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics:
            fail(f"expected inherited metric mapping `{expected}`")

    if "val space10: Dp = 40.dp" not in metrics or "Launcher-owned 40 dp" not in metrics:
        fail("Launcher 40 dp convenience must remain explicitly non-canonical")

    for marker in (
        "GLAZE UI V1.4",
        "non-semantic",
        "never establish protection, privacy, identity",
        "GlazeOpticalV14",
    ):
        if marker not in atmosphere:
            fail(f"atmospheric V1.4 authority boundary missing `{marker}`")

    for marker in (
        "GLAZE UI V1.4 structural appearance mapping",
        "enum class GlazeThemeMode { SYSTEM, LIGHT, DARK, DEEP_DARK }",
        "background = Color(0xFFF5F7FA)",
        "background = Color(0xFF0B0D11)",
        "background = Color(0xFF05070A)",
        "GlazeThemeMode.DEEP_DARK -> deepDark",
    ):
        if marker not in theme:
            fail(f"missing V1.4 structural appearance evidence `{marker}`")

    for marker in (
        f'const val targetVersion = "{TARGET_VERSION}"',
        f'const val stableSourceRevision = "{SOURCE_REVISION}"',
        "const val maxMemoryTintInfluence = 0.08f",
        "SOLID_ACCESSIBLE",
        "reducedTransparency",
        "forcedColors",
        "increasedContrast",
        "semanticProtection",
        "backgroundComplexity",
        "backgroundLuminance",
        "performs no collection itself" if False else "does not collect telemetry",
    ):
        if marker not in optical:
            fail(f"missing V1.4 Optical Intelligence invariant `{marker}`")

    # Keep the resolver explicitly local and non-authoritative.
    for forbidden in ("HttpClient", "URLConnection", "Socket(", "Camera", "WallpaperManager"):
        if forbidden in optical:
            fail(f"optical resolver gained forbidden collection/network primitive `{forbidden}`")

    for marker in (
        "busy bright backgrounds increase frost",
        "semantic importance increases protection and reduces blur",
        "memory tint is capped at eight percent",
        "reduced transparency fails closed to solid accessible mode",
        "forced colors fail closed to solid accessible mode",
        "increased contrast suppresses decorative warmth and tint",
        "launcher targets exact stable Glaze UI v1_4 release",
    ):
        if marker not in optical_test:
            fail(f"missing V1.4 optical unit coverage `{marker}`")

    manager_combined = repository + "\n" + manager + "\n" + catalog + "\n" + settings
    for marker in (
        "fun setMode(mode: GlazeThemeMode)",
        "GlazeThemeMode.DARK -> GlazeThemeMode.DEEP_DARK",
        "GlazeThemeMode.DEEP_DARK -> GlazeThemeMode.SYSTEM",
        "GlazeThemeManagerCatalog",
        'title = "Deep Dark"',
        "GlazeAtmosphere.softAmber",
        "fun ThemeManagerSurface(",
        "fun LauncherSettingsSurface(",
        "LauncherSettingsDestinationHost",
    ):
        if marker not in manager_combined:
            fail(f"Theme Manager/Settings boundary drift `{marker}`")

    for marker in (
        "# GLAZE UI V1.4 Migration — GoreeCloud Launcher",
        "Status: **Migration in progress / Development**",
        "Official target: **GLAZE UI V1.4 (`1.4.0`)**",
        f"Exact Stable merged source authority: `{SOURCE_REVISION}`",
        "Production eligible on the Glaze UI gate: **no**",
        "does **not** establish complete V1.4 consumer conformance",
        "Content-Aware Frost",
        "Semantic Blur Protection",
        "caps Environmental Color Memory influence at 8%",
        "Forced Colors and Reduced Transparency fail closed",
        "V1.4.1 human-validation boundary",
        "Passing source, unit, build, schema, emulator, or registry checks remains Development evidence only",
    ):
        if marker not in adoption:
            fail(f"missing V1.4 adoption evidence `{marker}`")

    for marker in (
        "Status: Development — GLAZE UI V1.4 migration in progress",
        f"`{SOURCE_REVISION}`",
        "GlazeOpticalV14",
        "SOLID_ACCESSIBLE",
        "Environmental Color Memory influence accepted by the resolver is capped at 8%",
        "V1.4.1 human-validation boundary",
        "representative-device Theme Manager navigation/persistence testing across all four modes",
    ):
        if marker not in development:
            fail(f"Theme Manager Development evidence is not synchronized with V1.4: `{marker}`")

    for marker in (
        'schema_version: "0.2"',
        "  id: goreecloud-launcher",
        '  glaze_ui:\n    result: applicable-migration-required\n    version: "1.4.0"',
        '  glaze_ui_required: "1.4.0"',
        "glaze-ui==1.4.0",
        "GlazeOpticalV14.kt",
        "GlazeOpticalV14Test.kt",
        "conformance:\n  status: nonconformant",
    ):
        if marker not in platform:
            fail(f"Platform Contract is missing the V1.4 Development boundary `{marker}`")

    for active_name, active_content in {
        "metrics": metrics,
        "atmosphere": atmosphere,
        "theme": theme,
        "adoption record": adoption,
        "Theme Manager Development record": development,
        "Platform Contract": platform,
    }.items():
        for stale in (
            "Official target: **GLAZE UI V1.1 (`1.1.0`)**",
            'glaze_ui_required: "1.1.0"',
            "glaze-ui==1.1.0",
            "current Stable GLAZE UI V1.1 / 1.1.0",
        ):
            if stale in active_content:
                fail(f"{active_name} retains superseded current-target marker `{stale}`")

    print(
        "GLAZE UI V1.4 Launcher source mapping passed: "
        f"target {TARGET_VERSION}, source {SOURCE_REVISION}; Platform Contract remains "
        "migration-required/nonconformant until rendered/accessibility/device/release acceptance."
    )


if __name__ == "__main__":
    main()
