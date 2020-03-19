package cz.covid19cz.app.ui.btonboard

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent
import cz.covid19cz.app.ui.btonboard.event.BtOnboardCommandEvent
import cz.covid19cz.app.ui.help.event.HelpCommandEvent
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent

class BtOnboardVM : BaseVM() {

    fun enableBluetooth() {
        publish(BtOnboardCommandEvent(BtOnboardCommandEvent.Command.ENABLE_BT))
    }

}