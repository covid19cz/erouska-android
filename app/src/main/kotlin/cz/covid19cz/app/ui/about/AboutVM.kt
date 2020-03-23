package cz.covid19cz.app.ui.about

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
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
        val managers = AboutRoleItem(R.string.about_managers)
        val app = AboutRoleItem(R.string.about_app_devs)
        val backend = AboutRoleItem(R.string.about_backend_devs)
        val uxui = AboutRoleItem(R.string.about_ux_ui)
        val testers = AboutRoleItem(R.string.about_testers)

        managers.add(AboutProfileItem("Iniciativa", "Covid19CZ", R.drawable.photo_covid19cz, "https://covid19cz.cz"))
        managers.add(AboutProfileItem("Vojtěch", "Komenda", R.drawable.photo_vojtech_komenda, "https://www.linkedin.com/in/vojtech-komenda-71365b15/"))

        app.add(AboutProfileItem("Štěpán", "Šonský", R.drawable.photo_stepan_sonsky, "https://www.linkedin.com/in/stepansonsky/"))
        app.add(AboutProfileItem("David", "Vávra", R.drawable.photo_david_vavra, "https://www.linkedin.com/in/dvavra/"))
        app.add(AboutProfileItem("Adam", "Šimek", R.drawable.photo_adam_simek, "https://www.linkedin.com/in/simek-adam-67868546/"))
        app.add(AboutProfileItem("Tomáš", "Chlápek", R.drawable.photo_tomas_chlapek, "https://www.linkedin.com/in/tom%C3%A1%C5%A1-chl%C3%A1pek-63852266/"))

        backend.add(AboutProfileItem("Roman", "Pichlík", R.drawable.photo_roman_pichlik, "https://www.linkedin.com/in/romanpichlik/"))
        backend.add(AboutProfileItem("Jan", "Kolena", R.drawable.photo_jan_kolena, "https://www.linkedin.com/in/jenda/"))
        backend.add(AboutProfileItem("Jakub", "Beránek", R.drawable.photo_jakub_beranek, "https://www.linkedin.com/in/jakub-beranek/"))
        backend.add(AboutProfileItem("Tomáš", "Zvala", R.drawable.photo_tomas_zvala, "https://www.linkedin.com/in/foxik/"))
        backend.add(AboutProfileItem("Jan", "Petr", R.drawable.photo_jan_petr, "https://www.linkedin.com/in/janpetr1/"))
        backend.add(AboutProfileItem("Pavel", "Kryl", R.drawable.photo_pavel_kryl, "https://www.linkedin.com/in/pavelkryl"))
        backend.add(AboutProfileItem("Ondřej", "Mašek", R.drawable.photo_ondrej_masek, "https://www.linkedin.com/in/masek/"))
        backend.add(AboutProfileItem("Zdeněk", "Hák", R.drawable.photo_zdenek_hak, "https://www.linkedin.com/in/zdeněk-hák-8a9a2154/"))

        items.add(AboutIntroItem())
        items.add(managers)
        items.add(app)
        items.add(backend)
        items.add(uxui)
    }

    fun profileClick(item: AboutProfileItem) {
        publish(UrlEvent(item.linkedin))
    }
}