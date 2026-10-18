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
