package cz.covid19cz.erouska.ui.exposure.event

import arch.event.LiveEvent
import cz.covid19cz.erouska.ui.exposure.Exposure
import cz.covid19cz.erouska.ui.exposure.entity.PreventionData
import cz.covid19cz.erouska.ui.exposure.entity.SymptomsData

class ExposuresCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        NO_EXPOSURES,
        NO_RECENT_EXPOSURES,
        RECENT_EXPOSURE
    }

}

sealed class RecentExposuresEvent {
    data class ExposuresLoadedEvent(val exposures: List<Exposure>) : RecentExposuresEvent()
}

sealed class SymptomsEvent {
    data class SymptomsDataLoaded(val data: SymptomsData) : SymptomsEvent()
}

sealed class PreventionEvent {
    data class PreventionDataLoaded(val data: PreventionData) : PreventionEvent()
}