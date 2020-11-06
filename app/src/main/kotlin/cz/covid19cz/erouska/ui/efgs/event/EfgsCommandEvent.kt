package cz.covid19cz.erouska.ui.efgs.event

import arch.event.LiveEvent

class EfgsCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        TURN_ON,
        TURN_OFF,
    }
}
