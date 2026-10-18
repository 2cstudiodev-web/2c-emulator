#!/usr/bin/env bash
# ui.sh dump            -> list clickable/text elements
# ui.sh tap "<text>"    -> tap first element whose text/desc contains <text>
export MSYS_NO_PATHCONV=1
ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
"$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
OUT="${DEVICE_TEST_OUT:-$(dirname "$0")/out}"; mkdir -p "$OUT"; export UI_XML="$OUT/ui.xml"
"$ADB" pull /sdcard/ui.xml "$UI_XML" >/dev/null 2>&1
python -I -X utf8 - "$@" <<'PY'
import sys, re, subprocess, os
xml = open(os.environ["UI_XML"], encoding="utf-8").read()
nodes = re.findall(r'<node [^>]*>', xml)
items = []
for n in nodes:
    a = dict(re.findall(r'([\w-]+)="([^"]*)"', n))
    label = a.get("text") or a.get("content-desc") or ""
    if not label: continue
    x1,y1,x2,y2 = map(int, re.findall(r'\d+', a["bounds"]))
    items.append((label, (x1+x2)//2, (y1+y2)//2, a.get("clickable")))
if len(sys.argv) < 2 or sys.argv[1] == "dump":
    for l,x,y,c in items: print(f"{l[:70]!r} @ {x},{y} click={c}")
elif sys.argv[1] == "full":
    import html
    for l,x,y,c in items: print(html.unescape(l))
else:
    q = sys.argv[2].lower()
    items.sort(key=lambda it: it[0].lower().strip()!=q)
    for l,x,y,c in items:
        if q in l.lower():
            adb = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
            subprocess.run([adb, "shell", "input", "tap", str(x), str(y)])
            print(f"tapped {l!r} @ {x},{y}"); break
    else:
        print("NOT FOUND:", q); sys.exit(1)
PY
