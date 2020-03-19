package cz.covid19cz.app.ui.btonboard

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.btonboard.event.BtOnboardCommandEvent

class BtOnboardVM : BaseVM() {

    fun enableBluetooth() {
        publish(BtOnboardCommandEvent(BtOnboardCommandEvent.Command.ENABLE_BT))
    }

}