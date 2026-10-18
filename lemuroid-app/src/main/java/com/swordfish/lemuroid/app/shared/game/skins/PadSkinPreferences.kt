package com.swordfish.lemuroid.app.shared.game.skins

import android.content.Context
import androidx.core.content.edit
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

/** Stores the selected controller skin in the multi-process preferences (games run in ":game"). */
object PadSkinPreferences {
    private const val KEY = "pad_skin_id"

    /** Shared with the opacity slider, which writes through intPreferenceState. */
    const val OPACITY_KEY = "pad_opacity_percent"
    const val OPACITY_DEFAULT = 100
    const val OPACITY_MIN = 20

    fun opacityPercent(context: Context): Int {
        return SharedPreferencesHelper.getSharedPreferences(context)
            .getInt(OPACITY_KEY, OPACITY_DEFAULT)
            .coerceIn(OPACITY_MIN, OPACITY_DEFAULT)
    }

    fun selectedId(context: Context): String {
        return SharedPreferencesHelper.getSharedPreferences(context).getString(KEY, null)
            ?: PadSkins.DEFAULT_ID
    }

    fun select(
        context: Context,
        id: String,
    ) {
        SharedPreferencesHelper.getSharedPreferences(context).edit { putString(KEY, id) }
    }
}
