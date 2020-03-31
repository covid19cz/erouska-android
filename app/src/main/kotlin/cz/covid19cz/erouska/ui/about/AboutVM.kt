package cz.covid19cz.erouska.ui.about

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.ui.about.entity.AboutProfileItem
import cz.covid19cz.erouska.ui.about.entity.AboutRoleItem
import cz.covid19cz.app.ui.base.UrlEvent
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.about.entity.AboutIntroItem
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import io.reactivex.Observable
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL

class AboutVM : BaseVM() {

    val items = ObservableArrayList<Any>()
    val aboutWebUrl = MutableLiveData<String>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        loadData()
    }

    fun loadData() {

        subscribe(Observable.just(AppConfig.aboutApi).map { url ->
            val urlConnection = URL(url).openConnection() as HttpURLConnection

            val stream: InputStream = BufferedInputStream(urlConnection.getInputStream())
            val reader = BufferedReader(InputStreamReader(stream))
            var line: String? = null

            val sb = StringBuilder()
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }

            val gson = Gson()
            val userListType: Type = object : TypeToken<ArrayList<AboutRoleItem>?>() {}.type

            val roles: ArrayList<AboutRoleItem> = gson.fromJson(sb.toString(), userListType)
            urlConnection.disconnect()

            return@map roles
        }, {
            L.e(it)
            //Show website if api fails
            aboutWebUrl.value = AppConfig.aboutWeb
        }, {
            //items.add(AboutIntroItem())
            items.addAll(it)
        }
        )
    }

    fun profileClick(item: AboutProfileItem) {
        publish(UrlEvent(item.linkedin))
    }
}