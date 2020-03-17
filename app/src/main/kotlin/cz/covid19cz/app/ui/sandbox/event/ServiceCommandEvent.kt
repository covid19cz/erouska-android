package cz.covid19cz.app.ui.sandbox.event

import arch.event.LiveEvent

class ServiceCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        TURN_ON,
        TURN_OFF
    }

}