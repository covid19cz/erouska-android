package cz.covid19cz.erouska.ui.symptomdate

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.timestampToDate
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.symptomdate.event.DatePickerEvent
import cz.covid19cz.erouska.ui.traveller.TravellerFragmentDirections
import java.util.*

class SymptomDateVM @ViewModelInject constructor(val prefs: SharedPrefsRepository) : BaseVM() {

    val hasSymptoms = SafeMutableLiveData(true)
    val symptomDate = MutableLiveData<Date>()
    val symptomDateString = MutableLiveData<String>()

    init {
        symptomDate.observeForever {
            setSymptomDateString(it?.time?.timestampToDate())
        }
        hasSymptoms.observeForever {
            if (!it) {
                clearSymptomFields()
            }
        }
    }

    private fun clearSymptomFields() {
        symptomDate.value = null
        setSymptomDateString(null)
    }

    private fun setSymptomDateString(symptomDate: String?) {
        symptomDateString.value = symptomDate
    }

    fun showDatePicker() {
        publish(DatePickerEvent(symptomDate.value))
    }

    fun next() {
        prefs.setSymptomDate(symptomDate.value?.time)
        if (!prefs.isTraveller()) {
            navigate(SymptomDateFragmentDirections.actionNavSymptomDateToNavTraveller())
        } else {
            navigate(TravellerFragmentDirections.actionNavTravellerToEfgsAgreementFragment())
        }
    }

}