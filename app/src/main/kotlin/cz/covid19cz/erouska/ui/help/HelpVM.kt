package cz.covid19cz.erouska.ui.help

import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent

class HelpVM : BaseVM() {

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun getTutorialUrl(): String {
        return AppConfig.tutorialLink
    }
}