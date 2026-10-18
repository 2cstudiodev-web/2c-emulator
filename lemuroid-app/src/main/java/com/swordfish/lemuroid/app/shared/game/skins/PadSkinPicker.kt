package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSlider
import com.swordfish.lemuroid.app.utils.android.settings.intPreferenceState
import com.swordfish.touchinput.radial.PadPalette

/** Grid of all controller skins grouped by series. Selecting one stores it immediately. */
@Composable
fun PadSkinPicker(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val selected = remember { mutableStateOf(PadSkinPreferences.selectedId(context)) }
    val opacity =
        intPreferenceState(
            key = PadSkinPreferences.OPACITY_KEY,
            default = PadSkinPreferences.OPACITY_DEFAULT,
        )

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        LemuroidSettingsSlider(
            enabled = true,
            state = opacity,
            steps = (PadSkinPreferences.OPACITY_DEFAULT - PadSkinPreferences.OPACITY_MIN) / 10 - 1,
            valueRange = PadSkinPreferences.OPACITY_MIN.toFloat()..PadSkinPreferences.OPACITY_DEFAULT.toFloat(),
            title = { Text(text = stringResource(R.string.pad_opacity_title)) },
            subtitle = { Text(text = stringResource(R.string.pad_opacity_value, opacity.value)) },
        )
        Text(
            text = stringResource(R.string.pad_opacity_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        PadSkins.ALL.groupBy { it.series }.forEach { (series, skins) ->
            Text(
                text = stringResource(series.titleId),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            skins.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { skin ->
                        PadSkinCard(
                            skin = skin,
                            opacityPercent = opacity.value,
                            isSelected = skin.id == selected.value,
                            onClick = {
                                PadSkinPreferences.select(context, skin.id)
                                selected.value = skin.id
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PadSkinCard(
    skin: PadSkin,
    opacityPercent: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(if (isSelected) 3.dp else 1.dp, borderColor),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1.8f)) {
            drawPadPreview(skin.palette.withOpacity(opacityPercent / 100f))
        }
        Text(
            text = if (isSelected) "✓ ${skin.name}" else skin.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

/** A small drawing of a d-pad and four face buttons, using the same colors the real pad uses. */
private fun DrawScope.drawPadPreview(palette: PadPalette) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF15161B), Color(0xFF0B0B0E))))
    drawRect(palette.base)

    val padRadius = size.height * 0.36f
    val leftCenter = Offset(size.width * 0.27f, size.height * 0.5f)
    val rightCenter = Offset(size.width * 0.73f, size.height * 0.5f)

    drawCircle(palette.ring, padRadius, leftCenter)
    drawCircle(palette.ring, padRadius, rightCenter)

    // D-pad: a plus made of two rounded bars.
    val arm = padRadius * 0.62f
    val thickness = padRadius * 0.42f
    val corner = CornerRadius(thickness * 0.3f)
    val button = palette.buttonBrush(false)
    drawRoundRect(
        brush = button,
        topLeft = Offset(leftCenter.x - arm, leftCenter.y - thickness / 2),
        size = Size(arm * 2, thickness),
        cornerRadius = corner,
    )
    drawRoundRect(
        brush = button,
        topLeft = Offset(leftCenter.x - thickness / 2, leftCenter.y - arm),
        size = Size(thickness, arm * 2),
        cornerRadius = corner,
    )
    drawCircle(palette.icon, thickness * 0.14f, leftCenter)

    // Face buttons in a diamond, one shown pressed.
    val distance = padRadius * 0.52f
    val buttonRadius = padRadius * 0.27f
    listOf(Offset(0f, -distance), Offset(distance, 0f), Offset(0f, distance), Offset(-distance, 0f))
        .forEachIndexed { index, offset ->
            val center = rightCenter + offset
            val pressed = index == 1
            drawCircle(palette.buttonBrush(pressed), buttonRadius, center)
            drawCircle(palette.icons(pressed), buttonRadius * 0.28f, center)
        }
}

private fun PadPalette.icons(pressed: Boolean) = if (pressed) iconPressed else icon
