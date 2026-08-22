#!/usr/bin/env python3
from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
denied = ("play-services-ads", "firebase-analytics", "facebook-android-sdk", "appsflyer", "adjust-android", "branch-android-sdk")
text = "\n".join(p.read_text(encoding="utf-8", errors="ignore") for p in root.rglob("*") if p.is_file() and p.suffix in {".kts",".xml",".kt",".java"})
bad = [x for x in denied if x.lower() in text.lower()]
manifest = (root/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
if "android.permission.INTERNET" in manifest: bad.append("INTERNET permission")
if bad:
    print("Privacy guard failed:", ", ".join(bad)); sys.exit(1)
print("Privacy guard passed.")
