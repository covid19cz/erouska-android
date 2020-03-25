package cz.covid19cz.app.ui.welcome

import android.app.Application
import android.bluetooth.BluetoothManager
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.ext.hasLocationPermission
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent
import cz.covid19cz.app.utils.isBluetoothEnabled

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
        return AppConfig.proclamationDynamicLink
    }
}
