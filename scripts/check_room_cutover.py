#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PRODUCTION_ROOT = ROOT / "app" / "src" / "main" / "java"
AUTHORITY_REPOSITORY = PRODUCTION_ROOT / "com" / "goreecloud" / "launcher" / "core" / "workspace" / "WorkspaceRepository.kt"
PLACEMENT_REPOSITORY = PRODUCTION_ROOT / "com" / "goreecloud" / "launcher" / "core" / "workspace" / "db" / "WorkspaceRoomPlacementRepository.kt"

errors: list[str] = []

for path in PRODUCTION_ROOT.rglob("*.kt"):
    text = path.read_text(encoding="utf-8")
    promotion_calls = text.count("promoteRoomAuthority(")
    if path == AUTHORITY_REPOSITORY:
        if promotion_calls != 1:
            errors.append(
                "WorkspaceRepository.kt must contain exactly the guarded promotion primitive definition."
            )
    elif promotion_calls:
        errors.append(
            f"Production Room promotion call is not accepted yet: {path.relative_to(ROOT)}"
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

print("Room cutover guard passed: promotion and ROOM-only placement routing remain uncalled in production.")
