package cz.covid19cz.erouska.ui.exposure

import arch.adapter.RecyclerLayoutStrategy
import cz.covid19cz.erouska.R

object PreventionRecyclerLayoutStrategy : RecyclerLayoutStrategy {
    override fun getLayoutId(item: Any): Int = R.layout.item_prevention
}