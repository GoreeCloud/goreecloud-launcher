#!/usr/bin/env python3
from pathlib import Path
import sys, xml.etree.ElementTree as ET
m = Path(__file__).resolve().parents[1]/"app/src/main/AndroidManifest.xml"
root = ET.parse(m).getroot()
ns = "{http://schemas.android.com/apk/res/android}"
permissions = {x.attrib.get(ns+"name") for x in root.findall("uses-permission")}
if permissions:
    print("Unexpected permissions:", sorted(permissions)); sys.exit(1)
text = m.read_text(encoding="utf-8")
for required in ("android.intent.category.HOME","android.intent.category.DEFAULT"):
    if required not in text:
        print("Missing:", required); sys.exit(1)
print("Manifest guard passed.")
