package cz.covid19cz.app.ui.btdisabled

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent

class BtDisabledVM : BaseVM() {

    fun enableBluetooth() {
        publish(BtDisabledCommandEvent(BtDisabledCommandEvent.Command.ENABLE_BT))
    }

}