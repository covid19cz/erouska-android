package cz.covid19cz.erouska.ui.efgs

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.efgs.event.EfgsCommandEvent

class EfgsVM @ViewModelInject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    val efgsState = SafeMutableLiveData(prefs.isTraveller())

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        efgsState.value = prefs.isTraveller()
    }

    fun turnOnEfgs() {
        efgsState.value = true
        prefs.setTraveller(true)
        publish(EfgsCommandEvent(EfgsCommandEvent.Command.TURN_ON))
    }

    fun turnOffEfgs() {
        efgsState.value = false
        prefs.setTraveller(false)
        publish(EfgsCommandEvent(EfgsCommandEvent.Command.TURN_OFF))
    }

    fun efgsDays() = AppConfig.efgsDays

    fun efgsSupportedCountries() = AppConfig.efgsSupportedCountries

}