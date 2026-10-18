# LibretroDroid (vendored)

Source of [LibretroDroid](https://github.com/Swordfish90/LibretroDroid) 0.14.0 (commit 8835c30),
licensed GPLv3 (see LICENSE). `oboe` and `libretro-common` are git submodules pinned to the
commits LibretroDroid 0.14.0 uses.

2C Emulator changes on top of upstream:

- Audio: fast-forward above ~4x overflowed the temporary audio buffer. Audio is now muted while
  fast-forwarding faster than it can be resampled, so any speed (up to 1000x) is safe.
- Fast-forward: each frame runs the core at most for a 20 ms budget, so high speeds act as a
  maximum instead of blocking the GL thread for seconds (which caused ANRs when opening the menu).
- Video: an aspect ratio of 0 from the core now means "use base width / height", as the libretro
  API specifies (Gearsystem reports 0 for 1:1 PAR, which rendered a black screen).
- Build: added missing `#include <functional>` so it compiles with NDK 27.
