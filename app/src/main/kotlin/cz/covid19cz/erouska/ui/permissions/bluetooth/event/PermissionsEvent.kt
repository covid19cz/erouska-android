package cz.covid19cz.erouska.ui.permissions.bluetooth.event

import arch.event.LiveEvent

class PermissionsEvent(val command: Command) : LiveEvent() {

    enum class Command {
        ENABLE_BT,
        ENABLE_LOCATION,
        ENABLE_BT_LOCATION
    }
}