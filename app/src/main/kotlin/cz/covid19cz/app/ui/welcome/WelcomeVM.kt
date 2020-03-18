package cz.covid19cz.app.ui.welcome

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent

class WelcomeVM : BaseVM() {

    fun verifyApp() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.VERIFY_APP))
    }

    fun help() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.HELP))
    }
}