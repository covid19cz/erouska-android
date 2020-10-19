package cz.covid19cz.erouska.ui.dashboard.event

import arch.event.LiveEvent

class DashboardCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        TURN_ON,
        TURN_OFF,
        DATA_OBSOLETE,
        DATA_UP_TO_DATE,
        RECENT_EXPOSURE,
        NOT_ACTIVATED
    }

}
