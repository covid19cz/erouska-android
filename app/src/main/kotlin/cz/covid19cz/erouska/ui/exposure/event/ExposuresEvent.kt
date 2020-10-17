package cz.covid19cz.erouska.ui.exposure.event

import arch.event.LiveEvent

class ExposuresCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        NO_RECENT_EXPOSURES,
        RECENT_EXPOSURE
    }

}
