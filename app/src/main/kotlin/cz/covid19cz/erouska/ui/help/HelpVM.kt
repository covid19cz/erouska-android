package cz.covid19cz.erouska.ui.help

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.Category
import cz.covid19cz.erouska.ui.help.data.Question
import cz.covid19cz.erouska.ui.helpcategory.HelpCategoryFragmentArgs
import cz.covid19cz.erouska.utils.L

class HelpVM @ViewModelInject constructor() : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_help_category
        }
    }

    var items = ObservableArrayList<Category>()

    fun fillInHelp() = items.addAll(
        listOf(
            Category(
                type = Category.Type.FAQ,
                title = "Fungování eRoušky",
                subtitle = "sběr a vyhodnocení dat, význam upozornění",
                icon = "https://erouska.cz/img/symptoms/ic_temperature.png",
                questions = listOf(
                    Question(
                        "Jak eRouška zaznamenává a zpracovává data o setkáních uživatelů?",
                        "Chytrý telefon s aplikací eRouška zaznamená přes Bluetooth LE anonymní identifikátory (ID) z jiných zařízení s touto aplikací. Informaci o „setkání“ a jeho délce ukládá do své vnitřní paměti."
                    ),
                    Question(
                        "Jak eRouška vyhodnocuje rizikové setkání?",
                        AppConfig.helpMarkdown)
                )
            ),
            Category(
                type = Category.Type.FAQ,
                title = "Instalace a kompatibilita",
                subtitle = "podporované mobily a možnosti instalace",
                icon = "https://erouska.cz/img/symptoms/ic_temperature.png",
                questions = listOf(
                    Question(
                        "Odkud si můžu eRoušku bezpečně stáhnout a nainstalovat?",
                        "Aplikace eRouška je dostupná pouze v Obchodě Play pro Android a v App Store pro iPhone."
                    ),
                    Question(
                        "Jak eRouška vyhodnocuje rizikové setkání?",
                        "Epidemiologové stanovují rizikový kontakt jako setkání, které je ve vzdálenosti bližší než 2 metry po dobu alespoň 15 minut. Aplikace eRouška se to snaží co nejpřesněji změřit dostupnými technologiemi. Vzdálenost mezi uživateli, respektive jejich telefony, se odhaduje na základě síly signálu Bluetooth. Doba setkání se posuzuje podle měřicích oken – telefon v několikaminutových intervalech zjišťuje, zda jsou v okolí jiné telefony s eRouškou."
                    )
                )
            )
        )
    )

    fun onSearchTapped() = navigate(R.id.nav_help_search)

    fun onItemClicked(category: Category) {
        L.i("clicked category $category")
        navigate(R.id.nav_help_category, HelpCategoryFragmentArgs(category = category).toBundle())
    }

}
