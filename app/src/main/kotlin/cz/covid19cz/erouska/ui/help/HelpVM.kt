package cz.covid19cz.erouska.ui.help

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent

class HelpVM @ViewModelInject constructor() : BaseVM() {

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun openChatBot() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.OPEN_CHATBOT))
    }
}