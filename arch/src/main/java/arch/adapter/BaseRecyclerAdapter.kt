package arch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import arch.viewmodel.BaseArchViewModel
import cz.stepansonsky.mvvm.BR

/**
 * Created by Stepan on 9.11.2016.
 */

abstract class BaseRecyclerAdapter<T> : RecyclerView.Adapter<BaseRecyclerAdapter<T>.BaseMvvmRecyclerViewHolder<T>> {

    private var viewModel: BaseArchViewModel? = null
    private var items: ObservableList<T>? = null
    var lifecycleOwner: LifecycleOwner? = null
    var parentItem: Any? = null

    private var onListChangedCallback: ObservableList.OnListChangedCallback<ObservableList<T>>? = null

    @LayoutRes
    protected abstract fun getLayoutId(itemType: Int): Int

    constructor(items: ObservableList<T>) {
        this.items = items
        initOnListChangedListener()
    }

    constructor(items: ObservableList<T>, viewModel: BaseArchViewModel?) {
        this.viewModel = viewModel
        this.items = items
        initOnListChangedListener()
    }

    private fun initOnListChangedListener() {
        onListChangedCallback = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(sender: ObservableList<T>) {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeInserted(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeMoved(
                sender: ObservableList<T>,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }
        }
        items!!.addOnListChangedCallback(onListChangedCallback)
    }

    private fun getViewHolderBinding(parent: ViewGroup, @LayoutRes itemLayoutId: Int): ViewDataBinding {
        return DataBindingUtil.inflate(LayoutInflater.from(parent.context), itemLayoutId, parent, false)
    }

    override fun onBindViewHolder(holder: BaseMvvmRecyclerViewHolder<T>, position: Int) {
        val item = items!![position]
        holder.bind(item, holder.binder)
        holder.binder!!.executePendingBindings()
    }

    fun getItem(position: Int): T {
        return items!![position]
    }

    override fun getItemCount(): Int {
        return if (items != null) {
            items!!.size
        } else 0
    }

    fun getItems(): List<T>? {
        return items
    }

    fun setItems(items: ObservableList<T>?) {
        if (items != null && items == this.items) {
            //notifyDataSetChanged();
        } else {
            this.items = items
            initOnListChangedListener()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMvvmRecyclerViewHolder<T> {
        return BaseMvvmRecyclerViewHolder(getViewHolderBinding(parent, getLayoutId(viewType)))
    }

    inner class BaseMvvmRecyclerViewHolder<T>(v: ViewDataBinding) : RecyclerView.ViewHolder(v.root) {

        val binder: ViewDataBinding? = DataBindingUtil.bind(v.root)

        fun bind(item: T, binder: ViewDataBinding?) {
            binder!!.setVariable(BR.vm, viewModel)
            binder.setVariable(BR.item, item)
            if (lifecycleOwner != null) {
                binder.setVariable(BR.lifecycle, lifecycleOwner)
                binder.lifecycleOwner = lifecycleOwner
            }
            if (parentItem != null) {
                binder.setVariable(BR.parentItem, parentItem)
            }
        }

    }

}
