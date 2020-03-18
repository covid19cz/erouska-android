package cz.covid19cz.app.ui.btenabled

import android.os.Bundle
import android.view.View
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBtDisabledBinding
import cz.covid19cz.app.databinding.FragmentHelpBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent
import cz.covid19cz.app.ui.btenabled.event.BtEnabledCommandEvent

class BtEnabledFragment :
    BaseFragment<FragmentBtDisabledBinding, BtEnabledVM>(R.layout.fragment_bt_enabled, BtEnabledVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(BtEnabledCommandEvent::class) {
            when (it.command) {
                BtEnabledCommandEvent.Command.GO_BACK -> enableBluetooth()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.bt_enabled_toolbar_title)
        enableUpInToolbar(true)
    }

    fun enableBluetooth() {
        requestEnableBt()
    }

    override fun onBluetoothEnabled() {
        navController().navigateUp()
    }
}