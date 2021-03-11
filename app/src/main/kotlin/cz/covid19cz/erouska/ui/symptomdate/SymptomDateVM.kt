package cz.covid19cz.erouska.ui.symptomdate

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.timestampToDate
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.symptomdate.event.DatePickerEvent
import java.util.*

class SymptomDateVM @ViewModelInject constructor(val prefs : SharedPrefsRepository) : BaseVM() {

    val nextEnabled = SafeMutableLiveData(true)
    val hasSymptoms = SafeMutableLiveData(true)
    val symptomDate = MutableLiveData<Date>()
    val symptomDateString = MutableLiveData<String>()

    init {
        symptomDate.observeForever {
            if (it != null){
                symptomDateString.value = it.time.timestampToDate()
            } else {
                symptomDateString.value = null
            }
            handleNextEnabled()
        }
        hasSymptoms.observeForever {
            if (!it){
                symptomDate.value = null
                symptomDateString.value = null
            }
            handleNextEnabled()
        }
    }

    fun handleNextEnabled(){
        nextEnabled.value = !hasSymptoms.value || (hasSymptoms.value && symptomDate.value != null)
    }

    fun showDatePicker(){
        publish(DatePickerEvent(symptomDate.value))
    }

    fun next(){
        prefs.setSymptomDate(symptomDate.value?.time)
        navigate(SymptomDateFragmentDirections.actionNavSymptomDateToNavTraveller())
    }

}