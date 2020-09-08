package cz.covid19cz.erouska.ui.permissions.location.event

import arch.event.LiveEvent

class LocationEvent(val command: Command) : LiveEvent() {
    enum class Command {
        ENABLE_LOCATION
    }
}