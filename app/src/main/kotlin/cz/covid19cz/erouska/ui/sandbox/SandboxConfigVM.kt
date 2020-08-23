package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.sandbox.event.SnackbarEvent

class SandboxConfigVM(val prefs : SharedPrefsRepository) : BaseVM() {

    val reportTypeWeights = SandboxConfigValues("reportTypeWeights", 6)
    val infectiousnessWeights = SandboxConfigValues("infectiousnessWeights", 3)
    val attenuationBucketThresholdDb = SandboxConfigValues("attenuationBucketThresholdDb", 3)
    val attenuationBucketWeights = SandboxConfigValues("attenuationBucketWeights", 4)
    val minimumWindowScore = SandboxConfigValues("minimumWindowScore", 1)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        load()
    }

    fun load(){
        prefs.getReportTypeWeights().let {
            if (it != null){
                reportTypeWeights.setValues(it)
            } else {
                reportTypeWeights.setDoubleValues(AppConfig.reportTypeWeights)
            }
        }

        prefs.getInfectiousnessWeights().let {
            if (it != null){
                infectiousnessWeights.setValues(it)
            } else {
                infectiousnessWeights.setDoubleValues(AppConfig.infectiousnessWeights)
            }
        }

        prefs.getAttenuationBucketThresholdDb().let {
            if (it != null){
                attenuationBucketThresholdDb.setValues(it)
            } else {
                attenuationBucketThresholdDb.setIntValues(AppConfig.attenuationBucketThresholdDb)
            }
        }

        prefs.getAttenuationBucketWeights().let {
            if (it != null){
                attenuationBucketWeights.setValues(it)
            } else {
                attenuationBucketWeights.setDoubleValues(AppConfig.attenuationBucketWeights)
            }
        }

        prefs.getMinimumWindowScore().let {
            if (it != null){
                minimumWindowScore.setValues(it)
            } else {
                minimumWindowScore.setDoubleValue(0, AppConfig.minimumWindowScore)
            }
        }
    }

    fun save(){
        prefs.setReportTypeWeights(reportTypeWeights.joinToString())
        prefs.setInfectiousnessWeights(infectiousnessWeights.joinToString())
        prefs.setAttenuationBucketThresholdDb(attenuationBucketThresholdDb.joinToString())
        prefs.setAttenuationBucketWeights(attenuationBucketWeights.joinToString())
        prefs.setMinimumWindowScore(minimumWindowScore.stringValues[0].value!!)
        publish(SnackbarEvent("Config saved"))
    }

    fun useDefaults(){
        prefs.clearCustomConfig()
        load()
        publish(SnackbarEvent("Using remote config"))
    }



}
