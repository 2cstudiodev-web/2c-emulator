package com.swordfish.lemuroid.app.shared.game

/**
 * Speeds offered by the fast-forward slider. Fine steps at the low end, where they are noticeable,
 * and coarse steps up to 1000x. The core runs this many frames per displayed frame, so the speed
 * actually reached is capped by how fast the device can run the core.
 */
object FastForwardSpeeds {
    val SPEEDS = listOf(1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, 25, 30, 40, 50, 75, 100, 150, 200, 300, 500, 750, 1000)

    const val DEFAULT_INDEX = 1

    fun speedAt(index: Int): Int = SPEEDS[index.coerceIn(SPEEDS.indices)]
}
