package cz.covid19cz.app.ui.dashboard.event

import arch.event.LiveEvent

class DashboardCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        TURN_ON,
        TURN_OFF,
        PAUSE,
        RESUME,
        UPLOAD,
        SHOW_DATA,
        SHARE
    }

}