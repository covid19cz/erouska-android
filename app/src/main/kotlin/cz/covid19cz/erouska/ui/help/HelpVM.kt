package cz.covid19cz.erouska.ui.help

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.Category
import cz.covid19cz.erouska.ui.helpcategory.HelpCategoryFragmentArgs
import cz.covid19cz.erouska.utils.L
import java.lang.reflect.Type

class HelpVM @ViewModelInject constructor() : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_help_category
        }
    }

    var items = ObservableArrayList<Category>()

    fun fillInHelp() {
        val categoryType: Type = object : TypeToken<ArrayList<Category>>() {}.type
        val structuredQs: ArrayList<Category> = Gson().fromJson(AppConfig.helpJson, categoryType)
        items.addAll(structuredQs)
    }

    fun onSearchTapped() = navigate(R.id.nav_help_search)

    fun onItemClicked(category: Category) {
        L.i("clicked category $category")
        navigate(R.id.nav_help_category, HelpCategoryFragmentArgs(category = category).toBundle())
    }

}
