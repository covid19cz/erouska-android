package cz.covid19cz.erouska.ui.permissions

import android.content.Context
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.permissions.onboarding.event.PermissionsOnboarding

abstract class BasePermissionsVM(
    private val context: Context
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

    fun enableExposureNotification() {
        publish(PermissionsOnboarding(PermissionsOnboarding.Command.ENABLE_EXPOSURE_NOTIFICATION))
    }

    fun checkState() {
        if (context.isBtEnabled() && checkExposureNotificationState()) {
            goToNextScreen()
        }
    }

    private fun checkExposureNotificationState(): Boolean {
        return false
    }

    abstract fun goToNextScreen()
}