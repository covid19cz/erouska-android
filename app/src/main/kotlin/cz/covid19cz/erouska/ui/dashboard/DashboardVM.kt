package cz.covid19cz.erouska.ui.dashboard

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent

class DashboardVM(private val prefs: SharedPrefsRepository) : BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)

    init {
        // TODO Check last download time
        // If lastDownload - now > 48 h -> publish DashboardCommandEvent.Command.DATA_OBSOLETE

        // TODO Check if EN API is off
        // If yes -> publish DashboardCommandEvent(DashboardCommandEvent.Command.EN_API_OFF)

        publish(DashboardCommandEvent(DashboardCommandEvent.Command.EN_API_OFF))
    }

    fun pause() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
    }

    fun start() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_ON))
    }
}
