package cz.covid19cz.erouska.ui.exposure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.ItemPreventionBinding
import cz.covid19cz.erouska.ui.exposure.entity.PreventionItem

class PreventionAdapter(
    private var items: List<PreventionItem> = emptyList()
) : RecyclerView.Adapter<PreventionAdapter.PreventionViewHolder>() {

    fun setItems(newItems: List<PreventionItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreventionViewHolder {
        val binding = DataBindingUtil.inflate<ItemPreventionBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_prevention,
            parent,
            false
        )
        return PreventionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PreventionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class PreventionViewHolder(private val binding: ItemPreventionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PreventionItem) {
            binding.symptomLabel.text = item.label
            Glide.with(binding.root.context).load(item.iconUrl).placeholder(R.drawable.ic_item_empty).into(binding.symptomImg)
        }

    }

}
