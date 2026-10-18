package com.swordfish.lemuroid.lib.library

import android.content.Context
import androidx.core.content.edit
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Lets 2C Studio withdraw a system (for example after a legal complaint) without shipping an update.
 *
 * Two sources are combined:
 * - systems removed from this build at compile time (`-PexcludedSystems=3ds,ps2`), and
 * - [CONFIG_URL], a small JSON file fetched at startup and cached, so a removal keeps working
 *   offline and a failed fetch never re-enables anything by accident.
 *
 * A disabled system is hidden from the library and its games cannot be launched. Nothing is deleted:
 * game files, saves and library entries stay, and come back if the system is enabled again.
 *
 * Config format: {"disabledSystems": ["3ds"], "message": {"en": "...", "zh": "..."}}
 */
object SystemAvailability {
    const val CONFIG_URL = "https://emulator.2cstudio.app/config.json"

    private const val PREF_DISABLED = "remote_disabled_systems"
    private const val PREF_MESSAGE_EN = "remote_disabled_message_en"
    private const val PREF_MESSAGE_ZH = "remote_disabled_message_zh"
    private const val TIMEOUT_MS = 8_000

    @Volatile
    private var buildDisabled: Set<String> = emptySet()

    @Volatile
    private var remoteDisabled: Set<String>? = null

    /** Call once per process before the library is shown. */
    fun init(
        context: Context,
        excludedAtBuildTime: Set<String>,
    ) {
        buildDisabled = excludedAtBuildTime
        remoteDisabled = readCached(context)
    }

    fun isDisabled(systemId: String): Boolean {
        return systemId in buildDisabled || systemId in (remoteDisabled ?: emptySet())
    }

    /** Message to show when a disabled game is opened, or null to use the app's default text. */
    fun disabledMessage(context: Context): String? {
        val preferences = SharedPreferencesHelper.getSharedPreferences(context)
        val chinese = Locale.getDefault().language == "zh"
        val key = if (chinese) PREF_MESSAGE_ZH else PREF_MESSAGE_EN
        return preferences.getString(key, null)?.takeIf { it.isNotBlank() }
    }

    /** Downloads the remote config and caches it. Network failures keep the previous state. */
    fun refresh(context: Context) {
        val body =
            runCatching { download(CONFIG_URL) }
                .onFailure { Timber.w(it, "Unable to fetch system availability config") }
                .getOrNull() ?: return

        runCatching {
            val json = JSONObject(body)
            val disabled = json.optJSONArray("disabledSystems")
            val systems = (0 until (disabled?.length() ?: 0)).map { disabled!!.getString(it) }.toSet()
            val message = json.optJSONObject("message")

            SharedPreferencesHelper.getSharedPreferences(context).edit {
                putStringSet(PREF_DISABLED, systems)
                putString(PREF_MESSAGE_EN, message?.optString("en"))
                putString(PREF_MESSAGE_ZH, message?.optString("zh"))
            }
            remoteDisabled = systems
            Timber.i("System availability updated, disabled: $systems")
        }.onFailure { Timber.w(it, "Invalid system availability config") }
    }

    private fun readCached(context: Context): Set<String> {
        return SharedPreferencesHelper.getSharedPreferences(context)
            .getStringSet(PREF_DISABLED, emptySet())
            ?.toSet()
            ?: emptySet()
    }

    private fun download(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.useCaches = false
        connection.setRequestProperty("Cache-Control", "no-cache")
        return try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
