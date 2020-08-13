package cz.covid19cz.erouska.ui.permissions.onboarding.event

import arch.event.LiveEvent

class PermissionsOnboarding(val command: Command) : LiveEvent() {

    enum class Command{
        ENABLE_BT,
        REQUEST_LOCATION_PERMISSION,
        ENABLE_LOCATION,
        PERMISSION_REQUIRED,
        ENABLE_EXPOSURE_NOTIFICATION
    }

}