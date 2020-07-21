package cz.covid19cz.erouska.ui.contacts

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import kotlinx.android.synthetic.main.fragment_contacts.*
import org.koin.android.ext.android.inject

class ContactsFragment : BaseFragment<FragmentPermissionssDisabledBinding, ContactsVM>(
    R.layout.fragment_contacts,
    ContactsVM::class
) {
    private val customTabHelper by inject<CustomTabHelper>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(ContactsCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                ContactsCommandEvent.Command.IMPORTANT -> openImportant()
                ContactsCommandEvent.Command.FAQ -> openFaq()
                ContactsCommandEvent.Command.CHATBOT -> openChatBot()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)

        val contactsHelpDescription: String = String.format(
            getString(R.string.contacts_help_desc)
        )
        contacts_help.text = HtmlCompat.fromHtml(contactsHelpDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)

        contacts_improve.text = HtmlCompat.fromHtml(getString(R.string.contacts_improve_desc), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun openImportant() {
        showWeb(viewModel.getImportantUrl(), customTabHelper)
    }

    private fun openFaq() {
        showWeb(viewModel.getFaqUrl(), customTabHelper)
    }

    private fun openChatBot() {
        showWeb(viewModel.getChatBotLink(), customTabHelper)
    }
}
