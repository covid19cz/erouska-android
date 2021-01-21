package cz.covid19cz.erouska.ui.help

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.AboutAppCategory
import cz.covid19cz.erouska.ui.help.data.Category
import cz.covid19cz.erouska.ui.help.data.FaqCategory
import cz.covid19cz.erouska.ui.help.data.HowItWorksCategory
import cz.covid19cz.erouska.ui.helpcategory.HelpCategoryFragmentArgs
import cz.covid19cz.erouska.utils.L
import java.lang.reflect.Type

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
        val categoryType: Type = object : TypeToken<ArrayList<FaqCategory>>() {}.type
        val structuredQs: ArrayList<FaqCategory> = Gson().fromJson(AppConfig.helpJson, categoryType)
        items.clear()
        items.add(HowItWorksCategory())
        items.addAll(structuredQs)
        items.add(AboutAppCategory())
        L.i("help filled in: $items")
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
                    R.id.nav_about
                )
            }

            is HowItWorksCategory -> {
                navigate(
                    R.id.nav_exposure_info
                )
            }
        }

    }

}
