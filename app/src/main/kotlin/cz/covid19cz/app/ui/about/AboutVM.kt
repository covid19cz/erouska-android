package cz.covid19cz.app.ui.about

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.app.App
import cz.covid19cz.app.R
import cz.covid19cz.app.ui.about.entity.AboutIntroItem
import cz.covid19cz.app.ui.about.entity.AboutProfileItem
import cz.covid19cz.app.ui.about.entity.AboutRoleItem
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.base.UrlEvent

class AboutVM : BaseVM() {

    val items = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val managers =
            AboutRoleItem(R.string.about_managers)
        val appDdevelopers =
            AboutRoleItem(R.string.about_app_devs)
        val backendDevelopers =
            AboutRoleItem(R.string.about_backend_devs)
        val uxui =
            AboutRoleItem(R.string.about_ux_ui)

        managers.add(
            AboutProfileItem(
                "Vojtěch",
                "Komenda",
                R.drawable.photo_vojtech_komenda,
                "https://www.linkedin.com/in/vojtech-komenda-71365b15/"
            )
        )
        managers.add(
            AboutProfileItem(
                "Roman",
                "Pichlík",
                R.drawable.photo_roman_pichlik,
                "https://www.linkedin.com/in/romanpichlik/"
            )
        )

        appDdevelopers.add(
            AboutProfileItem(
                "Štěpán",
                "Šonský",
                R.drawable.photo_stepan_sonsky,
                "https://www.linkedin.com/in/stepansonsky/"
            )
        )
        appDdevelopers.add(
            AboutProfileItem(
                "David",
                "Vávra",
                R.drawable.photo_david_vavra,
                "https://www.linkedin.com/in/dvavra/"
            )
        )
        appDdevelopers.add(
            AboutProfileItem(
                "Tomáš",
                "Chlápek",
                R.drawable.photo_tomas_chlapek,
                "https://www.linkedin.com/in/tom%C3%A1%C5%A1-chl%C3%A1pek-63852266/"
            )
        )

        items.add(AboutIntroItem())
        items.add(managers)
        items.add(appDdevelopers)
        items.add(backendDevelopers)
        items.add(uxui)
    }

    fun profileClick(item: AboutProfileItem) {
        publish(UrlEvent(item.linkedin))
    }

    fun logoClick(url : String) {
        publish(UrlEvent(url))
    }
}