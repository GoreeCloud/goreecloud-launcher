#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
METRICS = ROOT / "app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"

TARGET_VERSION = "1.5.0"
REFERENCE_REVISION = "2e1618397f6ebcdd254a76bfdd7e98846f2c5aa3"

EXPECTED_METRICS = {
    "space1": 4,
    "space2": 8,
    "space3": 12,
    "space4": 16,
    "space5": 20,
    "space6": 24,
    "space8": 32,
    "radiusSmall": 10,
    "radiusMedium": 14,
    "radiusControl": 16,
    "radiusLarge": 22,
    "radiusExtraLarge": 28,
    "radius2ExtraLarge": 32,
    "minimumTarget": 44,
    "comfortableTarget": 48,
}


def fail(message: str) -> None:
    raise SystemExit(f"Glaze UI contract failed: {message}")


def main() -> None:
    if not METRICS.is_file():
        fail(f"missing native metric map: {METRICS.relative_to(ROOT)}")
    if not ADOPTION.is_file():
        fail(f"missing adoption evidence: {ADOPTION.relative_to(ROOT)}")

    metrics_text = METRICS.read_text(encoding="utf-8")
    adoption_text = ADOPTION.read_text(encoding="utf-8")

    for name, value in EXPECTED_METRICS.items():
        expected = f"val {name}: Dp = {value}.dp"
        if expected not in metrics_text:
            fail(f"expected canonical 1.5 metric mapping `{expected}`")

    required_evidence = [
        "Canonical design system: `GoreeCloud/goreecloud-glaze-ui`",
        "Target: Glaze UI 1.5 Stable",
        f"Canonical token version reviewed: {TARGET_VERSION}",
        f"Reviewed canonical revision: `{REFERENCE_REVISION}`",
        "Adoption state: Adoption Candidate",
        "Production eligible on Glaze UI gate: no",
        "Adoption mode: native Android semantic mapping",
        "Full Glaze UI 1.5 conformance.",
        "Complete Stable motion-token mapping or reduced-motion acceptance.",
        "Phone/tablet rendered visual acceptance.",
        "Physical-device launcher acceptance.",
    ]
    for evidence in required_evidence:
        if evidence not in adoption_text:
            fail(f"missing adoption evidence `{evidence}`")

    stale_markers = [
        "Target: Glaze UI 1.4 Stable",
        "Canonical token version reviewed: 1.4.0",
        "Native Android mapping of the Glaze UI 1.4 Stable",
    ]
    combined = adoption_text + "\n" + metrics_text
    for marker in stale_markers:
        if marker in combined:
            fail(f"stale historical target remains active: `{marker}`")

    print(
        "Glaze UI 1.5 Launcher Adoption Candidate contract passed: "
        f"{len(EXPECTED_METRICS)} native metrics + exact Stable evidence boundary validated."
    )


if __name__ == "__main__":
    main()
