package com.swordfish.touchinput.radial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class LemuroidPadTheme(val palette: PadPalette = PadPalette.DEFAULT) {
    val foregroundPadding: Dp = 8.dp
    val padding: Dp = 4.dp

    val level3Shadow = palette.shadow.copy(0.05f)
    val level3ShadowWidth = 4.dp

    val level2Shadow = palette.shadow.copy(0.05f)
    val level2ShadowWidth = 4.dp

    val level1Fill = palette.ring
    val level1Shadow = palette.shadow.copy(0.10f)
    val level1ShadowWidth = 4.dp

    val level0CornerRadius = 0.dp
    val level0Fill = palette.base
    val level0Shadow = palette.shadow.copy(0.10f)
    val level0ShadowWidth = 2.dp

    fun compositeFill(pressed: Boolean): Color {
        return if (pressed) palette.compositePressed else palette.composite
    }

    fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) palette.buttonPressed else palette.button.first()
    }

    fun foregroundBrush(pressed: Boolean): Brush = palette.buttonBrush(pressed)

    fun icons(pressed: Boolean): Color {
        return if (pressed) palette.iconPressed else palette.icon
    }
}

val LocalLemuroidPadTheme =
    compositionLocalOf<LemuroidPadTheme> {
        error("LemuroidPadTheme is missing")
    }
