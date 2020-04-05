package cz.covid19cz.erouska.ui.contacts

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.ext.makeCall
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent
import kotlinx.android.synthetic.main.fragment_contacts.*

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
                ContactsCommandEvent.Command.WEB -> goToWeb()
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

        contacts_improve.text = HtmlCompat.fromHtml(getString(R.string.contacts_improve_desc), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun openImportant() {
        showWeb(viewModel.getImportantUrl())
    }

    private fun openFaq() {
        showWeb(viewModel.getFaqUrl())
    }

    private fun callEmergency() {
        context?.makeCall(viewModel.getEmergencyNumber())
    }

    private fun goToWeb() {
        showWeb(viewModel.getHomepageLink())
    }
}