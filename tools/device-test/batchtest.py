"""Smoke-test every game of one system in 2C Emulator on the USB-connected phone.

usage: python -I -X utf8 batchtest.py <systemId> "<Systems tab title>" [seconds]

For each game: open it from the Systems tab, wait, screenshot, check for crashes.
Appends results to $DEVICE_TEST_OUT/results.csv and saves $DEVICE_TEST_OUT/<system>/<n>.png
(DEVICE_TEST_OUT defaults to ./out next to this script).
"""

import csv
import os
import re
import sqlite3
import subprocess
import sys
import time

from PIL import Image

ADB = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
PKG = os.environ.get("APP_PACKAGE", "com.twocstudio.emulator.debug")
MAIN = PKG + "/com.swordfish.lemuroid.app.mobile.feature.main.MainActivity"
SHOTS = os.environ.get("DEVICE_TEST_OUT", os.path.join(os.path.dirname(os.path.abspath(__file__)), "out"))
DB_DIR = os.path.join(SHOTS, "db")


def adb(*args, binary=False):
    out = subprocess.run([ADB, *args], capture_output=True)
    return out.stdout if binary else out.stdout.decode("utf-8", "replace")


def shell(cmd):
    return adb("shell", cmd)


def ui_items():
    shell("uiautomator dump /sdcard/ui.xml")
    xml = adb("exec-out", "cat", "/sdcard/ui.xml")
    items = []
    for node in re.findall(r"<node [^>]*>", xml):
        attrs = dict(re.findall(r'([\w-]+)="([^"]*)"', node))
        label = attrs.get("text") or attrs.get("content-desc") or ""
        if not label:
            continue
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", attrs["bounds"]))
        items.append((label, (x1 + x2) // 2, (y1 + y2) // 2))
    return items


def screen_size():
    png = adb("exec-out", "screencap", "-p", binary=True)
    return int.from_bytes(png[16:20], "big"), int.from_bytes(png[20:24], "big")


def tap_text(text, scrolls=14):
    w, h = screen_size()
    for _ in range(scrolls + 1):
        items = ui_items()
        exact = [i for i in items if i[0].strip() == text]
        if exact:
            _, x, y = exact[0]
            shell(f"input tap {x} {y}")
            return True
        shell(f"input swipe {w // 2} {int(h * 0.75)} {w // 2} {int(h * 0.35)} 350")
        time.sleep(1)
    return False


def load_titles(system_id):
    os.makedirs(DB_DIR, exist_ok=True)
    # Release builds are not debuggable, so run-as fails; set SKIP_DB_PULL=1 to reuse the debug
    # build's copy (both builds scan the same folder into the same titles).
    for f in ("retrograde", "retrograde-wal", "retrograde-shm") if not os.environ.get("SKIP_DB_PULL") else ():
        data = adb("exec-out", "run-as", PKG, "cat", f"databases/{f}", binary=True)
        open(f"{DB_DIR}/{f}", "wb").write(data)
    con = sqlite3.connect(f"{DB_DIR}/retrograde")
    ids = system_id.split(",")
    rows = con.execute(
        f"select title, fileName from games where systemId in ({','.join('?' * len(ids))}) order by title collate nocase",
        ids,
    ).fetchall()
    con.close()
    return rows


def lit_percent(path):
    im = Image.open(path).convert("L")
    w, h = im.size
    area = im.crop((0, int(h * 0.06), w, int(h * 0.55))) if h > w else im.crop((int(w * 0.18), 0, int(w * 0.82), h))
    pixels = list(area.get_flattened_data()) if hasattr(area, "get_flattened_data") else list(area.getdata())
    return sum(1 for p in pixels if p > 40) * 100 // len(pixels)


def test_game(system_id, system_title, title, filename, index, wait):
    shell(f"am force-stop {PKG}")
    shell(f"am start -n {MAIN}")
    time.sleep(3)
    if not tap_text("Systems", 0):
        return "NAV", "Systems tab not found"
    time.sleep(2)
    if not tap_text(system_title):
        return "NAV", f"system '{system_title}' not found"
    time.sleep(2)
    adb("logcat", "-c")
    if not tap_text(title):
        return "NAV", f"game '{title}' not found in list"
    time.sleep(wait)
    top = shell("dumpsys activity activities | grep topResumedActivity")
    folder = system_id.replace(",", "+")
    os.makedirs(f"{SHOTS}/{folder}", exist_ok=True)
    shot = f"{SHOTS}/{folder}/{index:02d}.png"
    open(shot, "wb").write(adb("exec-out", "screencap", "-p", binary=True))
    log = adb("logcat", "-d", "-v", "brief")
    crash = [l for l in log.split("\n") if re.search(r"Abort message|F/libc |FATAL EXCEPTION|reverting to SIG_DFL|Cannot dlopen", l)]
    lit = lit_percent(shot)
    if "GameCrashActivity" in top:
        return "ERROR", "core error screen; " + (crash[0][:160] if crash else "")
    if crash:
        return "CRASH", crash[0][:200]
    if "GameActivity" not in top:
        return "EXITED", top.strip()[-120:]
    if lit <= 1:
        return "DARK", f"lit={lit}%"
    return "OK", f"lit={lit}%"


def main():
    system_id, system_title = sys.argv[1], sys.argv[2]
    wait = int(sys.argv[3]) if len(sys.argv) > 3 else 15
    games = load_titles(system_id)
    print(f"{system_id}: {len(games)} games")
    new_file = not os.path.exists(f"{SHOTS}/results.csv")
    with open(f"{SHOTS}/results.csv", "a", newline="", encoding="utf-8") as out:
        writer = csv.writer(out)
        if new_file:
            writer.writerow(["system", "index", "title", "file", "status", "detail"])
        for index, (title, filename) in enumerate(games):
            status, detail = test_game(system_id, system_title, title, filename, index, wait)
            writer.writerow([system_id, index, title, filename, status, detail])
            out.flush()
            print(f"  {index:02d} {status:6} {title} ({filename}) {detail}")
    shell(f"am force-stop {PKG}")


if __name__ == "__main__":
    main()
