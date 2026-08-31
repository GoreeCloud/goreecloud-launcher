#!/usr/bin/env python3
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

m = Path(__file__).resolve().parents[1] / "app/src/main/AndroidManifest.xml"
root = ET.parse(m).getroot()
ns = "{http://schemas.android.com/apk/res/android}"

permissions = {x.attrib.get(ns + "name") for x in root.findall("uses-permission")}
if permissions:
    print("Unexpected permissions:", sorted(permissions))
    sys.exit(1)

text = m.read_text(encoding="utf-8")
for required in (
    "android.intent.category.HOME",
    "android.intent.category.DEFAULT",
):
    if required not in text:
        print("Missing:", required)
        sys.exit(1)

queries = root.find("queries")
if queries is None:
    print("Missing <queries> package-visibility declaration for launchable apps.")
    sys.exit(1)

has_launcher_query = False
has_index_search_query = False
for intent in queries.findall("intent"):
    action_names = {
        action.attrib.get(ns + "name")
        for action in intent.findall("action")
    }
    category_names = {
        category.attrib.get(ns + "name")
        for category in intent.findall("category")
    }
    if (
        "android.intent.action.MAIN" in action_names
        and "android.intent.category.LAUNCHER" in category_names
    ):
        has_launcher_query = True
    if "com.goreecloud.index.action.SEARCH" in action_names:
        has_index_search_query = True

if not has_launcher_query:
    print("Missing MAIN/LAUNCHER visibility query required for complete app discovery.")
    sys.exit(1)

if not has_index_search_query:
    print("Missing bounded GoreeCloud Index search visibility query.")
    sys.exit(1)

if "android.permission.QUERY_ALL_PACKAGES" in text:
    print("Broad QUERY_ALL_PACKAGES visibility is not permitted.")
    sys.exit(1)

print("Manifest guard passed.")
