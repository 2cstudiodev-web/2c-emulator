#!/usr/bin/env bash
# pushsys.sh <local folder> <phone folder> <ext regex>
export MSYS_NO_PATHCONV=1
ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
"$ADB" shell mkdir -p "/sdcard/2CGames/$2"
n=0
for f in "${TESTGAMES:-C:/dev/testgames}"/$1/*; do
  [ -f "$f" ] || continue
  echo "$f" | grep -qiE "\.($3)$" || continue
  "$ADB" push "$f" "/sdcard/2CGames/$2/" >/dev/null && n=$((n+1))
done
echo "$1 -> $2: pushed $n files"
