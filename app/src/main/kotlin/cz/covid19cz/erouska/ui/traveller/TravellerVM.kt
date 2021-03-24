package cz.covid19cz.erouska.ui.traveller

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM

class TravellerVM @ViewModelInject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    fun next(traveller : Boolean){
        prefs.setTraveller(traveller)
        navigate(TravellerFragmentDirections.actionNavTravellerToEfgsAgreementFragment())
    }


}