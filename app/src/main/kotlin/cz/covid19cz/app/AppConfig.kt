package cz.covid19cz.app

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import cz.covid19cz.app.utils.L

object AppConfig {

    const val CSV_VERSION = 3

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
    val advertiseRestartMinutes
        get() = firebaseRemoteConfig.getLong("advertiseRestartMinutes")
    val criticalExpositionRssi
        get() = firebaseRemoteConfig.getLong("criticalExpositionRssi").toInt()
    val criticalExpositionMinutes
        get() = firebaseRemoteConfig.getLong("criticalExpositionMinutes").toInt()
    val uploadWaitingMinutes
        get() = firebaseRemoteConfig.getLong("uploadWaitingMinutes").toInt()

    var overrideAdvertiseTxPower : Int? = null

    init {
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
            print()
        }
    }

    fun fetchRemoteConfig(){
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
                task ->
            if (task.isSuccessful) {
                val updated = task.result
                L.d("Config params updated: $updated")
                print()
            } else {
                L.e("Config params update failed")
            }
        }
    }

    private fun print() {
        for (item in firebaseRemoteConfig.all) {
            L.d("${item.key}: ${item.value.asString()}")
        }
    }
}