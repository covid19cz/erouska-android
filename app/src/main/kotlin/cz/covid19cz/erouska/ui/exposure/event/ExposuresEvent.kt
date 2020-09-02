package cz.covid19cz.erouska.ui.exposure.event

import arch.event.LiveEvent
import cz.covid19cz.erouska.ui.exposure.Exposure

class ExposuresCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        NO_RECENT_EXPOSURES,
        RECENT_EXPOSURE
    }

}

sealed class RecentExposuresEvent {
    data class ExposuresLoadedEvent(val exposures: List<Exposure>) : RecentExposuresEvent()
}
