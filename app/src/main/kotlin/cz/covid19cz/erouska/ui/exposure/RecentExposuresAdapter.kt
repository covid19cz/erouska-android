package cz.covid19cz.erouska.ui.exposure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cz.covid19cz.erouska.BR
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.ItemRecentExposureBinding

class RecentExposuresAdapter(
    private var items: List<Exposure> = emptyList()
) :
    RecyclerView.Adapter<RecentExposuresAdapter.RecentExposureViewHolder>() {

    fun updateItems(newItems: List<Exposure>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentExposureViewHolder {
        val binding = DataBindingUtil.inflate<ItemRecentExposureBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_recent_exposure,
            parent,
            false
        )
        return RecentExposureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentExposureViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class RecentExposureViewHolder(private val binding: ItemRecentExposureBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Exposure) {
            binding.setVariable(BR.exposure, item)
            binding.executePendingBindings()
        }

    }
}