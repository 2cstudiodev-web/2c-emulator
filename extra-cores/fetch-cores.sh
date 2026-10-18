#!/usr/bin/env bash
# Downloads the libretro cores that 2C Emulator ships on top of the upstream
# lemuroid-cores submodule. Binaries are not committed (MAME alone is ~145 MB).
set -euo pipefail

BASE_URL="https://buildbot.libretro.com/nightly/android/latest/arm64-v8a"
DEST="$(cd "$(dirname "$0")" && pwd)/jniLibs/arm64-v8a"
CORES=(bsnes blastem gearsystem mame2016 play)

mkdir -p "$DEST"
for core in "${CORES[@]}"; do
    file="${core}_libretro_android.so"
    if [[ -f "$DEST/lib$file" && "${FORCE:-0}" != "1" ]]; then
        echo "✓ $core (cached)"
        continue
    fi
    echo "↓ $core"
    tmp="$(mktemp -d)"
    curl -fsSL "$BASE_URL/$file.zip" -o "$tmp/core.zip"
    unzip -oq "$tmp/core.zip" -d "$tmp"
    mv "$tmp/$file" "$DEST/lib$file"
    rm -rf "$tmp"
done
