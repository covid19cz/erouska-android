package cz.covid19cz.erouska.ui.help

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.about.AboutFragmentArgs
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.*
import cz.covid19cz.erouska.ui.helpcategory.HelpCategoryFragmentArgs

class HelpVM @ViewModelInject constructor() : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return when (item) {
                is FaqCategory -> R.layout.item_help_faq_category
                is AboutAppCategory -> R.layout.item_help_about_category
                else -> R.layout.item_help_how_category
            }
        }
    }

    var items = ObservableArrayList<Category>()

    fun fillInHelp() {
        items.clear()
        items.add(HowItWorksCategory())
        items.addAll(AppConfig.helpJson.toFaqCategories())
        items.add(AboutAppCategory())
    }

    fun onSearchTapped() = navigate(R.id.nav_help_search)

    fun onItemClicked(category: Category) {
        when (category) {
            is FaqCategory -> {
                navigate(
                    R.id.nav_help_category,
                    HelpCategoryFragmentArgs(category = category).toBundle()
                )
            }

            is AboutAppCategory -> {
                navigate(
                    R.id.nav_about,
                    AboutFragmentArgs(fullscreen = true).toBundle()
                )
            }

            is HowItWorksCategory -> {
                navigate(
                    R.id.nav_how_it_works
                )
            }
        }

    }

}
