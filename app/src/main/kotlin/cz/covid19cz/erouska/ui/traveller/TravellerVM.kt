package cz.covid19cz.erouska.ui.traveller

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.timestampToDate
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.symptomdate.event.DatePickerEvent
import java.util.*

class TravellerVM @ViewModelInject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    fun next(traveller : Boolean){
        prefs.setTraveller(traveller)
        navigate(TravellerFragmentDirections.actionNavTravellerToEfgsAgreementFragment())
    }


}