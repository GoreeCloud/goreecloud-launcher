#!/usr/bin/env python3
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
required = [
    root / "branding/source/goreecloud-launcher-icon.svg",
    root / "branding/README.md",
    root / "docs/product-identity.md",
    root / "app/src/main/res/values/colors.xml",
    root / "app/src/main/res/drawable/ic_launcher_foreground.xml",
    root / "app/src/main/res/drawable/ic_launcher_monochrome.xml",
    root / "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
    root / "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
    root / "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml",
    root / "app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml",
]
missing = [str(path.relative_to(root)) for path in required if not path.is_file()]
if missing:
    print("Missing Launcher identity candidate files:", missing)
    sys.exit(1)

for path in required:
    if path.suffix in {".xml", ".svg"}:
        try:
            ET.parse(path)
        except ET.ParseError as exc:
            print(f"Invalid XML/SVG in {path.relative_to(root)}: {exc}")
            sys.exit(1)

manifest = ET.parse(root / "app/src/main/AndroidManifest.xml").getroot()
application = manifest.find("application")
ns = "{http://schemas.android.com/apk/res/android}"
if application is None:
    print("Missing Android <application> element.")
    sys.exit(1)
if application.attrib.get(ns + "icon") != "@mipmap/ic_launcher":
    print("Android application icon is not wired to @mipmap/ic_launcher.")
    sys.exit(1)
if application.attrib.get(ns + "roundIcon") != "@mipmap/ic_launcher_round":
    print("Android round icon is not wired to @mipmap/ic_launcher_round.")
    sys.exit(1)

v33 = (root / "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml").read_text(encoding="utf-8")
if "<monochrome" not in v33 or "@drawable/ic_launcher_monochrome" not in v33:
    print("Android 13+ adaptive icon is missing its monochrome derivative.")
    sys.exit(1)

source = (root / "branding/source/goreecloud-launcher-icon.svg").read_text(encoding="utf-8")
for required_token in ("#18214B", "#F7F9FF", "#6FE7FF"):
    if required_token not in source:
        print("Canonical candidate source is missing expected identity token:", required_token)
        sys.exit(1)

identity_doc = (root / "docs/product-identity.md").read_text(encoding="utf-8").lower()
if "development identity candidate" not in identity_doc:
    print("Identity documentation must preserve candidate status.")
    sys.exit(1)
if "not yet the approved official" not in identity_doc:
    print("Identity documentation must preserve the explicit approval boundary.")
    sys.exit(1)

print("Launcher identity candidate guard passed.")
