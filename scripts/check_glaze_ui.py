#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
METRICS = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt"
ATMOSPHERE = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeAtmosphere.kt"
THEME = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeTheme.kt"
THEME_REPOSITORY = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeThemeRepository.kt"
THEME_MANAGER = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/ThemeManagerSurface.kt"
THEME_CATALOG = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeThemeManagerCatalog.kt"
SETTINGS_SURFACE = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/LauncherSettingsSurface.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"
DEVELOPMENT = ROOT / "docs/development/saveable-theme-manager-settings-composition.md"
PLATFORM = ROOT / "goreecloud.platform.yaml"

TARGET_VERSION = "1.2.0"
SOURCE_REVISION = "f285b9145e27e6e7027b075c37299d101945c272"

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

EXPECTED_FROSTED_NEUTRAL = (
    "frostWhite = Color(0xA8FFFFFF)",
    "frostWhiteDense = Color(0xD9FFFFFF)",
    "frostGraphite = Color(0xA61C1D20)",
    "frostGraphiteDense = Color(0xD91A1C21)",
    "frostDeepDark = Color(0xB012151A)",
    "iceBlueAccent = Color(0xFF78A7FF)",
    "lightNeutralLine = Color(0x1A505050)",
    "darkNeutralLine = Color(0x1AFFFFFF)",
    "decorativeAccentMaxAlpha = 0.12f",
    "defaultAccentFieldsMax = 1",
)

EXPECTED_THEME_MARKERS = (
    "GLAZE UI V1.2 Frosted Neutral",
    "Neutral glass is the material",
    "surface = Color(0x94FFFFFF)",
    "surfaceVariant = Color(0xD9FFFFFF)",
    "surface = Color(0x9E19191B)",
    "surfaceVariant = Color(0xD91A1C21)",
    "surface = Color(0xB012151A)",
    "surfaceVariant = Color(0xD90D1015)",
    "outlineVariant = GlazeAtmosphere.lightNeutralLine",
    "outlineVariant = GlazeAtmosphere.darkNeutralLine",
)


def fail(message: str) -> None:
    raise SystemExit(f"GLAZE UI V1.2 source / Platform Contract v0.2 boundary failed: {message}")


def read(path: Path, label: str) -> str:
    if not path.is_file():
        fail(f"missing {label}: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def main() -> None:
    metrics_text = read(METRICS, "native metric map")
    atmosphere_text = read(ATMOSPHERE, "Frosted Neutral token map")
    theme_text = read(THEME, "native theme map")
    repository_text = read(THEME_REPOSITORY, "theme persistence repository")
    manager_text = read(THEME_MANAGER, "native Theme Manager surface")
    catalog_text = read(THEME_CATALOG, "native Theme Manager catalog")
    settings_text = read(SETTINGS_SURFACE, "Launcher Settings composition")
    adoption_text = read(ADOPTION, "V1.2 adoption evidence")
    development_text = read(DEVELOPMENT, "Theme Manager development evidence")
    platform_text = read(PLATFORM, "Platform Contract declaration")

    for marker in (
        f'const val targetVersion = "{TARGET_VERSION}"',
        f'const val sourceRevision = "{SOURCE_REVISION}"',
        "Frosted Neutral",
    ):
        if marker not in metrics_text:
            fail(f"missing exact V1.2 provenance marker `{marker}`")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics_text:
            fail(f"expected inherited metric mapping `{expected}`")

    if "val space10: Dp = 40.dp" not in metrics_text or "Launcher-owned 40 dp" not in metrics_text:
        fail("Launcher 40 dp layout convenience must remain explicitly non-canonical")

    for marker in EXPECTED_FROSTED_NEUTRAL:
        if marker not in atmosphere_text:
            fail(f"missing V1.2 Frosted Neutral evidence `{marker}`")

    for retired in (
        "deepTeal",
        "mineralTeal",
        "softAqua",
        "softAmber",
        "champagneGold",
        "AuraMaxAlpha",
        "defaultAuraFieldsMax",
    ):
        if retired in atmosphere_text:
            fail(f"retired chromatic material/aura marker remains active: `{retired}`")

    for authority_marker in (
        "Neutral glass is the material; color is an accent",
        "never establish protection, privacy, identity",
        "Accessibility and producer-owned semantics always take precedence",
    ):
        if authority_marker not in atmosphere_text:
            fail(f"Frosted Neutral authority boundary missing `{authority_marker}`")

    for marker in EXPECTED_THEME_MARKERS:
        if marker not in theme_text:
            fail(f"missing V1.2 appearance evidence `{marker}`")

    combined_manager = repository_text + "\n" + manager_text + "\n" + catalog_text + "\n" + settings_text
    for marker in (
        "fun setMode(mode: GlazeThemeMode)",
        "GlazeThemeMode.DARK -> GlazeThemeMode.DEEP_DARK",
        "GlazeThemeMode.DEEP_DARK -> GlazeThemeMode.SYSTEM",
        "GlazeThemeManagerCatalog",
        "mode = GlazeThemeMode.DEEP_DARK",
        'title = "Deep Dark"',
        "GlazeAtmosphere.iceBlueAccent",
        "Frosted Neutral foundation",
        "fun ThemeManagerSurface(",
        "fun LauncherSettingsSurface(",
        "LauncherSettingsDestinationHost",
    ):
        if marker not in combined_manager:
            fail(f"missing bounded Theme Manager/Settings V1.2 evidence `{marker}`")

    for retired_manager_marker in ("GlazeAtmosphere.softAmber", "restrained V1.1 atmosphere"):
        if retired_manager_marker in manager_text:
            fail(f"Theme Manager retains retired V1.1 visual marker `{retired_manager_marker}`")

    for evidence in (
        "# GLAZE UI V1.2 Migration — GoreeCloud Launcher",
        "Official target: **GLAZE UI V1.2 (`1.2.0`)**",
        f"Exact Stable release source authority: `{SOURCE_REVISION}`",
        "Neutral glass is the material; color is an accent",
        "Production eligible on the Glaze UI gate: **no**",
        "Representative physical-device Theme Manager navigation/persistence",
    ):
        if evidence not in adoption_text:
            fail(f"missing V1.2 adoption evidence `{evidence}`")

    for marker in (
        "Status: Development — GLAZE UI V1.2 migration in progress",
        f"`{SOURCE_REVISION}`",
        "System/Light/Dark/Deep Dark",
        "Frosted Neutral",
        "48 dp normal interaction floor",
        "56 dp Touch Assistance / far-view target",
        "Theme Manager is **Application** settings content",
    ):
        if marker not in development_text:
            fail(f"Theme Manager Development evidence is not synchronized with V1.2: `{marker}`")

    platform_markers = (
        'schema_version: "0.2"',
        "  id: goreecloud-launcher",
        '  glaze_ui:\n    result: applicable-migration-required\n    version: "1.2.0"',
        "current Stable GLAZE UI V1.2 / 1.2.0",
        '  platform_contract: "0.2"',
        '  glaze_ui_required: "1.2.0"',
        "goreecloud-platform-contract==0.2",
        "glaze-ui==1.2.0",
        "conformance:\n  status: nonconformant",
    )
    for marker in platform_markers:
        if marker not in platform_text:
            fail(f"Platform Contract v0.2 is missing V1.2 Development boundary `{marker}`")

    for stale_platform_marker in (
        '  glaze_ui:\n    result: applicable-migration-required\n    version: "1.1.0"',
        '  glaze_ui_required: "1.1.0"',
        "glaze-ui==1.1.0",
        "stable_eligible: true",
    ):
        if stale_platform_marker in platform_text:
            fail(f"Platform Contract retains superseded declaration `{stale_platform_marker}`")

    print(
        "GLAZE UI V1.2 Launcher source mapping passed: "
        f"target {TARGET_VERSION}, source {SOURCE_REVISION}; Platform Contract v0.2 remains "
        "migration-required/nonconformant until rendered/accessibility/device/release acceptance."
    )


if __name__ == "__main__":
    main()
