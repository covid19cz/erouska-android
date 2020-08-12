package cz.covid19cz.erouska.ui.permissions.onboarding

import android.app.Application
import android.bluetooth.BluetoothManager
import cz.covid19cz.erouska.ui.permissions.BasePermissionsVM

class PermissionsOnboardingVM(bluetoothManager: BluetoothManager, app: Application) :
    BasePermissionsVM(bluetoothManager, app) {
    override fun goToNextScreen() {

    }
}