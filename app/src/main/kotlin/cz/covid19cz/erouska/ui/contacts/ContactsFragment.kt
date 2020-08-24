package cz.covid19cz.erouska.ui.contacts

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.contacts.event.ContactsEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import kotlinx.android.synthetic.main.fragment_contacts.*
import org.koin.android.ext.android.inject

class ContactsFragment : BaseFragment<FragmentPermissionssDisabledBinding, ContactsVM>(
    R.layout.fragment_contacts,
    ContactsVM::class
) {
    private val customTabHelper by inject<CustomTabHelper>()

    private val contactsAdapter: ContactsAdapter by lazy {
        ContactsAdapter(viewModel.items, viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) { updateState(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(false)

        contacts_list.setHasFixedSize(true)
        contacts_list.adapter = contactsAdapter
    }

    private fun updateState(event: ContactsEvent) {
        when (event) {
            is ContactsEvent.ContactLinkClicked -> showWeb(event.link, customTabHelper)
        }
    }

}
