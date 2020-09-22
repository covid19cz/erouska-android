package cz.covid19cz.erouska.ui.activation

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

class ActivationNotificationsVM @ViewModelInject constructor(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val prefs : SharedPrefsRepository,
    @ApplicationContext private val context: Context
) : BaseVM() {

    fun enableNotifications() {
        if (context.isBtEnabled()) {
            viewModelScope.launch {
                kotlin.runCatching {
                    exposureNotificationsRepository.start()
                }.onSuccess {
                    publish(NotificationsVerifiedEvent)
                    prefs.setExposureNotificationsEnabled(true)
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