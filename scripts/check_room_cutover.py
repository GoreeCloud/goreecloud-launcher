#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PRODUCTION_ROOT = ROOT / "app" / "src" / "main" / "java"
AUTHORITY_REPOSITORY = PRODUCTION_ROOT / "com" / "goreecloud" / "launcher" / "core" / "workspace" / "WorkspaceRepository.kt"
PROMOTION_COORDINATOR = PRODUCTION_ROOT / "com" / "goreecloud" / "launcher" / "core" / "workspace" / "db" / "WorkspaceProductionPromotionCoordinator.kt"
PLACEMENT_REPOSITORY = PRODUCTION_ROOT / "com" / "goreecloud" / "launcher" / "core" / "workspace" / "db" / "WorkspaceRoomPlacementRepository.kt"

NON_EXECUTABLE_KOTLIN = re.compile(
    r'""".*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|/\*.*?\*/|//[^\n]*',
    re.DOTALL,
)


def executable_kotlin(text: str) -> str:
    """Remove comments and literals so documentation text cannot satisfy or trip code guards."""
    return NON_EXECUTABLE_KOTLIN.sub("", text)


errors: list[str] = []

for path in PRODUCTION_ROOT.rglob("*.kt"):
    text = executable_kotlin(path.read_text(encoding="utf-8"))
    promotion_calls = text.count("promoteRoomAuthority(")
    if path == AUTHORITY_REPOSITORY:
        if promotion_calls != 1:
            errors.append(
                "WorkspaceRepository.kt must contain exactly the guarded promotion primitive definition."
            )
    elif path == PROMOTION_COORDINATOR:
        if promotion_calls != 1:
            errors.append(
                "WorkspaceProductionPromotionCoordinator.kt must contain exactly one guarded promotion call."
            )
    elif promotion_calls:
        errors.append(
            f"Production Room promotion call is outside the reviewed coordinator: {path.relative_to(ROOT)}"
        )

    coordinator_references = text.count("WorkspaceProductionPromotionCoordinator(")
    if path == PROMOTION_COORDINATOR:
        if coordinator_references != 1:
            errors.append(
                "WorkspaceProductionPromotionCoordinator.kt must contain exactly its class declaration."
            )
    elif coordinator_references:
        errors.append(
            "Production activation of WorkspaceProductionPromotionCoordinator is not accepted yet: "
            f"{path.relative_to(ROOT)}"
        )

    placement_references = text.count("WorkspaceRoomPlacementRepository(")
    if path == PLACEMENT_REPOSITORY:
        if placement_references != 1:
            errors.append(
                "WorkspaceRoomPlacementRepository.kt must contain exactly its class declaration."
            )
    elif placement_references:
        errors.append(
            "Production Home/launcher routing to WorkspaceRoomPlacementRepository is not accepted yet: "
            f"{path.relative_to(ROOT)}"
        )

if errors:
    print("Room cutover guard failed:")
    for error in errors:
        print(f"- {error}")
    raise SystemExit(1)

print(
    "Room cutover guard passed: the reviewed promotion coordinator exists but remains unwired, "
    "and ROOM-only Home placement routing remains inactive in production."
)
