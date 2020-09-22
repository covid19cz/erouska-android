package cz.covid19cz.erouska.ui.permissions

import android.content.Context
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.permissions.onboarding.event.PermissionsOnboarding
import cz.covid19cz.erouska.utils.DeviceUtils

abstract class BasePermissionsVM(
    protected val deviceUtils: DeviceUtils
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
        if (deviceUtils.isBtEnabled() && checkExposureNotificationState()) {
            goToNextScreen()
        }
    }

    private fun checkExposureNotificationState(): Boolean {
        return false
    }

    abstract fun goToNextScreen()
}