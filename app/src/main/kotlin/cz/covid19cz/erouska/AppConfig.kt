package cz.covid19cz.erouska

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils

object AppConfig {

    const val CSV_VERSION = 4
    const val FIREBASE_REGION = "europe-west1"

    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    // Exposure Notifications Settings
    val reportTypeWeights
        get() = firebaseRemoteConfig.getString("reportTypeWeights").split(";").map { it.toDouble() }
    val infectiousnessWeights
        get() = firebaseRemoteConfig.getString("infectiousnessWeights").split(";")
            .map { it.toDouble() }
    val attenuationBucketThresholdDb
        get() = firebaseRemoteConfig.getString("attenuationBucketThresholdDb").split(";")
            .map { it.toInt() }
    val attenuationBucketWeights
        get() = firebaseRemoteConfig.getString("attenuationBucketWeights").split(";")
            .map { it.toDouble() }
    val minimumWindowScore
        get() = firebaseRemoteConfig.getDouble("minimumWindowScore")

    val shareAppDynamicLink
        get() = firebaseRemoteConfig.getString("shareAppDynamicLink")
    val proclamationLink
        get() = getLocalized("proclamationLink")
    val aboutJson
        get() = getLocalized("aboutJson")
    val termsAndConditionsLink
        get() = getLocalized("termsAndConditionsLink")
    val chatBotLink
        get() = getLocalized("chatBotLink")
    val batteryOptimizationAsusMarkdown
        get() = getLocalized("batteryOptimizationAsusMarkdown")
    val batteryOptimizationLenovoMarkdown
        get() = getLocalized("batteryOptimizationLenovoMarkdown")
    val batteryOptimizationSamsungMarkdown
        get() = getLocalized("batteryOptimizationSamsungMarkdown")
    val batteryOptimizationSonyMarkdown
        get() = getLocalized("batteryOptimizationSonyMarkdown")
    val batteryOptimizationXiaomiMarkdown
        get() = getLocalized("batteryOptimizationXiaomiMarkdown")
    val batteryOptimizationHuaweiMarkdown
        get() = getLocalized("batteryOptimizationHuaweiMarkdown")
    val helpMarkdown
        get() = getLocalized("helpMarkdown")
    val dataCollectionMarkdown
        get() = getLocalized("dataCollectionMarkdown")
    val myDataText
        get() = getLocalized("myDataText")
    val minSupportedVersionCodeAndroid
        get() = firebaseRemoteConfig.getLong("minSupportedVersionCodeAndroid")
    val riskyEncountersTitle
        get() = firebaseRemoteConfig.getString("riskyEncountersTitle")
    val noEncounterHeader
        get() = firebaseRemoteConfig.getString("noEncounterHeader")
    val noEncounterBody
        get() = firebaseRemoteConfig.getString("noEncounterBody")
    val earlierExposures
        get() = firebaseRemoteConfig.getString("earlierExposures")
    val exposureUITitle
        get() = firebaseRemoteConfig.getString("exposureUITitle")
    val symptomsUITitle
        get() = firebaseRemoteConfig.getString("symptomsUITitle")
    val spreadPreventionUITitle
        get() = firebaseRemoteConfig.getString("spreadPreventionUITitle")
    val recentExposuresUITitle
        get() = firebaseRemoteConfig.getString("recentExposuresUITitle")
    val symptomsContentJson
        get() = getLocalized("symptomsContentJson")
    val preventionContentJson
        get() = getLocalized("preventionContentJson")
    val encounterWarning
        get() = firebaseRemoteConfig.getString("encounterWarning")
    val keyExportUrl
        get() = firebaseRemoteConfig.getString("keyExportUrl")
    val keyImportPeriodHours
        get() = firebaseRemoteConfig.getLong("keyImportPeriodHours")
    val keyImportDataOutdatedHours
        get() = firebaseRemoteConfig.getLong("keyImportDataOutdatedHours")
    val contactsContentJson
        get() = firebaseRemoteConfig.getString("contactsContentJson")
    val riskyEncountersWithSymptoms
        get() = firebaseRemoteConfig.getString("riskyEncountersWithSymptoms")
    val riskyEncountersWithoutSymptoms
        get() = firebaseRemoteConfig.getString("riskyEncountersWithoutSymptoms")
    val currentMeasuresUrl
        get() = firebaseRemoteConfig.getString("currentMeasuresUrl")

    init {
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
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