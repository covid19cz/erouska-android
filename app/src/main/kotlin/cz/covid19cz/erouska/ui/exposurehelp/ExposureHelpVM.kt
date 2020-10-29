package cz.covid19cz.erouska.ui.exposurehelp

import androidx.databinding.ObservableArrayList
import arch.adapter.RecyclerLayoutStrategy
import com.google.gson.Gson
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.exposurehelp.entity.ExposureHelpData
import cz.covid19cz.erouska.ui.exposurehelp.entity.ExposureHelpItem
import cz.covid19cz.erouska.ui.exposurehelp.entity.ExposureHelpTitle
import java.lang.IllegalArgumentException

class ExposureHelpVM : BaseVM(){

    val layoutStrategy = object: RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return when(item){
                is ExposureHelpItem -> R.layout.item_exposure_help
                is ExposureHelpTitle -> R.layout.item_exposure_help_title
                else -> throw IllegalArgumentException("Missing layout mapping")
            }
        }
    }

    val items = ObservableArrayList<Any>()

    fun setData(jsonData : String, bottomTitle : Boolean = false){
        val data: ExposureHelpData = Gson().fromJson(jsonData, ExposureHelpData::class.java)
        items.clear()
        if (data.title != null && !bottomTitle){
            items.add(ExposureHelpTitle(data.title))
        }
        items.addAll(data.items)
        if (data.title != null && bottomTitle){
            items.add(ExposureHelpTitle(data.title))
        }
    }

}