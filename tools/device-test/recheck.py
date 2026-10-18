"""Re-test specific games: longer wait, press Start, screenshot before/after.

usage: python -I -X utf8 recheck.py <systemIds> "<Systems title>" <file1> [file2 ...]
Saves $DEVICE_TEST_OUT/recheck/<file>-a.png (after wait) and -b.png (after pressing Start).
"""

import os
import re
import sqlite3
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import batchtest as bt  # noqa: E402

OUT = os.path.join(bt.SHOTS, "recheck")


def title_for(system_ids, filename):
    con = sqlite3.connect(f"{bt.DB_DIR}/retrograde")
    ids = system_ids.split(",")
    row = con.execute(
        f"select title from games where fileName = ? and systemId in ({','.join('?' * len(ids))})",
        [filename, *ids],
    ).fetchone()
    return row[0] if row else None


def press_start():
    # Hardware-style key events first, then the on-screen Start button in the portrait layouts.
    bt.shell("input keyevent KEYCODE_BUTTON_START")
    time.sleep(0.5)
    bt.shell("input keyevent KEYCODE_ENTER")
    time.sleep(0.5)
    for x, y in ((714, 1932), (1060, 1760), (900, 2100)):
        bt.shell(f"input swipe {x} {y} {x} {y} 120")
        time.sleep(0.6)


def main():
    system_ids, system_title, files = sys.argv[1], sys.argv[2], sys.argv[3:]
    os.makedirs(OUT, exist_ok=True)
    bt.load_titles(system_ids)
    for filename in files:
        title = title_for(system_ids, filename)
        bt.shell(f"am force-stop {bt.PKG}")
        bt.shell(f"am start -n {bt.MAIN}")
        time.sleep(3)
        bt.tap_text("Systems", 0)
        time.sleep(2)
        bt.tap_text(system_title)
        time.sleep(2)
        bt.adb("logcat", "-c")
        if not bt.tap_text(title):
            print(f"{filename}: not found ({title})")
            continue
        time.sleep(35)
        a = f"{OUT}/{filename}-a.png"
        open(a, "wb").write(bt.adb("exec-out", "screencap", "-p", binary=True))
        press_start()
        time.sleep(6)
        b = f"{OUT}/{filename}-b.png"
        open(b, "wb").write(bt.adb("exec-out", "screencap", "-p", binary=True))
        top = bt.shell("dumpsys activity activities | grep topResumedActivity")
        log = bt.adb("logcat", "-d", "-v", "brief")
        crash = [l for l in log.split("\n") if re.search(r"Abort message|F/libc |FATAL EXCEPTION|reverting to SIG_DFL", l)]
        print(f"{filename}: lit_a={bt.lit_percent(a)}% lit_b={bt.lit_percent(b)}% "
              f"{'GAME' if 'GameActivity' in top else 'NOT-GAME'} {crash[0][:150] if crash else ''}")
    bt.shell(f"am force-stop {bt.PKG}")


if __name__ == "__main__":
    main()
