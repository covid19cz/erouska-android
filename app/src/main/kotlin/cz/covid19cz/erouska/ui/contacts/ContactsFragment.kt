package cz.covid19cz.erouska.ui.contacts

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.erouska.utils.makeCall
import cz.covid19cz.erouska.utils.openChromeTab
import cz.covid19cz.erouska.utils.sendEmail
import kotlinx.android.synthetic.main.fragment_contacts.contacts_emergency
import kotlinx.android.synthetic.main.fragment_contacts.contacts_help

class ContactsFragment : BaseFragment<FragmentPermissionssDisabledBinding, ContactsVM>(
    R.layout.fragment_contacts,
    ContactsVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(ContactsCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                ContactsCommandEvent.Command.IMPORTANT -> openImportant()
                ContactsCommandEvent.Command.FAQ -> openFaq()
                ContactsCommandEvent.Command.EMERGENCY -> callEmergency()
                ContactsCommandEvent.Command.EMAIL -> sendEmail()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)

        val contactsHelpDescription: String = String.format(
            getString(R.string.contacts_help_desc),
            viewModel.getEmergencyNumber()
        )
        contacts_help.text = HtmlCompat.fromHtml(contactsHelpDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)
        contacts_emergency.text = getString(R.string.contacts_emergency, viewModel.getEmergencyNumber())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun openImportant() {
        context?.openChromeTab(viewModel.getImportantUrl())
    }

    fun openFaq() {
        context?.openChromeTab(viewModel.getFaqUrl())
    }

    fun callEmergency() {
        context?.makeCall(viewModel.getEmergencyNumber())
    }

    fun sendEmail() {
        context?.sendEmail(getString(R.string.default_email_subject), getString(R.string.default_email_address))
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}