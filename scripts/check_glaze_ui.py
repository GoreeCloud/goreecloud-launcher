#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
THEME_ROOT = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme"
METRICS = THEME_ROOT / "GlazeMetrics.kt"
ATMOSPHERE = THEME_ROOT / "GlazeAtmosphere.kt"
THEME = THEME_ROOT / "GlazeTheme.kt"
OPTICAL = THEME_ROOT / "GlazeOpticalV14.kt"
CAPABILITY = THEME_ROOT / "GlazeCapabilityV15.kt"
OPTICAL_TEST = ROOT / "app/src/test/java/com/goreecloud/launcher/ui/theme/GlazeOpticalV14Test.kt"
METRICS_TEST = ROOT / "app/src/test/java/com/goreecloud/launcher/ui/theme/GlazeMetricsContractTest.kt"
CAPABILITY_TEST = ROOT / "app/src/test/java/com/goreecloud/launcher/ui/theme/GlazeCapabilityV15Test.kt"
THEME_REPOSITORY = THEME_ROOT / "GlazeThemeRepository.kt"
THEME_MANAGER = THEME_ROOT / "ThemeManagerSurface.kt"
THEME_CATALOG = THEME_ROOT / "GlazeThemeManagerCatalog.kt"
SETTINGS_SURFACE = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/LauncherSettingsSurface.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"
DEVELOPMENT = ROOT / "docs/development/saveable-theme-manager-settings-composition.md"
PLATFORM = ROOT / "goreecloud.platform.yaml"

TARGET_VERSION = "1.5.0"
STABLE_REVISION = "b7fa8164bfdeaa1dc0acb21b770e7601120da04e"
IMPLEMENTATION_ANCHOR = "ee1032a0822ab8e103f8afe48e5c1859fde65cc9"
OPTICAL_BASELINE_VERSION = "1.4.1"
OPTICAL_BASELINE_REVISION = "4fab9da0fad2e5c974e0e66ec88632c61745751c"
OPTICAL_ROLLBACK_VERSION = "1.4.0"
ROLLBACK_VERSION = "1.4.1"

EXPECTED_METRICS = {
    "space1": 4, "space2": 8, "space3": 12, "space4": 16, "space5": 20,
    "space6": 24, "space8": 32, "space12": 48, "space16": 64,
    "radiusSmall": 12, "radiusMedium": 20, "radiusControl": 12,
    "radiusLarge": 20, "radiusExtraLarge": 28, "radius2ExtraLarge": 28,
    "radiusPill": 999, "opticalMicro": 8, "opticalControl": 16,
    "opticalContainer": 24, "opticalHero": 32, "opticalCapsule": 999,
    "minimumTarget": 48, "comfortableTarget": 48, "touchAssistanceTarget": 56,
}


def fail(message: str) -> None:
    raise SystemExit(f"GLAZE UI V1.5 Launcher boundary failed: {message}")


def read(path: Path, label: str) -> str:
    if not path.is_file():
        fail(f"missing {label}: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(text: str, marker: str, label: str) -> None:
    if marker not in text:
        fail(f"{label} missing `{marker}`")


def main() -> None:
    metrics = read(METRICS, "native metric map")
    atmosphere = read(ATMOSPHERE, "native atmospheric map")
    theme = read(THEME, "native theme map")
    optical = read(OPTICAL, "native optical resolver")
    capability = read(CAPABILITY, "native V1.5 capability resolver")
    optical_test = read(OPTICAL_TEST, "native optical tests")
    metrics_test = read(METRICS_TEST, "native metric tests")
    capability_test = read(CAPABILITY_TEST, "native V1.5 capability tests")
    repository = read(THEME_REPOSITORY, "theme persistence repository")
    manager = read(THEME_MANAGER, "Theme Manager surface")
    catalog = read(THEME_CATALOG, "Theme Manager catalog")
    settings = read(SETTINGS_SURFACE, "Launcher Settings surface")
    adoption = read(ADOPTION, "adoption record")
    development = read(DEVELOPMENT, "Theme Manager Development evidence")
    platform = read(PLATFORM, "Platform Contract declaration")

    # V1.5 inherits rather than relabels the reviewed V1.4.1 optical implementation.
    for marker in (
        f'const val targetVersion = "{OPTICAL_BASELINE_VERSION}"',
        f'const val sourceRevision = "{OPTICAL_BASELINE_REVISION}"',
        f'const val rollbackVersion = "{OPTICAL_ROLLBACK_VERSION}"',
    ):
        require(metrics, marker, "metrics")

    for name, value in EXPECTED_METRICS.items():
        require(metrics, f"val {name}: Dp = {value}.dp", "metrics")
    require(metrics, "Launcher-owned 40 dp", "metrics")

    for marker in (
        "GLAZE UI V1.4", "non-semantic", "never establish protection, privacy, identity", "GlazeOpticalV14",
    ):
        require(atmosphere, marker, "atmosphere")
    for marker in (
        "enum class GlazeThemeMode { SYSTEM, LIGHT, DARK, DEEP_DARK }",
        "GlazeThemeMode.DEEP_DARK -> deepDark",
    ):
        require(theme, marker, "theme")

    for marker in (
        f'const val targetVersion = "{OPTICAL_BASELINE_VERSION}"',
        f'const val stableSourceRevision = "{OPTICAL_BASELINE_REVISION}"',
        f'const val rollbackVersion = "{OPTICAL_ROLLBACK_VERSION}"',
        "const val maxMemoryTintInfluence = 0.08f",
        "SOLID_ACCESSIBLE", "reducedTransparency", "forcedColors", "increasedContrast",
        "semanticProtection", "backgroundComplexity", "backgroundLuminance",
        "does not collect telemetry",
    ):
        require(optical, marker, "optical resolver")
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
        "launcher targets exact stable Glaze UI v1_4_1 release",
    ):
        require(optical_test, marker, "optical tests")
    require(metrics_test, "launcher targets exact GLAZE UI V1_4_1 Stable release", "metric tests")

    # The active V1.5 layer is presentation-only and must fail closed on authority ambiguity.
    for marker in (
        f'const val targetVersion = "{TARGET_VERSION}"',
        f'const val stableSourceRevision = "{STABLE_REVISION}"',
        f'const val reviewedImplementationAnchor = "{IMPLEMENTATION_ANCHOR}"',
        f'const val opticalBaselineVersion = "{OPTICAL_BASELINE_VERSION}"',
        f'const val opticalBaselineRevision = "{OPTICAL_BASELINE_REVISION}"',
        f'const val rollbackVersion = "{ROLLBACK_VERSION}"',
        "TEMPORARILY_UNAVAILABLE", "RESTRICTED", "UNKNOWN", "CONFLICT",
        'reasons += "capability-unknown:$capabilityId"',
        'reasons += "capability-conflict:$capabilityId"',
        "automaticExecutionAllowed = false",
        "authorityInferred = false",
        "providerPrecedenceInferred = false",
    ):
        require(capability, marker, "V1.5 capability resolver")

    for marker in (
        "launcher targets exact GLAZE UI v1_5_0 Stable authority",
        "available required capability enables presentation but never automatic execution",
        "missing capability fails closed without disabling unrelated local capability",
        "duplicate capability ownership fails closed without provider precedence",
        "restricted consequential action remains disabled and cannot auto execute",
    ):
        require(capability_test, marker, "V1.5 capability tests")

    for forbidden in (
        "HttpClient", "URLConnection", "Socket(", "Camera", "WallpaperManager",
        "requestPermissions(", "startActivity(", "navigate(",
    ):
        if forbidden in capability:
            fail(f"V1.5 capability resolver gained forbidden authority primitive `{forbidden}`")

    manager_combined = repository + "\n" + manager + "\n" + catalog + "\n" + settings
    for marker in (
        "fun setMode(mode: GlazeThemeMode)",
        "GlazeThemeMode.DARK -> GlazeThemeMode.DEEP_DARK",
        "GlazeThemeManagerCatalog", 'title = "Deep Dark"',
        "GlazeAtmosphere.softAmber", "fun ThemeManagerSurface(",
        "fun LauncherSettingsSurface(", "LauncherSettingsDestinationHost",
    ):
        require(manager_combined, marker, "Theme Manager/Settings")

    for marker in (
        "# GLAZE UI V1.5.0 Migration — GoreeCloud Launcher",
        "Official target: **GLAZE UI V1.5 (`1.5.0`)**",
        f"Exact Stable merged source authority: `{STABLE_REVISION}`",
        f"Reviewed V1.5 implementation anchor: `{IMPLEMENTATION_ANCHOR}`",
        "Inherited optical and immediate rollback baseline: **V1.4.1 (`1.4.1`)**",
        "Shared Glaze V1.5.0 Stable qualification does **not** establish complete Launcher consumer conformance",
        "Platform Contract `0.2`",
        "GoreeCloud Sync is a separately governed application/service capability",
    ):
        require(adoption, marker, "adoption record")

    for marker in (
        "Status: Development — GLAZE UI V1.5.0 migration in progress",
        STABLE_REVISION,
        IMPLEMENTATION_ANCHOR,
        "GlazeCapabilityV15",
        "GlazeOpticalV14",
        "Environmental Color Memory influence accepted by the inherited resolver remains capped at 8%",
        "Shared V1.5.0 qualification does not establish Launcher-local",
    ):
        require(development, marker, "Theme Manager Development evidence")

    for marker in (
        'schema_version: "0.2"',
        "  id: goreecloud-launcher",
        '  glaze_ui:\n    result: applicable-migration-required\n    version: "1.5.0"',
        '  glaze_ui_required: "1.5.0"',
        "goreecloud-platform-contract==0.2",
        "glaze-ui==1.5.0",
        "GoreeCloud Sync integration and acceptance are not established",
        "local backup/restore and portable snapshots are not synchronization evidence",
        "conformance:\n  status: nonconformant",
    ):
        require(platform, marker, "Platform Contract")

    platform_systems_text = platform.split("platform_systems:\n", 1)[1].split("\nhealth:\n", 1)[0]
    if "\n  sync:\n" in platform_systems_text:
        fail("Platform Contract incorrectly classifies GoreeCloud Sync as an eighth Integral Platform System")

    print(
        "GLAZE UI V1.5 Launcher source mapping passed: "
        f"presentation {TARGET_VERSION} at {STABLE_REVISION}; "
        f"optical baseline/rollback {OPTICAL_BASELINE_VERSION} at {OPTICAL_BASELINE_REVISION}; "
        "Platform Contract remains migration-required/nonconformant until "
        "Launcher-local rendered/accessibility/device/performance/release acceptance."
    )


if __name__ == "__main__":
    main()
