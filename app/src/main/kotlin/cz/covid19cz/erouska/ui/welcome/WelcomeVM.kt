package cz.covid19cz.erouska.ui.welcome

import android.app.Application
import android.bluetooth.BluetoothManager
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.isBluetoothEnabled
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.welcome.event.WelcomeCommandEvent

class WelcomeVM(private val app: Application,
                private val prefs: SharedPrefsRepository,
                private val bluetoothManager: BluetoothManager
) : BaseVM() {

    fun nextStep() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.VERIFY_APP))
    }

    private fun needsPermisssions() = bluetoothManager.isBluetoothEnabled()

    fun help() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.HELP))
    }

    fun getProclamationUrl(): String {
        return AppConfig.proclamationLink
    }

    fun wasAppUpdated(): Boolean {
        return prefs.isUpdateFromLegacyVersion()
    }
}
