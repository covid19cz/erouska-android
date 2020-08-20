package cz.covid19cz.erouska.ui.exposure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.ItemSymptomBinding
import cz.covid19cz.erouska.ui.exposure.entity.SymptomItem

class SymptomsAdapter(
    private var items: List<SymptomItem> = emptyList()
) : RecyclerView.Adapter<SymptomsAdapter.SymptomViewHolder>() {

    fun updateData(newItems: List<SymptomItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val binding = DataBindingUtil.inflate<ItemSymptomBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_symptom,
            parent,
            false
        )
        return SymptomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SymptomViewHolder(private val binding: ItemSymptomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SymptomItem) {
            binding.symptomLabel.text = item.label
            Glide.with(binding.root.context).load(item.iconUrl).placeholder(R.drawable.ic_item_empty).into(binding.symptomImg)
        }

    }
}
