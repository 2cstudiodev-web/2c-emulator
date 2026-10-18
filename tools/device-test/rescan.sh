#!/usr/bin/env bash
export MSYS_NO_PATHCONV=1
ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
"$ADB" shell am force-stop com.twocstudio.emulator.debug
"$ADB" shell am start -n com.twocstudio.emulator.debug/com.swordfish.lemuroid.app.mobile.feature.main.MainActivity >/dev/null
sleep 3
HERE="$(dirname "$0")"; OUT="${DEVICE_TEST_OUT:-$HERE/out}"; mkdir -p "$OUT/db"
bash "$HERE/ui.sh" tap "Settings" >/dev/null; sleep 2
bash "$HERE/ui.sh" tap "Rescan"
sleep "${1:-40}"
for f in retrograde retrograde-wal retrograde-shm; do "$ADB" exec-out run-as com.twocstudio.emulator.debug cat databases/$f > "$OUT/db/$f"; done
python -I -c "
import sqlite3; c=sqlite3.connect('$OUT/db/retrograde')
print(dict(c.execute('select systemId, count(*) from games group by systemId').fetchall()))"
