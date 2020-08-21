package cz.covid19cz.erouska.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.ItemContactsBinding

class ContactsAdapter(
    private var items: List<Contact> = emptyList(),
    private val onLinkClick: (String) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    fun updateItems(newItems: List<Contact>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val binding = DataBindingUtil.inflate<ItemContactsBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_contacts,
            parent,
            false
        )
        return ContactsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ContactsViewHolder(private val binding: ItemContactsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contact) {
            binding.contactTitle.text = item.title
            binding.contactDesc.text = item.text
            binding.contactLink.text = item.linkTitle
            binding.contactLink.setOnClickListener { onLinkClick(item.link) }
        }

    }

}