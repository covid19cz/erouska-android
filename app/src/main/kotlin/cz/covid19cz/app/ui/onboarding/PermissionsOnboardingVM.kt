package cz.covid19cz.app.ui.onboarding

import android.app.Application
import android.bluetooth.BluetoothManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import cz.covid19cz.app.R
import cz.covid19cz.app.ext.hasLocationPermission
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.onboarding.event.PermissionsOnboarding
import cz.covid19cz.app.utils.isBluetoothEnabled

class PermissionsOnboardingVM(
    private val bluetoothManager: BluetoothManager,
    private val app: Application,
    private val firebaseAnalytics: FirebaseAnalytics
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
            navigateToLogin()
        }
    }

    fun onLocationPermissionDenied() {
        logPermissionDenied()
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
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        navigate(R.id.action_nav_bt_onboard_to_nav_login)
    }

    private fun logPermissionDenied() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "location_permission_denied")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }
}