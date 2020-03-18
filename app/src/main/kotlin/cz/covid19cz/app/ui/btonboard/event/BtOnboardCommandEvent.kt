package cz.covid19cz.app.ui.btonboard.event

import arch.event.LiveEvent

class BtOnboardCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        ENABLE_BT,
    }

}