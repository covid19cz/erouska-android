package cz.covid19cz.app.ui.welcome.event

import arch.event.LiveEvent

class WelcomeCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        VERIFY_APP,
        HELP
    }

}