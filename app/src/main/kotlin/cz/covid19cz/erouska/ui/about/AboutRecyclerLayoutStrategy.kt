package cz.covid19cz.erouska.ui.about

import arch.adapter.RecyclerLayoutStrategy
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.about.entity.AboutIntroItem
import cz.covid19cz.erouska.ui.about.entity.AboutProfileItem
import cz.covid19cz.erouska.ui.about.entity.AboutRoleItem

object AboutRecyclerLayoutStrategy : RecyclerLayoutStrategy{
    override fun getLayoutId(item: Any): Int {
        return when(item){
            is AboutIntroItem -> R.layout.item_about_intro
            is AboutRoleItem -> R.layout.item_about_row
            is AboutProfileItem -> R.layout.item_about
            else -> 0
        }
    }

}