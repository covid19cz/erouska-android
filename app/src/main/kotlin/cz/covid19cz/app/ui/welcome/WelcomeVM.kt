package cz.covid19cz.app.ui.welcome

import android.app.Application
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent
import cz.covid19cz.app.utils.boolean
import cz.covid19cz.app.utils.sharedPrefs
import cz.covid19cz.app.utils.string

class WelcomeVM(val app: Application, val bluetoothRepository: BluetoothRepository) : BaseVM() {

    var userSignedIn by app.sharedPrefs().boolean()

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