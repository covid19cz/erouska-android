package arch.adapter

import androidx.databinding.ObservableArrayList
import arch.viewmodel.BaseArchViewModel

class StrategyRecyclerAdapter(items: ObservableArrayList<Any>, var strategy: RecyclerLayoutStrategy, vm: BaseArchViewModel?) :
    BaseRecyclerAdapter<Any>(items, vm) {

    override fun getLayoutId(itemType: Int): Int {
        return itemType
    }

    override fun getItemViewType(position: Int): Int {
        return strategy.getLayoutId(getItem(position))
    }
}
