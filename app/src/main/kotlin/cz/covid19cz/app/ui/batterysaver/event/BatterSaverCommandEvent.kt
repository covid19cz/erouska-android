package cz.covid19cz.app.ui.batterysaver.event

import arch.event.LiveEvent

class BatterSaverCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        DISABLE_BATTER_SAVER,
    }

}