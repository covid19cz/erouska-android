package cz.covid19cz.erouska.ui.exposure

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.exposure.entity.SymptomItem
import cz.covid19cz.erouska.ui.exposure.entity.SymptomsData
import cz.covid19cz.erouska.ui.exposure.event.SymptomsEvent
import java.lang.reflect.Type

class MainSymptomsVM : BaseVM() {

    val state = MutableLiveData<SymptomsEvent>()

    val items = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val gson = Gson()

        val symptoms: SymptomsData = gson.fromJson(AppConfig.symptomsContentJson, SymptomsData::class.java)
        items.clear()
        items.addAll(symptoms.items)

        state.value = SymptomsEvent.SymptomsDataLoaded(symptoms)
    }

}