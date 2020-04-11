package cz.covid19cz.erouska.ui.dashboard

import androidx.lifecycle.Observer
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.bt.BluetoothRepository
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.utils.Auth
import cz.covid19cz.erouska.utils.formatPhoneNumber

class DashboardVM(
    val bluetoothRepository: BluetoothRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)
    val phoneNumber = Auth.getPhoneNumber().formatPhoneNumber()

    private val serviceObserver = Observer<Boolean> { isRunning ->
        if (!isRunning && !prefs.getAppPaused()) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_ON))
        } else {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.UPDATE_STATE))
        }
    }

    override fun onCleared() {
        serviceRunning.removeObserver(serviceObserver)
        super.onCleared()
    }

    fun init() {
        serviceRunning.observeForever(serviceObserver)
    }

    fun pause() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
    }

    fun start() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_ON))
    }
}
