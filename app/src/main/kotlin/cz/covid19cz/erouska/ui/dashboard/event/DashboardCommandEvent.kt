package cz.covid19cz.erouska.ui.dashboard.event

import arch.event.LiveEvent

class DashboardCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        TURN_ON,
        TURN_OFF,
        DATA_OBSOLETE,
        RECENT_EXPOSURE,
        EN_API_OFF
    }
}
