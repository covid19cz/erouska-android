package cz.covid19cz.erouska.ui.sandbox

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.sandbox.event.SnackbarEvent

class SandboxConfigVM @ViewModelInject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    val reportTypeWeights = SandboxConfigValues("reportTypeWeights", 6)
    val infectiousnessWeights = SandboxConfigValues("infectiousnessWeights", 3)
    val attenuationBucketThresholdDb = SandboxConfigValues("attenuationBucketThresholdDb", 3)
    val attenuationBucketWeights = SandboxConfigValues("attenuationBucketWeights", 4)
    val minimumWindowScore = SandboxConfigValues("minimumWindowScore", 1)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        load()
    }

    private fun load(){
        reportTypeWeights.setDoubleValues(prefs.getReportTypeWeights() ?: AppConfig.reportTypeWeights)
        infectiousnessWeights.setDoubleValues(prefs.getInfectiousnessWeights() ?: AppConfig.infectiousnessWeights)
        attenuationBucketThresholdDb.setIntValues(prefs.getAttenuationBucketThresholdDb() ?: AppConfig.attenuationBucketThresholdDb)
        attenuationBucketWeights.setDoubleValues(prefs.getAttenuationBucketWeights() ?: AppConfig.attenuationBucketWeights)
        minimumWindowScore.setDoubleValue(0, prefs.getMinimumWindowScore() ?: AppConfig.minimumWindowScore)
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
