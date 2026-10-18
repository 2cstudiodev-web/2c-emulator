# Device smoke tests

Scripts that drive a debug build of 2C Emulator on a USB-connected phone (developed on a Solana
Seeker, Windows + Git Bash) to launch every game of each system, take a screenshot and check
for crashes. They were used for the 2026-10-18 compatibility pass (202 homebrew games).

Requirements: `adb` in `%LOCALAPPDATA%\Android\Sdk\platform-tools`, Python 3 with Pillow,
the debug APK installed, the terms accepted, notifications allowed, and a ROM folder already
picked in the app. Keep the phone unlocked; portrait and landscape both work.

Only use games you are allowed to use: open-source or free homebrew, or backups of your own
cartridges and discs. No game files belong in this repository.

| File | What it does |
|---|---|
| `pushsys.sh <folder> <phone folder> <ext regex>` | Copies `$TESTGAMES/<folder>/*` (default `C:/dev/testgames`) to `/sdcard/2CGames/<phone folder>/` |
| `rescan.sh [seconds]` | Rescans the library in the app and prints the game count per system |
| `batchtest.py <systemIds> "<Systems tab title>" [seconds]` | Opens every game of a system, waits, screenshots, records OK / DARK / CRASH / ERROR |
| `runbatch.sh systems.txt` | Runs `batchtest.py` for every line `ids\|title\|seconds` of the list |
| `recheck.py <systemIds> "<title>" <file>...` | Retests specific games with a longer wait and a Start press |
| `contactsheet.py [system...]` | Builds one labelled screenshot grid per system |
| `ui.sh dump\|tap "<text>"\|full` | Reads or taps the phone UI through uiautomator |
| `make_ps2_iso.py <elf> <iso>` | Wraps a PS2 homebrew ELF into a bootable ISO (needs `pip install pycdlib`) |

Output goes to `$DEVICE_TEST_OUT` (default `./out`, ignored by git). Run Python with
`python -I -X utf8` and feed loops `< /dev/null`, because adb otherwise eats the loop's stdin.

"DARK" is only a pixel count: many games show a dim title screen. Brighten the screenshot and
look before calling it a bug.
