package cz.covid19cz.erouska.ui.permissions.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ext.isLocationEnabled
import cz.covid19cz.erouska.ui.permissions.BasePermissionsVM
import cz.covid19cz.erouska.ui.permissions.bluetooth.event.PermissionsEvent
import cz.covid19cz.erouska.utils.DeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext

class PermissionDisabledVM @ViewModelInject constructor(
    deviceUtils: DeviceUtils
) : BasePermissionsVM(deviceUtils) {

    val state = SafeMutableLiveData(ScreenState.BT_DISABLED)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
            val btDisabled = !deviceUtils.isBtEnabled()
            val locationDisabled = !deviceUtils.isLocationEnabled()

            if (btDisabled && locationDisabled) {
                state.value = ScreenState.LOCATION_BT_DISABLED
                return
            }

            if (btDisabled) {
                state.value = ScreenState.BT_DISABLED
            }

            if (locationDisabled) {
                state.value = ScreenState.LOCATION_DISABLED
            }

            if (!btDisabled && !locationDisabled) {
                navigate(R.id.action_nav_bt_disabled_to_nav_dashboard)
                return
            }
    }

    fun initViewModel() {
        val bluetoothDisabled = !deviceUtils.isBtEnabled()
        val locationDisabled = !deviceUtils.isLocationEnabled()

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