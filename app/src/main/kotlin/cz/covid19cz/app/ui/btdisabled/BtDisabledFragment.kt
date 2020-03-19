package cz.covid19cz.app.ui.btdisabled

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBtDisabledBinding
import cz.covid19cz.app.databinding.FragmentHelpBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent

class BtDisabledFragment :
    BaseFragment<FragmentBtDisabledBinding, BtDisabledVM>(R.layout.fragment_bt_disabled, BtDisabledVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(BtDisabledCommandEvent::class) {
            when (it.command) {
                BtDisabledCommandEvent.Command.ENABLE_BT -> enableBluetooth()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.bt_disabled_toolbar_title)
        enableUpInToolbar(true)
    }

    fun enableBluetooth() {
        requestEnableBt()
    }

    override fun onBluetoothEnabled() {
        navigate(R.id.action_nav_bt_disabled_to_nav_bt_enabled, null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_bt_disabled,
                    true).build())
    }
}