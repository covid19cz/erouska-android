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
import dagger.hilt.android.qualifiers.ApplicationContext

class PermissionDisabledVM @ViewModelInject constructor(
    @ApplicationContext private val context: Context
) : BasePermissionsVM(context) {

    val state = SafeMutableLiveData(ScreenState.BT_DISABLED)

    private val btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val btEnabled = this@PermissionDisabledVM.context.isBtEnabled()
            val locationEnabled = context.isLocationEnabled()

            when {
                !btEnabled && !locationEnabled -> state.value = ScreenState.LOCATION_BT_DISABLED
                !btEnabled -> state.value = ScreenState.BT_DISABLED
                !locationEnabled -> state.value = ScreenState.LOCATION_DISABLED
            }

            if (btEnabled && locationEnabled) {
                navigate(R.id.action_nav_bt_disabled_to_nav_dashboard)
                context?.unregisterReceiver(this)
                context?.unregisterReceiver(locationReceiver)
            }
        }
    }

    private val locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val btEnabled = this@PermissionDisabledVM.context.isBtEnabled()
            val locationEnabled = context.isLocationEnabled()

            when {
                !btEnabled && !locationEnabled -> state.value = ScreenState.LOCATION_BT_DISABLED
                !btEnabled -> state.value = ScreenState.BT_DISABLED
                !locationEnabled -> state.value = ScreenState.LOCATION_DISABLED
            }

            if (btEnabled && locationEnabled) {
                navigate(R.id.action_nav_bt_disabled_to_nav_dashboard)
                context?.unregisterReceiver(btReceiver)
                context?.unregisterReceiver(this)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
            val btDisabled = !context.isBtEnabled()
            val locationDisabled = !context.isLocationEnabled()

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


        context.registerReceiver(btReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context.registerReceiver(
            locationReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    fun initViewModel() {
        val bluetoothDisabled = !context.isBtEnabled()
        val locationDisabled = !context.isLocationEnabled()

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