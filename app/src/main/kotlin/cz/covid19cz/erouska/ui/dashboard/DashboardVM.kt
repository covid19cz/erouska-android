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
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class DashboardVM(private val exposureNotificationsRepository: ExposureNotificationsRepository, private val prefs: SharedPrefsRepository) : BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)

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
}
