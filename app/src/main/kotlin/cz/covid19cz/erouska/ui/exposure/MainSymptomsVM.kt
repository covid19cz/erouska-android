package cz.covid19cz.erouska.ui.exposure

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.exposure.entity.SymptomItem
import java.lang.reflect.Type

class MainSymptomsVM : BaseVM() {

    val items = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val gson = Gson()
        val symptomListType: Type = object : TypeToken<ArrayList<SymptomItem>?>() {}.type

        val symptoms: ArrayList<SymptomItem> = gson.fromJson(AppConfig.symptomsContentJson, symptomListType)
        items.clear()
        items.addAll(symptoms)
    }

}