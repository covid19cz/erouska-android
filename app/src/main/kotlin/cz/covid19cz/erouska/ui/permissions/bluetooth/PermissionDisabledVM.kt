package cz.covid19cz.erouska.ui.permissions.bluetooth

import android.app.Application
import android.bluetooth.BluetoothManager
import android.location.LocationManager
import androidx.annotation.StringRes
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ext.isBluetoothEnabled
import cz.covid19cz.erouska.ext.isLocationProvided
import cz.covid19cz.erouska.ui.permissions.BasePermissionsVM
import cz.covid19cz.erouska.ui.permissions.bluetooth.event.PermissionsEvent

class PermissionDisabledVM(
    private val bluetoothManager: BluetoothManager,
    private val locationManager: LocationManager,
    app: Application
) : BasePermissionsVM(bluetoothManager, app) {

    val state = SafeMutableLiveData(ScreenState.BT_DISABLED)

    fun initViewModel() {
        val bluetoothDisabled = !bluetoothManager.isBluetoothEnabled()
        val locationDisabled = !locationManager.isLocationProvided()

        state.value = when {
            bluetoothDisabled && locationDisabled -> ScreenState.LOCATION_BT_DISABLED
            locationDisabled -> ScreenState.LOCATION_DISABLED
            bluetoothDisabled -> ScreenState.BT_DISABLED
            else -> ScreenState.ALL_ENABLED
        }

        if (state.value == ScreenState.ALL_ENABLED) {
            navigate(R.id.action_nav_bt_disabled_to_nav_dashboard)
        }
    }

    override fun goToNextScreen() {
        navigate(R.id.action_nav_bt_disabled_to_nav_dashboard)
    }

    fun enableLocation() {
        publish(PermissionsEvent(PermissionsEvent.Command.ENABLE_LOCATION))
    }

    fun enableLocationBT() {
        publish(PermissionsEvent(PermissionsEvent.Command.ENABLE_BT_LOCATION))
    }

    enum class ScreenState {
        BT_DISABLED, ALL_ENABLED, EN_API_DISABLED, LOCATION_DISABLED, LOCATION_BT_DISABLED
    }
}