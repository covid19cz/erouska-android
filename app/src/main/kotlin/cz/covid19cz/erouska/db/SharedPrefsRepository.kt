package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import cz.covid19cz.erouska.ext.toIntList

class SharedPrefsRepository(c: Context) {

    companion object {
        const val APP_PAUSED = "preference.app_paused"
        const val ATTENUATION_THRESHOLD = "ATTENUATION_THRESHOLD"
        const val TRANSMISSION_RISK_SCORE = "TRANSMISSION_RISK_SCORE"
        const val MINIMUM_RISK_SCORE = "MINIMUM_RISK_SCORE"
        const val DURATION_SCORE = "DURATION_SCORE"
        const val DAYS_SINCE_LAST_EXPOSURE_SCORE = "DAYS_SINCE_LAST_EXPOSURE_SCORE"
        const val ATTENUATION_SCORE = "ATTENUATION_SCORES"
        const val DEFAULT_ATTENUATION_THRESHOLD = "33,63"
        const val DEFAULT_TRANSMISSION_RISK_SCORE = "1,1,1,1,1,1,1,1"
        const val DEFAULT_DURATION_SCORE = "0,1,2,3,4,5,6,7"
        const val DEFAULT_DAYS_SINCE_LAST_EXPOSURE_SCORE = "1,1,1,1,1,1,1,1"
        const val DEFAULT_ATTENUATION_SCORE = "0,1,2,2,8,8,8,8"
        const val DEFAULT_MINIMUM_RISK_SCORE = 1
    }

    val prefs: SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)

    var minimumRiskScore: Int
        get() {
            return prefs.getInt(
                MINIMUM_RISK_SCORE,
                DEFAULT_MINIMUM_RISK_SCORE
            )
        }
        set(value) {
            prefs.edit().putInt(
                MINIMUM_RISK_SCORE,
                value
            ).apply()
        }

    var attenuationThreshold: List<Int>
        get() {
            return prefs.getString(
                ATTENUATION_THRESHOLD,
                DEFAULT_ATTENUATION_THRESHOLD
            )!!.toIntList()
        }
        set(value) {
            prefs.edit().putString(
                ATTENUATION_THRESHOLD,
                value.joinToString(",")
            ).apply()
        }

    var attenuationScore: List<Int>
        get() {
            return prefs.getString(
                ATTENUATION_SCORE,
                DEFAULT_ATTENUATION_SCORE
            )!!.toIntList()
        }
        set(value) {
            prefs.edit().putString(
                ATTENUATION_SCORE,
                value.joinToString(",")
            ).apply()
        }

    var durationScore: List<Int>
        get() {
            return prefs.getString(
                DURATION_SCORE,
                DEFAULT_DURATION_SCORE
            )!!.toIntList()
        }
        set(value) {
            prefs.edit().putString(
                DURATION_SCORE,
                value.joinToString(",")
            ).apply()
        }

    var transmissionScore: List<Int>
        get() {
            return prefs.getString(
                TRANSMISSION_RISK_SCORE,
                DEFAULT_TRANSMISSION_RISK_SCORE
            )!!.toIntList()
        }
        set(value) {
            prefs.edit().putString(
                TRANSMISSION_RISK_SCORE,
                value.joinToString(",")
            ).apply()
        }

    var daysSinceLastExposureScore: List<Int>
        get() {
            return prefs.getString(
                DAYS_SINCE_LAST_EXPOSURE_SCORE,
                DEFAULT_DAYS_SINCE_LAST_EXPOSURE_SCORE
            )!!.toIntList()
        }
        set(value) {
            prefs.edit().putString(
                DAYS_SINCE_LAST_EXPOSURE_SCORE,
                value.joinToString(",")
            ).apply()
        }

    fun setAppPaused(appPaused: Boolean) {
        prefs.edit().putBoolean(APP_PAUSED, appPaused).apply()
    }

    fun getAppPaused() = prefs.getBoolean(APP_PAUSED, false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}