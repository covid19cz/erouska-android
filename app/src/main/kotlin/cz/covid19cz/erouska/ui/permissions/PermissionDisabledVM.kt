package cz.covid19cz.erouska.ui.permissions

import android.app.Application
import android.bluetooth.BluetoothManager
import androidx.annotation.StringRes
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ext.isBluetoothEnabled

class PermissionDisabledVM(
    private val bluetoothManager: BluetoothManager,
    private val app: Application
) : BasePermissionsVM(bluetoothManager, app) {

    val state = SafeMutableLiveData(ScreenState.BT_DISABLED)

    fun initViewModel() {
        val bluetoothDisabled = !bluetoothManager.isBluetoothEnabled()

        state.value = when {
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