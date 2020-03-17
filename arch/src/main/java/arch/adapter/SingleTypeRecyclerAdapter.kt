package arch.adapter

import androidx.annotation.LayoutRes
import androidx.databinding.ObservableArrayList

import arch.viewmodel.BaseArchViewModel

/**
 * Handcrafted by Štěpán Šonský on 15.01.2018.
 */

class SingleTypeRecyclerAdapter<T> : BaseRecyclerAdapter<T> {

    @LayoutRes
    private var layoutId: Int = 0

    constructor(items: ObservableArrayList<T>, viewModel: BaseArchViewModel?, itemLaoyutId: Int) : super(items, viewModel) {
        this.layoutId = itemLaoyutId
    }

    constructor(items: ObservableArrayList<T>, itemLaoyutId: Int) : super(items) {
        this.layoutId = itemLaoyutId
    }

    override fun getLayoutId(itemType: Int): Int {
        return layoutId
    }
}
