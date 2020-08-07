package cz.covid19cz.erouska.ui.exposure

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.viewmodel.BaseArchViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.exposure.entity.PreventionItem
import java.lang.reflect.Type

class SpreadPreventionVM : BaseArchViewModel() {

    val items = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<PreventionItem>?>() {}.type

        val preventions: ArrayList<PreventionItem> = gson.fromJson(AppConfig.preventionContentJson, type)
        items.clear()
        items.addAll(preventions)

    }

}