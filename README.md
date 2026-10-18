# 2C Emulator

Android retro game emulator by 2C Studio. Players load their **own** game files — the app ships
no games, no BIOS files and no encryption keys.

Based on [Lemuroid](https://github.com/Swordfish90/Lemuroid) by Filippo Scognamiglio, licensed
under the GNU GPL v3. 2C Emulator is distributed under the same license (see `COPYING`); the
original upstream README is kept in `README.lemuroid.md`.

## Supported systems and cores

Only cores whose licenses allow commercial distribution are shipped.

| System | Core | License |
|---|---|---|
| NES | FCEUmm | GPLv2 |
| SNES | bsnes | GPLv3 |
| Game Boy / Color | Gambatte | GPLv2 |
| Game Boy Advance | mGBA | MPL 2.0 |
| Nintendo 64 | Mupen64Plus-Next | GPLv2 |
| Nintendo DS | melonDS / DeSmuME | GPLv3 / GPLv2 |
| Nintendo 3DS (beta, decrypted dumps only) | Citra | GPLv2+ |
| Master System / Game Gear | Gearsystem | GPLv3 |
| Mega Drive / Genesis | BlastEm | GPLv3 |
| PlayStation | PCSX ReARMed | GPLv2 |
| PlayStation 2 (beta) | Play! | MIT/BSD |
| PSP | PPSSPP | GPLv2 |
| Arcade | MAME 2016 | GPLv2+ |
| Atari 2600 / 7800 / Lynx | Stella / ProSystem / Handy | GPLv2 / GPLv2 / Zlib |
| PC Engine, Neo Geo Pocket, WonderSwan | Mednafen (Beetle) | GPLv2 |
| DOS | DOSBox Pure | GPLv2 |

Removed from upstream because their licenses forbid commercial use: Snes9x, Genesis Plus GX,
FBNeo, MAME 2003-Plus (and with them Sega CD support).

## Game folders

Disc-based systems are detected from the folder name, so keep games in folders such as
`psx/`, `ps2/`, `psp/` and `arcade/`. PS2 DVD images are also recognised automatically.
PS2 emulation needs a recent 64-bit phone; Play! uses a high-level BIOS so no BIOS file is needed.

## Building

```bash
./extra-cores/fetch-cores.sh          # bsnes, BlastEm, Gearsystem, MAME 2016, Play!
git submodule update --init           # upstream lemuroid-cores
./gradlew :lemuroid-app:assembleFreeBundleDebug
```

The APK targets `arm64-v8a` only.

## Testing on a device

`tools/device-test/` holds the scripts used to smoke-test every system on a USB-connected Solana
Seeker (launch each game, screenshot, check for crashes). See its README.

## Website and legal pages

Terms of use, privacy policy and copyright information are published at
https://emulator.2cstudio.app. The terms shown there are generated from the same string
resources as the in-app terms.

## Trademarks

"2C Emulator", "2C Studio" and their logos and icons are trademarks of 2C Studio. The GPL covers
the source code only and grants no rights to these names or marks. If you distribute a modified
version, give it a different name and icon, and do not present it as made or endorsed by
2C Studio.

2C Emulator is not affiliated with, endorsed or sponsored by Nintendo, Sony, Sega, Atari, SNK,
Bandai or any other console maker. All other trademarks belong to their respective owners and are
mentioned only to describe compatibility.
