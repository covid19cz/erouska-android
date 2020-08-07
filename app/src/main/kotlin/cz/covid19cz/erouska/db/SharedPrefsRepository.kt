package cz.covid19cz.erouska.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPrefsRepository(c: Context) {

    companion object {
        const val APP_PAUSED = "preference.app_paused"
    }

    val prefs: SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)

    fun setAppPaused(appPaused: Boolean) {
        prefs.edit().putBoolean(APP_PAUSED, appPaused).apply()
    }

    fun getAppPaused() = prefs.getBoolean(APP_PAUSED, false)

    fun hasAppPaused() = prefs.contains(APP_PAUSED)

    fun removeAppPaused() {
        prefs.edit().remove(APP_PAUSED).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}