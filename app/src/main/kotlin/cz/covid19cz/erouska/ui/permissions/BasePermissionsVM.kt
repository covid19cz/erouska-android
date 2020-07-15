package cz.covid19cz.erouska.ui.permissions

import android.app.Application
import android.bluetooth.BluetoothManager
import cz.covid19cz.erouska.ext.isBluetoothEnabled
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.permissions.onboarding.event.PermissionsOnboarding

abstract class BasePermissionsVM(
    private val bluetoothManager: BluetoothManager,
    private val app: Application
) : BaseVM() {

    fun onBluetoothEnabled() {
        onLocationPermissionGranted()
    }

    fun onLocationPermissionGranted() {
        goToNextScreen()
    }

    fun enableBluetooth() {
        publish(PermissionsOnboarding(PermissionsOnboarding.Command.ENABLE_BT))
    }

    fun checkState() {
        if (bluetoothManager.isBluetoothEnabled()) {
            goToNextScreen()
        }
    }

    abstract fun goToNextScreen()
}