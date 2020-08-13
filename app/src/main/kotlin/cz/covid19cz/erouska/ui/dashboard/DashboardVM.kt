package cz.covid19cz.erouska.ui.dashboard

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class DashboardVM(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)

    init {
        // TODO Check last download time
        // If lastDownload - now > 48 h -> publish DashboardCommandEvent.Command.DATA_OBSOLETE

        // TODO Check last exposure
        // If last exposure occured in less than 14 days -> publish DashboardCommandEvent.Command.RECENT_EXPOSURE

        // TODO Check if EN API is off
        // If yes -> publish DashboardCommandEvent(DashboardCommandEvent.Command.EN_API_OFF)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
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
            kotlin.runCatching {
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
        if (exposureNotificationsRepository.isBluetoothEnabled()) {
            viewModelScope.launch {
                kotlin.runCatching {
                    exposureNotificationsRepository.start()
                }.onSuccess {
                    serviceRunning.value = true
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

    fun debugRun() {
        serviceRunning.value = true
    }

    fun debugStop() {
        serviceRunning.value = false
    }

    fun debugData() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.DATA_OBSOLETE))
    }

    fun debugContact() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.RECENT_EXPOSURE))
    }
}
