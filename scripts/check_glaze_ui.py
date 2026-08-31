#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
METRICS = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt"
THEME = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeTheme.kt"
THEME_REPOSITORY = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeThemeRepository.kt"
THEME_MANAGER = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/ThemeManagerSurface.kt"
THEME_CATALOG = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeThemeManagerCatalog.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"

TARGET_VERSION = "2.1.0"
REFERENCE_REVISION = "c49113eb8b93c267613fdf1bbca1f814495acad7"

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
        (ADOPTION, "adoption evidence"),
    )
    for path, label in required_files:
        if not path.is_file():
            fail(f"missing {label}: {path.relative_to(ROOT)}")

    metrics_text = METRICS.read_text(encoding="utf-8")
    theme_text = THEME.read_text(encoding="utf-8")
    repository_text = THEME_REPOSITORY.read_text(encoding="utf-8")
    manager_text = THEME_MANAGER.read_text(encoding="utf-8")
    catalog_text = THEME_CATALOG.read_text(encoding="utf-8")
    adoption_text = ADOPTION.read_text(encoding="utf-8")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics_text:
            fail(f"expected Glaze UI 2.1 metric mapping `{expected}`")

    for invented_alias in ("val space7:", "val space9:"):
        if invented_alias in metrics_text:
            fail(f"non-canonical sequential spacing alias remains active: `{invented_alias}`")

    for marker in EXPECTED_THEME_MARKERS:
        if marker not in theme_text:
            fail(f"missing Glaze UI 2.1 Light/Dark theme evidence `{marker}`")

    manager_markers = [
        "fun setMode(mode: GlazeThemeMode)",
        "GlazeThemeManagerCatalog",
        "fun ThemeManagerSurface(",
        "Icon packs, masking, Deep Dark",
    ]
    manager_combined = repository_text + "\n" + manager_text + "\n" + catalog_text
    for marker in manager_markers:
        if marker not in manager_combined:
            fail(f"missing bounded Theme Manager foundation evidence `{marker}`")

    required_evidence = [
        "Canonical design system: `GoreeCloud/goreecloud-glaze-ui`",
        "Target: Glaze UI 2.1 Stable",
        f"Canonical token version reviewed: {TARGET_VERSION}",
        f"Reviewed canonical revision: `{REFERENCE_REVISION}`",
        "Adoption state: Adoption Candidate",
        "Production eligible on Glaze UI gate: no",
        "Adoption mode: native Android semantic mapping",
        "Implemented 2.1 mapping",
        "canonical spacing token keys",
        "48 dp current-contract general interaction floor",
        "56 dp touch-assistance target",
        "compatibility token `target.minimum=44`",
        "Reduced transparency must resolve to Solid",
        "Deep Dark is not approximated with an invented palette",
        "Theme Manager surface is source foundation",
        "Historical migration evidence",
        "Full Glaze UI 2.1 conformance.",
        "Complete Accessibility Resolution Matrix integration.",
        "TalkBack or switch-access acceptance.",
        "Phone/tablet rendered/native visual acceptance.",
        "Representative physical-device launcher acceptance.",
    ]
    for evidence in required_evidence:
        if evidence not in adoption_text:
            fail(f"missing adoption evidence `{evidence}`")

    stale_active_markers = [
        "Target: Glaze UI 2.0 Stable",
        "Canonical token version reviewed: 2.0.0",
        "Native Android mapping of the Glaze UI 2.0 Stable",
        "Target: Glaze UI 1.6 Stable",
        "Canonical token version reviewed: 1.6.0",
        "Glaze UI 1.6 Stable remains the production design-system authority",
        "Native Android mapping of the Glaze UI 1.5 Stable",
        "Target: Glaze UI 1.5 Stable",
        "Canonical token version reviewed: 1.5.0",
        "Target: Glaze UI 1.4 Stable",
        "Canonical token version reviewed: 1.4.0",
        "Native Android mapping of the Glaze UI 1.4 Stable",
    ]
    combined = adoption_text + "\n" + metrics_text + "\n" + theme_text
    for marker in stale_active_markers:
        if marker in combined:
            fail(f"stale historical target remains active: `{marker}`")

    if "val minimumTarget: Dp = 44.dp" in combined:
        fail("superseded 44 dp general target floor remains in active Glaze mapping")

    print(
        "Glaze UI 2.1 Launcher Adoption Candidate contract passed: "
        f"{len(EXPECTED_METRICS)} native metrics + canonical spacing token identities + "
        "promoted Light/Dark mapping + bounded Theme Manager foundation + exact Stable evidence boundary validated."
    )


if __name__ == "__main__":
    main()
