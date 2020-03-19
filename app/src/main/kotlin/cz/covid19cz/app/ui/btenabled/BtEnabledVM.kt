package cz.covid19cz.app.ui.btenabled

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.btenabled.event.BtEnabledCommandEvent

class BtEnabledVM : BaseVM() {

    fun goBack() {
        publish(BtEnabledCommandEvent(BtEnabledCommandEvent.Command.GO_BACK))
    }

}