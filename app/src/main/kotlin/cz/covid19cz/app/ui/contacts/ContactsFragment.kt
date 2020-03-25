package cz.covid19cz.app.ui.contacts

import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.app.utils.makeCall
import cz.covid19cz.app.utils.openChromeTab
import cz.covid19cz.app.utils.sendEmail
import kotlinx.android.synthetic.main.fragment_contacts.contacts_emergency
import kotlinx.android.synthetic.main.fragment_contacts.contacts_help
import kotlinx.android.synthetic.main.fragment_contacts.contacts_suspicion
import kotlinx.android.synthetic.main.fragment_help.help_desc

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