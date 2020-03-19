package arch.binding

import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arch.adapter.BaseRecyclerAdapter
import arch.adapter.RecyclerLayoutStrategy
import arch.adapter.SingleTypeRecyclerAdapter
import arch.adapter.StrategyRecyclerAdapter
import arch.viewmodel.BaseArchViewModel


/**
 * Created by Stepan on 23.11.2016.
 */

@BindingAdapter(value = ["viewModel", "items", "layoutId", "layoutStrategy", "orientation", "spanCount", "lifecycle", "parentItem"], requireAll = false)
fun <T> bindItems(
    view: RecyclerView,
    vm: BaseArchViewModel?,
    items: ObservableArrayList<T>,
    layoutId: Int?,
    layoutStrategy: RecyclerLayoutStrategy?,
    orientation: Int?,
    spanCount: Int?,
    lifecycleOwner: LifecycleOwner?,
    parentItem: Any?
) {
    if (view.adapter == null) {
        if (view.layoutManager == null) {
            if (spanCount == null) {
                view.layoutManager = object : LinearLayoutManager(
                    view.context, orientation
                        ?: RecyclerView.VERTICAL, false
                ) {
                    override fun supportsPredictiveItemAnimations(): Boolean {
                        return false
                    }
                }
            } else {
                view.layoutManager = object : GridLayoutManager(
                    view.context, spanCount) {
                    override fun supportsPredictiveItemAnimations(): Boolean {
                        return false
                    }
                }
            }
        }
        if (layoutStrategy == null) {
            if (layoutId != null) {
                view.adapter = SingleTypeRecyclerAdapter(items, vm, layoutId)
            }
        } else {
            view.adapter = StrategyRecyclerAdapter(items as ObservableArrayList<Any>, layoutStrategy, vm)
        }
    } else {
        (view.adapter as BaseRecyclerAdapter<T>).setItems(items)
    }
    (view.adapter as BaseRecyclerAdapter<*>).lifecycleOwner = lifecycleOwner
    (view.adapter as BaseRecyclerAdapter<*>).parentItem = parentItem

}
