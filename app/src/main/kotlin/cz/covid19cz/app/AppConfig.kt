package cz.covid19cz.app

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import cz.covid19cz.app.utils.Log

object AppConfig {

    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val collectionSeconds
        get() = firebaseRemoteConfig.getLong("collectionSeconds")
    val waitingSeconds
        get() = firebaseRemoteConfig.getLong("waitingSeconds")
    val advertiseTxPower
        get() = firebaseRemoteConfig.getLong("advertiseTxPower").toInt()
    val advertiseMode
        get() = firebaseRemoteConfig.getLong("advertiseMode").toInt()
    val scanMode
        get() = firebaseRemoteConfig.getLong("scanMode").toInt()
    val smsTimeoutSeconds
        get() = firebaseRemoteConfig.getLong("smsTimeoutSeconds").toInt()

    init {
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings).addOnCompleteListener {
            print()
        }
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
            print()
        }
    }

    fun init(){
        // just init instance
    }

    private fun print(){
        for (item in firebaseRemoteConfig.all) {
            Log.d("${item.key}: ${item.value.asString()}")
        }
    }

}