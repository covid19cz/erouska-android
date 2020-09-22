package cz.covid19cz.erouska.ui.exposure

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.exposure.entity.SymptomItem
import cz.covid19cz.erouska.ui.exposure.entity.SymptomsData

class MainSymptomsVM @ViewModelInject constructor() : BaseVM() {

    val items = ObservableArrayList<SymptomItem>()
    val placeholderId: Int = R.drawable.ic_item_empty
    var title = ""

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val gson = Gson()

        val symptoms: SymptomsData = gson.fromJson(AppConfig.symptomsContentJson, SymptomsData::class.java)

        title = symptoms.title

        items.clear()
        items.addAll(symptoms.items)

    }

}