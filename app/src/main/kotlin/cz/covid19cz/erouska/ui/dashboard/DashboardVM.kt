package cz.covid19cz.erouska.ui.dashboard

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardVM(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val exposureNotificationsServerRepository: ExposureServerRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)
    val lastUpdate = MutableLiveData<String>()

    init {

        if (!prefs.isActivated()) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.NOT_ACTIVATED))
        }

        // TODO Check last download time
        // If lastDownload - now > 48 h -> publish DashboardCommandEvent.Command.DATA_OBSOLETE

        // TODO Check last exposure
        // If last exposure occured in less than 14 days -> publish DashboardCommandEvent.Command.RECENT_EXPOSURE

        // TODO Check if EN API is off
        // If yes -> publish DashboardCommandEvent(DashboardCommandEvent.Command.EN_API_OFF)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (!prefs.isActivated()) return
        val formatter = SimpleDateFormat("d.M.yyyy H:mm", Locale.getDefault())
        val lastImportTimestamp = prefs.getLastKeyImport()
        if (lastImportTimestamp != 0L) {
            lastUpdate.value = formatter.format(Date(prefs.getLastKeyImport()))
        }

        viewModelScope.launch {
            kotlin.runCatching {
                if (!prefs.getENAutoRequested()) {
                    exposureNotificationsRepository.start()
                }
                val result = exposureNotificationsRepository.isEnabled()
                if (result && !exposureNotificationsServerRepository.isKeyDownloadScheduled()) {
                    exposureNotificationsServerRepository.scheduleKeyDownload()
                }
                return@runCatching result
            }.onSuccess { enabled ->
                L.d("Exposure Notifications enabled $enabled")
                serviceRunning.value = enabled
                if (enabled) {
                    checkForRiskyExposure()
                }
            }.onFailure {
                if (it is ApiException) {
                    prefs.setENAutoRequested()
                    publish(GmsApiErrorEvent(it.status))
                }
                L.e(it)
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.stop()
            }.onSuccess {
                serviceRunning.value = false
                exposureNotificationsServerRepository.unscheduleKeyDownload()
                L.d("Exposure Notifications stopped")
            }.onFailure {
                L.e(it)
            }
        }
    }


    fun start() {
        if (exposureNotificationsRepository.isBluetoothEnabled()) {
            viewModelScope.launch {
                kotlin.runCatching {
                    exposureNotificationsRepository.start()
                }.onSuccess {
                    serviceRunning.value = true
                    exposureNotificationsServerRepository.scheduleKeyDownload()
                    L.d("Exposure Notifications started")
                }.onFailure {
                    if (it is ApiException) {
                        publish(GmsApiErrorEvent(it.status))
                    }
                    L.e(it)
                }
            }
        } else {
            publish(BluetoothDisabledEvent())
        }
    }

    fun wasAppUpdated(): Boolean {
        return prefs.isUpdateFromLegacyVersion()
    }

    fun checkForRiskyExposure() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.getLastRiskyExposure()
            }.onSuccess {
                it?.let {
                    showExposure()
                }
            }.onFailure {
                L.e(it)
            }
        }
    }

    private fun showExposure() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.RECENT_EXPOSURE))
    }


    private fun showDataObsolete() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.DATA_OBSOLETE))
    }

    fun unregister() {
        prefs.saveEhrid(null)
    }
}
