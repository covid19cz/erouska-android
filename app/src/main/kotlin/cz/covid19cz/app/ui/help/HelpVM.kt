package cz.covid19cz.app.ui.help

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.help.event.HelpCommandEvent

class HelpVM : BaseVM() {

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

}