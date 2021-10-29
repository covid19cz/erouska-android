package cz.covid19cz.erouska.ui.traveller

import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TravellerVM @Inject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    fun next(traveller : Boolean){
        prefs.setTraveller(traveller)
        navigate(TravellerFragmentDirections.actionNavTravellerToEfgsAgreementFragment())
    }


}