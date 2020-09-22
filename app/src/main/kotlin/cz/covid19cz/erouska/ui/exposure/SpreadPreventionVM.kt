package cz.covid19cz.erouska.ui.exposure

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.viewmodel.BaseArchViewModel
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.exposure.entity.PreventionData
import cz.covid19cz.erouska.ui.exposure.entity.PreventionItem

class SpreadPreventionVM @ViewModelInject constructor() : BaseArchViewModel() {

    val items = ObservableArrayList<PreventionItem>()

    val placeholderId: Int = R.drawable.ic_item_empty
    var title = ""

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val gson = Gson()

        val preventions: PreventionData = gson.fromJson(AppConfig.preventionContentJson, PreventionData::class.java)

        title = preventions.title

        items.clear()
        items.addAll(preventions.items)
    }

}