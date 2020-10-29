package cz.covid19cz.erouska.ui.activation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class ActivationNotificationsVM @ViewModelInject constructor(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val prefs: SharedPrefsRepository,
    private val deviceUtils: DeviceInfo
) : BaseVM() {

    fun enableNotifications() {
        if (deviceUtils.isBtEnabled()) {
            viewModelScope.launch {
                kotlin.runCatching {
                    exposureNotificationsRepository.start()
                }.onSuccess {
                    publish(NotificationsVerifiedEvent)
                    prefs.setExposureNotificationsEnabled(true)
                    L.d("Exposure Notifications started")
                }.onFailure {
                    publish(GmsApiErrorEvent(it))
                }
            }
        } else {
            publish(BluetoothDisabledEvent())
        }
    }

}