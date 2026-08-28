#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
METRICS = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt"
THEME = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeTheme.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"

TARGET_VERSION = "2.0.0"
REFERENCE_REVISION = "ff3fff4306bd53ea9c0715a7c0d64265bb038617"

EXPECTED_METRICS = {
    "space1": 4,
    "space2": 8,
    "space3": 12,
    "space4": 16,
    "space5": 24,
    "space6": 32,
    "space8": 48,
    "radiusSmall": 8,
    "radiusMedium": 12,
    "radiusControl": 999,
    "radiusLarge": 16,
    "radiusExtraLarge": 24,
    "radius2ExtraLarge": 32,
    "minimumTarget": 48,
    "comfortableTarget": 48,
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
    for path, label in ((METRICS, "native metric map"), (THEME, "native theme map"), (ADOPTION, "adoption evidence")):
        if not path.is_file():
            fail(f"missing {label}: {path.relative_to(ROOT)}")

    metrics_text = METRICS.read_text(encoding="utf-8")
    theme_text = THEME.read_text(encoding="utf-8")
    adoption_text = ADOPTION.read_text(encoding="utf-8")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics_text:
            fail(f"expected Glaze UI 2.0 metric mapping `{expected}`")

    for marker in EXPECTED_THEME_MARKERS:
        if marker not in theme_text:
            fail(f"missing Glaze UI 2.0 Light/Dark theme evidence `{marker}`")

    required_evidence = [
        "Canonical design system: `GoreeCloud/goreecloud-glaze-ui`",
        "Target: Glaze UI 2.0 Stable",
        f"Canonical token version reviewed: {TARGET_VERSION}",
        f"Reviewed canonical revision: `{REFERENCE_REVISION}`",
        "Adoption state: Adoption Candidate",
        "Production eligible on Glaze UI gate: no",
        "Adoption mode: native Android semantic mapping",
        "Implemented 2.0 mapping",
        "48 dp general interaction floor",
        "Deep Dark is not approximated with an invented palette",
        "Historical migration evidence",
        "Full Glaze UI 2.0 conformance.",
        "Complete 2.0 motion-token mapping or reduced-motion acceptance.",
        "TalkBack or switch-access acceptance.",
        "Phone/tablet rendered/native visual acceptance.",
        "Representative physical-device launcher acceptance.",
    ]
    for evidence in required_evidence:
        if evidence not in adoption_text:
            fail(f"missing adoption evidence `{evidence}`")

    stale_active_markers = [
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
        "Glaze UI 2.0 Launcher Adoption Candidate contract passed: "
        f"{len(EXPECTED_METRICS)} native metrics + promoted Light/Dark token mapping + exact Stable evidence boundary validated."
    )


if __name__ == "__main__":
    main()
