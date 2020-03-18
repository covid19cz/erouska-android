package cz.covid19cz.app.ui.login.event

import arch.event.LiveEvent

class LoginCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        ENABLE_BT,
    }

}