package cz.covid19cz.erouska.ui.contacts

import androidx.databinding.ObservableArrayList
import arch.adapter.BaseRecyclerAdapter
import cz.covid19cz.erouska.R

class ContactsAdapter(items: ObservableArrayList<Contact>, vm: ContactsVM) : BaseRecyclerAdapter<Contact>(items, vm) {

    override fun getLayoutId(itemType: Int): Int = R.layout.item_contacts

}