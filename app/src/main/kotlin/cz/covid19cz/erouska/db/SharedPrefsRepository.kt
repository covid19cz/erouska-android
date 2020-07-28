package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPrefsRepository(c : Context) {

    companion object{
        const val APP_PAUSED = "preference.app_paused"
        const val ATTENUATION_THRESHOLD_1 = "ATTENUATION_THRESHOLD_1"
        const val ATTENUATION_THRESHOLD_2 = "ATTENUATION_THRESHOLD_2"
    }

    val prefs : SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)

    fun getAttenuationThreshold1(defaultThreshold: Int): Int {
        return prefs.getInt(
            ATTENUATION_THRESHOLD_1,
            defaultThreshold
        )
    }

    fun setAttenuationThreshold1(threshold: Int) {
        prefs.edit().putInt(
            ATTENUATION_THRESHOLD_1,
            threshold
        ).apply()
    }

    fun getAttenuationThreshold2(defaultThreshold: Int): Int {
        return prefs.getInt(
            ATTENUATION_THRESHOLD_2,
            defaultThreshold
        )
    }

    fun setAttenuationThreshold2(threshold: Int) {
        prefs.edit().putInt(
            ATTENUATION_THRESHOLD_2,
            threshold
        ).apply()
    }

    fun setAppPaused(appPaused: Boolean) {
        prefs.edit().putBoolean(APP_PAUSED, appPaused).apply()
    }

    fun getAppPaused() = prefs.getBoolean(APP_PAUSED, false)

    fun clear(){
        prefs.edit().clear().apply()
    }
}