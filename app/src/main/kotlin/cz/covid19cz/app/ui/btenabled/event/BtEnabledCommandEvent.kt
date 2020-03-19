package cz.covid19cz.app.ui.btenabled.event

import arch.event.LiveEvent

class BtEnabledCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        GO_BACK,
    }

}