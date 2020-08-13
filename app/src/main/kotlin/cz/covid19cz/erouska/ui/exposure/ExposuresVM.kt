package cz.covid19cz.erouska.ui.exposure

import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent

class ExposuresVM : BaseArchViewModel() {

    fun checkExposures() {
        // TODO Check if there were any exposures in last 14 days
        // If yes -> Show RECENT_EXPOSURE
        // If not and there are some exposures in the past -> Show NO_RECENT_EXPOSURES
        // If there are NO exposures in the DB -> Show NO_EXPOSURES
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.RECENT_EXPOSURE))
    }

    fun debugRecentExp() {
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.RECENT_EXPOSURE))
    }

    fun debugNoExp() {
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.NO_EXPOSURES))
    }

    fun debugExp() {
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES))
    }

}