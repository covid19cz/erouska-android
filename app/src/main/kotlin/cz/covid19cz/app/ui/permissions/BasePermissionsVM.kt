package cz.covid19cz.app.ui.permissions

import android.app.Application
import android.bluetooth.BluetoothManager
import cz.covid19cz.app.ext.hasLocationPermission
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.permissions.onboarding.event.PermissionsOnboarding
import cz.covid19cz.app.utils.isBluetoothEnabled

abstract class BasePermissionsVM(
    private val bluetoothManager: BluetoothManager,
    private val app: Application
) : BaseVM() {

    fun onBluetoothEnabled() {
        if (app.hasLocationPermission()) {
            onLocationPermissionGranted()
        } else {
            publish(PermissionsOnboarding(PermissionsOnboarding.Command.REQUEST_LOCATION_PERMISSION))
        }
    }

    fun onLocationPermissionGranted() {
        if (!app.isLocationEnabled()) {
            publish(PermissionsOnboarding(PermissionsOnboarding.Command.ENABLE_LOCATION))
        } else {
            goToNextScreen()
        }
    }

    fun onLocationPermissionDenied() {
        publish(PermissionsOnboarding(PermissionsOnboarding.Command.PERMISSION_REQUIRED))
    }

    fun enableBluetooth() {
        publish(PermissionsOnboarding(PermissionsOnboarding.Command.ENABLE_BT))
    }

    fun checkState() {
        if (bluetoothManager.isBluetoothEnabled()
            && app.isLocationEnabled()
            && app.hasLocationPermission()
        ) {
            goToNextScreen()
        }
    }

    abstract fun goToNextScreen()
}