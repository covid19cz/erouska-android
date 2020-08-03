package cz.covid19cz.erouska

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils

object AppConfigV2 {

    const val CSV_VERSION = 4
    const val FIREBASE_REGION = "europe-west1"

    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val attenuationThreshold1
        get() = firebaseRemoteConfig.getLong("attenuationThreshold1")
    val attenuationThreshold2
        get() = firebaseRemoteConfig.getLong("attenuationThreshold2")

    init {
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults_v2).addOnCompleteListener {
            print()
        }
    }

    fun fetchRemoteConfig() {
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                L.d("Config params updated: $updated")
                print()
            } else {
                L.e("Config params update failed")
                task.exception?.printStackTrace()
            }
        }
    }

    private fun print() {
        for (item in firebaseRemoteConfig.all) {
            L.d("${item.key}: ${item.value.asString()}")
        }
    }

    /**
     * It tries current supported language first.
     * If it's not found, it tries English.
     * If English is not found, it shows Czech.
     */
    private fun getLocalized(key: String): String {
        val currentLanguage = LocaleUtils.getSupportedLanguage()
        return if (currentLanguage == "cs") {
            firebaseRemoteConfig.getString(key)
        } else {
            val translatedValue = firebaseRemoteConfig.getString(key + "_" + currentLanguage)
            if (translatedValue.isEmpty()) {
                val englishTranslatedValue = firebaseRemoteConfig.getString(key + "_en")
                if (englishTranslatedValue.isEmpty()) {
                    firebaseRemoteConfig.getString(key)
                } else {
                    englishTranslatedValue
                }
            } else {
                translatedValue
            }
        }
    }
}