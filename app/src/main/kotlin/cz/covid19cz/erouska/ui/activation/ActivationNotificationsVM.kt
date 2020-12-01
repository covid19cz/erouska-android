package cz.covid19cz.erouska.ui.activation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatus
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivationNotificationsVM @ViewModelInject constructor(
    private val exposureNotificationsRepository: ExposureNotificationsRepository
) : BaseVM() {

    fun enableNotifications() {
            viewModelScope.launch {
                runCatching {
                    if (!exposureNotificationsRepository.isEnabled()){
                        exposureNotificationsRepository.start()
                        L.d("Exposure Notifications started")
                    }
                }.onSuccess {
                    publish(NotificationsVerifiedEvent)
                }.onFailure {
                    publish(GmsApiErrorEvent(it))
                }
            }
        }
}