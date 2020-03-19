package cz.covid19cz.app.ui.btenabled

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.btdisabled.event.BtDisabledCommandEvent
import cz.covid19cz.app.ui.btenabled.event.BtEnabledCommandEvent
import cz.covid19cz.app.ui.help.event.HelpCommandEvent
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent

class BtEnabledVM : BaseVM() {

    fun goBack() {
        publish(BtEnabledCommandEvent(BtEnabledCommandEvent.Command.GO_BACK))
    }

}