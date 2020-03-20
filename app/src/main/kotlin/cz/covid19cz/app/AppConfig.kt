package cz.covid19cz.app

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import cz.covid19cz.app.utils.Log

object AppConfig {

    const val BLE_OUT_OF_RANGE_TIMEOUT = 30

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
                Log.d("Config params updated: $updated")
                print()
            } else {
                Log.e("Config params update failed")
            }
        }
    }

    private fun print() {
        for (item in firebaseRemoteConfig.all) {
            Log.d("${item.key}: ${item.value.asString()}")
        }
    }
}