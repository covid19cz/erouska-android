package cz.covid19cz.erouska.ui.welcome

import android.app.Application
import android.bluetooth.BluetoothManager
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ext.hasLocationPermission
import cz.covid19cz.erouska.ext.isBluetoothEnabled
import cz.covid19cz.erouska.ext.isLocationEnabled
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.welcome.event.WelcomeCommandEvent

class WelcomeVM(private val app: Application,
                private val bluetoothManager: BluetoothManager
) : BaseVM() {

    fun nextStep() {
        if (needsPermisssions()) {
            publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.VERIFY_APP))
        } else {
            publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.OPEN_BT_ONBOARD))
        }
    }

    private fun needsPermisssions() =
        bluetoothManager.isBluetoothEnabled() && app.isLocationEnabled() && app.hasLocationPermission()

    fun help() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.HELP))
    }

    fun getProclamationUrl(): String {
        return AppConfig.proclamationLink
    }
}
