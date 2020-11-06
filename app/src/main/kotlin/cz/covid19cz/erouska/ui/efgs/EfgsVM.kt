package cz.covid19cz.erouska.ui.efgs

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.efgs.event.EfgsCommandEvent

class EfgsVM @ViewModelInject constructor() : BaseVM() {

    val efgsState = SafeMutableLiveData(false)

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        efgsState.value = false // TODO : return true if EFGS is enabled
    }

    fun turnOnEfgs() {
        efgsState.value = true
        publish(EfgsCommandEvent(EfgsCommandEvent.Command.TURN_ON))
    }

    fun turnOffEfgs() {
        efgsState.value = false
        publish(EfgsCommandEvent(EfgsCommandEvent.Command.TURN_OFF))
    }

    fun efgsDays() = AppConfig.efgsDays

    fun efgsSupportedCountries() = AppConfig.efgsSupportedCountries

}