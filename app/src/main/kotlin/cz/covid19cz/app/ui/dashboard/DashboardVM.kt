package cz.covid19cz.app.ui.dashboard

import androidx.lifecycle.Observer
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.dashboard.event.DashboardCommandEvent

class DashboardVM(
    val bluetoothRepository: BluetoothRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)
    val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber?.formatPhoneNumber() ?: "?"

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

val PHONE_REGEX = Regex("""(\+\d{1,3})?\s*(\d{1,3})\s*(\d{1,3})\s*(\d{1,3})""")

private fun String.formatPhoneNumber(): String {
    val match = PHONE_REGEX.matchEntire(this) ?: return this
    return match.groupValues.drop(1).joinToString(" ")
}
