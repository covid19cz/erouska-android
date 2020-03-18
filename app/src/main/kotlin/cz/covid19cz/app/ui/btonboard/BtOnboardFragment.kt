package cz.covid19cz.app.ui.btonboard

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBtDisabledBinding
import cz.covid19cz.app.databinding.FragmentHelpBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent
import cz.covid19cz.app.ui.btonboard.event.BtOnboardCommandEvent

class BtOnboardFragment :
    BaseFragment<FragmentBtDisabledBinding, BtOnboardVM>(R.layout.fragment_bt_onboard, BtOnboardVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(BtOnboardCommandEvent::class) {
            when (it.command) {
                BtOnboardCommandEvent.Command.ENABLE_BT -> enableBluetooth()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.bt_onboard_toolbar_title)
        enableUpInToolbar(true)
    }

    fun enableBluetooth() {
        requestEnableBt()
    }

    override fun onBluetoothEnabled() {
        navigate(R.id.action_nav_bt_onboard_to_nav_login)
    }
}