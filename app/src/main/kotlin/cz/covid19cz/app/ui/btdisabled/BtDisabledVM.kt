package cz.covid19cz.app.ui.btdisabled

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent
import cz.covid19cz.app.ui.help.event.HelpCommandEvent
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent

class BtDisabledVM : BaseVM() {

    fun enableBluetooth() {
        publish(BtDisabledCommandEvent(BtDisabledCommandEvent.Command.ENABLE_BT))
    }

}