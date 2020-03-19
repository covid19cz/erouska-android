package cz.covid19cz.app.ui.welcome

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent

class WelcomeVM(val app: Application,
                val bluetoothRepository: BluetoothRepository,
                private val sharedPrefsRepository: SharedPrefsRepository
) : BaseVM() {

    val userInitialized
        get() = FirebaseAuth.getInstance().currentUser != null && sharedPrefsRepository.getDeviceBuid() != null

    fun nextStep() {
        if (bluetoothRepository.isBtEnabled()) {
            publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.VERIFY_APP))
        } else {
            publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.OPEN_BT_ONBOARD))
        }
    }

    fun help() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.HELP))
    }
}
