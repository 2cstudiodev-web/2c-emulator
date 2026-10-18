package com.swordfish.touchinput.radial

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor

/**
 * Colors of the touch controls. [button] holds one color, or several for a gradient.
 * [DEFAULT] reproduces Lemuroid's original translucent glass look.
 */
@Immutable
data class PadPalette(
    val base: Color,
    val ring: Color,
    val composite: Color,
    val compositePressed: Color,
    val button: List<Color>,
    val buttonPressed: Color,
    val icon: Color,
    val iconPressed: Color,
    val shadow: Color = DefaultShadowColor,
) {
    fun buttonBrush(pressed: Boolean): Brush =
        when {
            pressed -> Brush.linearGradient(listOf(buttonPressed, buttonPressed))
            button.size == 1 -> Brush.linearGradient(listOf(button[0], button[0]))
            else -> Brush.linearGradient(button)
        }

    /** Multiplies every color's opacity, for the user's opacity setting. */
    fun withOpacity(factor: Float): PadPalette {
        val f = factor.coerceIn(0f, 1f)

        fun Color.scaled() = copy(alpha = alpha * f)

        return copy(
            base = base.scaled(),
            ring = ring.scaled(),
            composite = composite.scaled(),
            compositePressed = compositePressed.scaled(),
            button = button.map { it.scaled() },
            buttonPressed = buttonPressed.scaled(),
            icon = icon.scaled(),
            iconPressed = iconPressed.scaled(),
        )
    }

    /**
     * Caps opacity for controls drawn on top of the game picture (landscape overlay). The caps
     * match the original glass theme, so [DEFAULT] is unchanged while colored skins stay see-through.
     */
    fun forOverlay(): PadPalette {
        fun Color.capped(max: Float) = copy(alpha = minOf(alpha, max))

        return copy(
            ring = ring.capped(OVERLAY_MAX_RING),
            composite = composite.capped(OVERLAY_MAX_COMPOSITE),
            compositePressed = compositePressed.capped(OVERLAY_MAX_COMPOSITE),
            button = button.map { it.capped(OVERLAY_MAX_BUTTON) },
            buttonPressed = buttonPressed.capped(OVERLAY_MAX_BUTTON),
            icon = icon.capped(OVERLAY_MAX_ICON),
            iconPressed = iconPressed.capped(OVERLAY_MAX_ICON),
        )
    }

    companion object {
        private const val OVERLAY_MAX_RING = 0.12f
        private const val OVERLAY_MAX_COMPOSITE = 0.15f
        private const val OVERLAY_MAX_BUTTON = 0.50f
        private const val OVERLAY_MAX_ICON = 0.70f

        private fun gray(
            luminosity: Float,
            opacity: Float,
        ) = Color(luminosity, luminosity, luminosity, opacity)

        val DEFAULT =
            PadPalette(
                base = gray(1.0f, 0.05f),
                ring = gray(1.0f, 0.10f),
                composite = gray(1.0f, 0.125f),
                compositePressed = gray(0.0f, 0.125f),
                button = listOf(gray(1.0f, 0.50f)),
                buttonPressed = gray(0.0f, 0.50f),
                icon = gray(0.0f, 0.50f),
                iconPressed = gray(1.0f, 0.50f),
            )
    }
}
