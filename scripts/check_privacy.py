#!/usr/bin/env python3
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
denied = (
    "play-services-ads",
    "firebase-analytics",
    "facebook-android-sdk",
    "appsflyer",
    "adjust-android",
    "branch-android-sdk",
)
text = "\n".join(
    p.read_text(encoding="utf-8", errors="ignore")
    for p in root.rglob("*")
    if p.is_file() and p.suffix in {".kts", ".xml", ".kt", ".java"}
)
bad = [x for x in denied if x.lower() in text.lower()]

manifest_path = root / "app/src/main/AndroidManifest.xml"
manifest = manifest_path.read_text(encoding="utf-8")
manifest_root = ET.parse(manifest_path).getroot()
android_ns = "{http://schemas.android.com/apk/res/android}"
application = manifest_root.find("application")

if "android.permission.INTERNET" in manifest:
    bad.append("INTERNET permission")
if "android.permission.QUERY_ALL_PACKAGES" in manifest:
    bad.append("QUERY_ALL_PACKAGES permission")
if application is None:
    bad.append("missing application manifest node")
elif application.attrib.get(android_ns + "allowBackup") != "false":
    bad.append("automatic Android backup must remain disabled")

if bad:
    print("Privacy guard failed:", ", ".join(bad))
    sys.exit(1)
print("Privacy guard passed.")
