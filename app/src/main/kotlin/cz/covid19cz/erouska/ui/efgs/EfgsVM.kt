package cz.covid19cz.erouska.ui.efgs

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EfgsVM @Inject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    val efgsState = SafeMutableLiveData(prefs.isTraveller())

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        efgsState.observeForever {
            prefs.setTraveller(it)
        }
    }

    fun efgsDays() = AppConfig.efgsDays

    fun efgsSupportedCountries() = AppConfig.efgsSupportedCountries

}