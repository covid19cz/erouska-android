package cz.covid19cz.erouska.ui.exposure

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import arch.viewmodel.BaseArchViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.exposure.entity.PreventionData
import cz.covid19cz.erouska.ui.exposure.entity.PreventionItem
import cz.covid19cz.erouska.ui.exposure.event.PreventionEvent
import java.lang.reflect.Type

class SpreadPreventionVM : BaseArchViewModel() {

    val state = MutableLiveData<PreventionEvent>()

    val items = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val gson = Gson()

        val preventions: PreventionData = gson.fromJson(AppConfig.preventionContentJson, PreventionData::class.java)
        items.clear()
        items.addAll(preventions.items)

        state.value = PreventionEvent.PreventionDataLoaded(preventions)
    }

}