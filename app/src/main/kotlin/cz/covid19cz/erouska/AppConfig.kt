package cz.covid19cz.erouska

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils

object AppConfig {

    const val CSV_VERSION = 4
    const val FIREBASE_REGION = "europe-west1"

    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val collectionSeconds
        get() = firebaseRemoteConfig.getLong("collectionSeconds")
    val waitingSeconds
        get() = firebaseRemoteConfig.getLong("waitingSeconds")
    val advertiseTxPower
        get() = overrideAdvertiseTxPower ?: firebaseRemoteConfig.getLong("advertiseTxPower").toInt()
    val advertiseMode
        get() = firebaseRemoteConfig.getLong("advertiseMode").toInt()
    val scanMode
        get() = firebaseRemoteConfig.getLong("scanMode").toInt()
    val smsTimeoutSeconds
        get() = firebaseRemoteConfig.getLong("smsTimeoutSeconds")
    val showVerifyLaterTimeoutSeconds
        get() = firebaseRemoteConfig.getLong("showVerifyLaterTimeoutSeconds")
    val smsErrorTimeoutSeconds
        get() = firebaseRemoteConfig.getLong("smsErrorTimeoutSeconds")
    val advertiseRestartMinutes
        get() = firebaseRemoteConfig.getLong("advertiseRestartMinutes")
    val criticalExpositionRssi
        get() = firebaseRemoteConfig.getLong("criticalExpositionRssi").toInt()
    val criticalExpositionMinutes
        get() = firebaseRemoteConfig.getLong("criticalExpositionMinutes").toInt()
    val uploadWaitingMinutes
        get() = firebaseRemoteConfig.getLong("uploadWaitingMinutes").toInt()
    val persistDataDays
        get() = firebaseRemoteConfig.getLong("persistDataDays").toInt()
    val shareAppDynamicLink
        get() = firebaseRemoteConfig.getString("shareAppDynamicLink")
    val faqLink
        get() = firebaseRemoteConfig.getString("faqLink")
    val importantLink
        get() = firebaseRemoteConfig.getString("importantLink")
    val proclamationLink
        get() = getLocalized("proclamationLink")
    val aboutJson
        get() = getLocalized("aboutJson")
    val termsAndConditionsLink
        get() = getLocalized("termsAndConditionsLink")
    val homepageLink
        get() = getLocalized("homepageLink")
    val chatBotLink
        get() = getLocalized("chatBotLink")
    val showBatteryOptimizationTutorial
        get() = firebaseRemoteConfig.getBoolean("showBatteryOptimizationTutorial")
    val allowVerifyLater
        get() = firebaseRemoteConfig.getBoolean("allowVerifyLater")
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
    val exposureBodyTop
        get() = firebaseRemoteConfig.getString("exposureBodyTop")
    val exposureBodyMid
        get() = firebaseRemoteConfig.getString("exposureBodyMid")
    val noExposureHeader
        get() = firebaseRemoteConfig.getString("noExposureHeader")
    val noExposureBody
        get() = firebaseRemoteConfig.getString("noExposureBody")
    val mainSymptoms
        get() = firebaseRemoteConfig.getString("mainSymptoms")
    val spreadPrevention
        get() = firebaseRemoteConfig.getString("spreadPrevention")
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
    val exposureNotificationContent
        get() = firebaseRemoteConfig.getString("exposureNotificationContent")
    val keyExportUrl
        get() = firebaseRemoteConfig.getString("keyExportUrl")

    var overrideAdvertiseTxPower: Int? = null

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