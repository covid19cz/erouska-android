package cz.covid19cz.erouska.ui.about

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.about.entity.AboutIntroItem
import cz.covid19cz.erouska.ui.about.entity.AboutProfileItem
import cz.covid19cz.erouska.ui.about.entity.AboutRoleItem
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.base.UrlEvent
import java.lang.reflect.Type

class AboutVM : BaseVM() {

    val items = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val gson = Gson()
        val userListType: Type = object : TypeToken<ArrayList<AboutRoleItem>?>() {}.type

        val roles: ArrayList<AboutRoleItem> = gson.fromJson(AppConfig.aboutJson, userListType)
        items.clear()
        items.add(AboutIntroItem())
        items.addAll(roles)
    }

    fun profileClick(item: AboutProfileItem) {
        item.linkedin?.let {
            publish(UrlEvent(it))
        }
    }

    fun versionClick() : Boolean{
        navigate(R.id.nav_sandbox)
        return true
    }
}