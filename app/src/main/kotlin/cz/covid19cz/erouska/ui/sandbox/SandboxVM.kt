package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class SandboxVM(
    val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val serverRepository: ExposureServerRepository,
    val prefs : SharedPrefsRepository
) : BaseVM() {

    lateinit var config : ExposureConfiguration? = null
    val serviceRunning = SafeMutableLiveData(false)
    val attenuationThreshold = SandboxConfigValues("Duration at Attenuation Thresholds", 2)
    val attenuationScore = SandboxConfigValues("Attenuation Scores", 8)
    val durationScore = SandboxConfigValues("Duration Scores", 8)
    val minimumRiskScore = SandboxConfigValues("Minimum Risk Scores", 1)
    val transmissionScore = SandboxConfigValues("Transmission Scores", 8)
    val daysSinceLastExposureScore = SandboxConfigValues("Days Since Last Exposure Scores", 8)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        loadSettings()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.isEnabled()
            }.onSuccess {
                L.d("Exposure Notifications enabled $it")
                serviceRunning.value = it
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.stop()
                serviceRunning.value = false
            }.onSuccess {
                L.d("Exposure Notifications started")
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.start()
            }.onSuccess {
                serviceRunning.value = true
                L.d("Exposure Notifications started")
            }.onFailure {
                if (it is ApiException){
                    publish(GmsApiErrorEvent(it.status))
                }
                L.e(it)
            }
        }
    }

    fun loadSettings(){
        durationScore.setValues(prefs.durationScore)
        attenuationThreshold.setValues(prefs.attenuationThreshold)
        attenuationScore.setValues(prefs.attenuationScore)
        minimumRiskScore.setValues(listOf(prefs.minimumRiskScore))
        transmissionScore.setValues(prefs.transmissionScore)
        daysSinceLastExposureScore.setValues(prefs.daysSinceLastExposureScore)
        buildConfig()
    }

    fun downloadKeyExport() {
        viewModelScope.launch {
            val files = serverRepository.downloadKeyExport()
            L.d("files=$files")
        }
    }

    fun provideDiagnosisKeys(){
        viewModelScope.launch {
            runCatching {
                val files = serverRepository.downloadKeyExport()
                exposureNotificationsRepository.provideDiagnosisKeys(files, config!!, token)
            }
        }
    }

    fun saveConfig(){
        prefs.minimumRiskScore = minimumRiskScore.getIntValue(0)
        prefs.attenuationThreshold = attenuationThreshold.getIntValues()
        prefs.attenuationScore = attenuationScore.getIntValues()
        prefs.durationScore = durationScore.getIntValues()
        prefs.transmissionScore = transmissionScore.getIntValues()
        prefs.daysSinceLastExposureScore = daysSinceLastExposureScore.getIntValues()
        buildConfig()
    }

    private fun buildConfig(){
        config = ExposureConfiguration.ExposureConfigurationBuilder()
            .setMinimumRiskScore(minimumRiskScore.getIntValue(0))
            .setDurationAtAttenuationThresholds(*attenuationThreshold.getIntValues().toIntArray())
            .setAttenuationScores(*attenuationScore.getIntValues().toIntArray())
            .setDaysSinceLastExposureScores(*daysSinceLastExposureScore.getIntValues().toIntArray())
            .setTransmissionRiskScores(*transmissionScore.getIntValues().toIntArray())
            .setDurationScores(*durationScore.getIntValues().toIntArray())
            .build()
    }

    fun toggleExposureNotifications(){
        if (!serviceRunning.value){
            stop()
        } else {
            start()
        }
    }

}