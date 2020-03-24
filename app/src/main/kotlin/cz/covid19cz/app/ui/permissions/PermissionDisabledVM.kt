package cz.covid19cz.app.ui.permissions

import android.app.Application
import android.bluetooth.BluetoothManager
import androidx.annotation.StringRes
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.R
import cz.covid19cz.app.ext.hasLocationPermission
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.utils.isBluetoothEnabled

class PermissionDisabledVM(
    private val bluetoothManager: BluetoothManager,
    private val app: Application
) : BasePermissionsVM(bluetoothManager, app) {

    val state = SafeMutableLiveData(ScreenState.BT_DISABLED)

    fun initViewModel() {
        val bluetoothDisabled = !bluetoothManager.isBluetoothEnabled()
        val locationDisabled = !app.isLocationEnabled() || !app.hasLocationPermission()

        state.value = when {
            bluetoothDisabled && locationDisabled -> ScreenState.BT_LOCATION_DISABLED
            bluetoothDisabled -> ScreenState.BT_DISABLED
            locationDisabled -> ScreenState.LOCATION_DISABLED
            else -> ScreenState.ALL_ENABLED
        }

        if (state.value == ScreenState.ALL_ENABLED) {
            navigate(R.id.nav_dashboard)
        }
    }

    override fun goToNextScreen() {
        navigate(R.id.nav_dashboard)
    }

    @StringRes fun getButtonTitle():  Int {
        return when (state.value) {
            ScreenState.BT_DISABLED -> R.string.enable_bluetooth_button
            ScreenState.LOCATION_DISABLED -> R.string.enable_location_button
            ScreenState.BT_LOCATION_DISABLED -> R.string.enable_bt_location_button
            ScreenState.ALL_ENABLED -> R.string.enable_bluetooth_button
        }
    }


    enum class ScreenState {
        BT_DISABLED, LOCATION_DISABLED, BT_LOCATION_DISABLED, ALL_ENABLED
    }
}