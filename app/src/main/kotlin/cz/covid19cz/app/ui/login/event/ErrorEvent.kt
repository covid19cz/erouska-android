package cz.covid19cz.app.ui.login.event

import arch.event.LiveEvent

class ErrorEvent(val command: Command) : LiveEvent() {

    enum class Command{
        ERROR_PHONE_NUMBER_INVALID_FORMAT
    }

}