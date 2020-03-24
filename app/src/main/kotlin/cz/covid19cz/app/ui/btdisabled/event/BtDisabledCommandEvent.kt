package cz.covid19cz.app.ui.btdisabled.event

import arch.event.LiveEvent

class BtDisabledCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        ENABLE_BT,
        REQUEST_LOCATION,
        ENABLED_LOCATION
    }

}