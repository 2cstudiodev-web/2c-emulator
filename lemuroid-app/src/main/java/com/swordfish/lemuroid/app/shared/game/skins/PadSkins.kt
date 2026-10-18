package com.swordfish.lemuroid.app.shared.game.skins

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.swordfish.lemuroid.R
import com.swordfish.touchinput.radial.PadPalette

enum class PadSkinSeries(
    @StringRes val titleId: Int,
) {
    ESSENTIALS(R.string.pad_skin_series_essentials),
    RETRO(R.string.pad_skin_series_retro),
    NEON(R.string.pad_skin_series_neon),
    ONCHAIN(R.string.pad_skin_series_onchain),
    GENESIS(R.string.pad_skin_series_genesis),
}

/** A controller skin. Names are generic on purpose: no console, game or company trademarks. */
data class PadSkin(
    val id: String,
    val name: String,
    val series: PadSkinSeries,
    val palette: PadPalette,
)

object PadSkins {
    const val DEFAULT_ID = "glass"

    private fun c(hex: Long) = Color(hex)

    /**
     * Builds a palette the same way the original theme is built: pressed buttons swap to the icon
     * color and pressed icons to the button color, so every skin keeps readable contrast.
     */
    private fun palette(
        buttons: List<Color>,
        icon: Color,
        tint: Color,
        buttonAlpha: Float = 0.85f,
        baseAlpha: Float = 0.14f,
        ringAlpha: Float = 0.22f,
    ): PadPalette {
        val buttonColors = buttons.map { it.copy(alpha = buttonAlpha) }
        return PadPalette(
            base = tint.copy(alpha = baseAlpha),
            ring = tint.copy(alpha = ringAlpha),
            composite = tint.copy(alpha = ringAlpha + 0.06f),
            compositePressed = icon.copy(alpha = 0.25f),
            button = buttonColors,
            buttonPressed = icon.copy(alpha = 0.80f),
            icon = icon.copy(alpha = 0.85f),
            iconPressed = buttons.first().copy(alpha = 0.95f),
        )
    }

    private fun skin(
        id: String,
        name: String,
        series: PadSkinSeries,
        palette: PadPalette,
    ) = PadSkin(id, name, series, palette)

    val ALL: List<PadSkin> =
        listOf(
            // Essentials
            skin(DEFAULT_ID, "Glass", PadSkinSeries.ESSENTIALS, PadPalette.DEFAULT),
            skin("midnight", "Midnight", PadSkinSeries.ESSENTIALS, palette(listOf(c(0xFF3B4A6B)), c(0xFFDCE3F2), c(0xFF6C7FA8))),
            skin("ivory", "Ivory", PadSkinSeries.ESSENTIALS, palette(listOf(c(0xFFF4EFE6)), c(0xFF4A4238), c(0xFFF4EFE6))),
            skin("slate", "Slate", PadSkinSeries.ESSENTIALS, palette(listOf(c(0xFF5B6573)), c(0xFFF2F4F7), c(0xFF8A94A3))),
            skin("mint", "Mint", PadSkinSeries.ESSENTIALS, palette(listOf(c(0xFF9FE3C8)), c(0xFF1F4D3D), c(0xFF9FE3C8))),
            // Retro
            skin("handheld", "Handheld", PadSkinSeries.RETRO, palette(listOf(c(0xFFC8C4BC)), c(0xFF6E2335), c(0xFFC8C4BC))),
            skin("olive-lcd", "Olive LCD", PadSkinSeries.RETRO, palette(listOf(c(0xFF8BA05A)), c(0xFF27321A), c(0xFF8BA05A))),
            skin("desktop", "Beige Desktop", PadSkinSeries.RETRO, palette(listOf(c(0xFFD9CBAA)), c(0xFF5A4630), c(0xFFD9CBAA))),
            skin("cartridge", "Cartridge", PadSkinSeries.RETRO, palette(listOf(c(0xFF4A4A4A)), c(0xFFFF5252), c(0xFF8A8A8A))),
            skin("cabinet", "Cabinet", PadSkinSeries.RETRO, palette(listOf(c(0xFFFFC400)), c(0xFF1A1A1A), c(0xFFFFC400))),
            // Neon
            skin("hot-pink", "Hot Pink", PadSkinSeries.NEON, palette(listOf(c(0xFFFF3EA5)), c(0xFF22001A), c(0xFFFF3EA5))),
            skin("electric", "Electric", PadSkinSeries.NEON, palette(listOf(c(0xFF00B3FF)), c(0xFF001A26), c(0xFF00B3FF))),
            skin("acid", "Acid", PadSkinSeries.NEON, palette(listOf(c(0xFFB6FF00)), c(0xFF1A2600), c(0xFFB6FF00))),
            skin("sunset", "Sunset", PadSkinSeries.NEON, palette(listOf(c(0xFFFF8A00), c(0xFFFF2D7A)), c(0xFF2A0A12), c(0xFFFF5A3D))),
            skin("ultraviolet", "Ultraviolet", PadSkinSeries.NEON, palette(listOf(c(0xFF7B2FF7), c(0xFF2F6BFF)), c(0xFFEDE7FF), c(0xFF5B3DF5))),
            // On-chain
            skin("mainnet", "Mainnet", PadSkinSeries.ONCHAIN, palette(listOf(c(0xFF9945FF), c(0xFF14F195)), c(0xFF0B0B1A), c(0xFF9945FF))),
            skin("devnet", "Devnet", PadSkinSeries.ONCHAIN, palette(listOf(c(0xFFDC1FFF), c(0xFF00FFA3)), c(0xFF120018), c(0xFFDC1FFF))),
            skin("lamport", "Lamport", PadSkinSeries.ONCHAIN, palette(listOf(c(0xFF00C2B8), c(0xFF19FB9B)), c(0xFF002420), c(0xFF00C2B8))),
            skin("epoch", "Epoch", PadSkinSeries.ONCHAIN, palette(listOf(c(0xFF5236A8)), c(0xFFFFC857), c(0xFF8E73E6))),
            skin("validator", "Validator", PadSkinSeries.ONCHAIN, palette(listOf(c(0xFF27303A)), c(0xFF14F195), c(0xFF14F195), baseAlpha = 0.10f)),
            // Genesis
            skin("genesis-gold", "Genesis Gold", PadSkinSeries.GENESIS, palette(listOf(c(0xFFFFE082), c(0xFFFFA000)), c(0xFF2A1C00), c(0xFFFFB300))),
            skin("genesis-obsidian", "Obsidian", PadSkinSeries.GENESIS, palette(listOf(c(0xFF55555F), c(0xFF26262C)), c(0xFFE6E9F0), c(0xFF9A9AAA))),
            skin("genesis-pearl", "Pearl", PadSkinSeries.GENESIS, palette(listOf(c(0xFFFFFFFF), c(0xFFDCE3FF)), c(0xFF3B3F5C), c(0xFFEFF2FF))),
            skin("genesis-aurora", "Aurora", PadSkinSeries.GENESIS, palette(listOf(c(0xFF14F195), c(0xFF00C2FF), c(0xFF9945FF)), c(0xFF061018), c(0xFF00C2FF))),
            skin("genesis-ember", "Ember", PadSkinSeries.GENESIS, palette(listOf(c(0xFFFF3D00), c(0xFFFFB300)), c(0xFF260A00), c(0xFFFF6D00))),
        )

    fun find(id: String?): PadSkin = ALL.firstOrNull { it.id == id } ?: ALL.first()
}
