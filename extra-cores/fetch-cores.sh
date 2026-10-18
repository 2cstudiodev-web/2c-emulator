#!/usr/bin/env bash
# Downloads the libretro cores that 2C Emulator ships on top of the upstream
# lemuroid-cores submodule. Binaries are not committed (MAME alone is ~145 MB).
#
# By default this fetches the pinned archive published on the project's GitHub
# releases (the exact binaries tested on a Solana Seeker) and checks its SHA-256.
# NIGHTLY=1 fetches the latest libretro buildbot nightlies instead (untested).
set -euo pipefail

PINNED_URL="https://github.com/2cstudiodev-web/2c-emulator/releases/download/cores-2026-10-18/extra-cores-2026-10-18.zip"
PINNED_SHA256="60cdd35e2d616061661a6f608505a21585bb9f74c9267723b1a6b0fdc4ef8dd6"
NIGHTLY_URL="https://buildbot.libretro.com/nightly/android/latest/arm64-v8a"

ROOT="$(cd "$(dirname "$0")" && pwd)"
DEST="$ROOT/jniLibs/arm64-v8a"
CORES=(bsnes blastem gearsystem mame2016 play)

sha256() {
    if command -v sha256sum >/dev/null; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}

all_cached() {
    for core in "${CORES[@]}"; do
        [[ -f "$DEST/lib${core}_libretro_android.so" ]] || return 1
    done
}

mkdir -p "$DEST"
if all_cached && [[ "${FORCE:-0}" != "1" ]]; then
    echo "✓ all cores cached (FORCE=1 to download again)"
    exit 0
fi

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

if [[ "${NIGHTLY:-0}" == "1" ]]; then
    for core in "${CORES[@]}"; do
        file="${core}_libretro_android.so"
        echo "↓ $core (nightly)"
        curl -fsSL "$NIGHTLY_URL/$file.zip" -o "$tmp/core.zip"
        unzip -oq "$tmp/core.zip" -d "$tmp"
        mv "$tmp/$file" "$DEST/lib$file"
    done
    exit 0
fi

echo "↓ pinned cores ($(basename "$PINNED_URL"))"
curl -fsSL "$PINNED_URL" -o "$tmp/cores.zip"
actual="$(sha256 "$tmp/cores.zip")"
if [[ "$actual" != "$PINNED_SHA256" ]]; then
    echo "✗ checksum mismatch: expected $PINNED_SHA256, got $actual" >&2
    exit 1
fi
unzip -oq "$tmp/cores.zip" -d "$ROOT/jniLibs"
for core in "${CORES[@]}"; do
    test -s "$DEST/lib${core}_libretro_android.so" || { echo "✗ missing $core" >&2; exit 1; }
    echo "✓ $core"
done
