package com.swordfish.lemuroid.app.mobile.feature.terms

import android.content.Context
import androidx.core.content.edit
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

/** Tracks which version of the terms of use the user accepted. Bump [VERSION] when the text changes. */
object TermsOfUse {
    const val VERSION = 1

    private const val PREF_ACCEPTED_VERSION = "terms_of_use_accepted_version"

    fun isAccepted(context: Context): Boolean {
        val preferences = SharedPreferencesHelper.getSharedPreferences(context)
        return preferences.getInt(PREF_ACCEPTED_VERSION, 0) >= VERSION
    }

    fun accept(context: Context) {
        SharedPreferencesHelper.getSharedPreferences(context).edit {
            putInt(PREF_ACCEPTED_VERSION, VERSION)
        }
    }
}
