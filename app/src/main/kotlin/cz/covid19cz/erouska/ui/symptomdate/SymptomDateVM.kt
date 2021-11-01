package cz.covid19cz.erouska.ui.symptomdate

import androidx.lifecycle.MutableLiveData
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.timestampToDate
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.symptomdate.event.DatePickerEvent
import cz.covid19cz.erouska.ui.symptomdate.event.SymptomDateCommandEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SymptomDateVM @Inject constructor(val prefs: SharedPrefsRepository) : BaseVM() {

    val hasSymptoms = SafeMutableLiveData(true)
    val symptomDate = MutableLiveData<Date?>()
    val symptomDateString = MutableLiveData<String?>()

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
        if (prefs.isTraveller()) {
            publish(SymptomDateCommandEvent(SymptomDateCommandEvent.Command.NAV_EFGS_AGREEMENT))
        } else {
            publish(SymptomDateCommandEvent(SymptomDateCommandEvent.Command.NAV_TRAVELLER))
        }
    }

}