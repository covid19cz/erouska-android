package cz.covid19cz.app.ui.contacts

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.app.utils.makeCall
import cz.covid19cz.app.utils.openChromeTab
import cz.covid19cz.app.utils.sendEmail

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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun openImportant() {
        context?.openChromeTab("https://koronavirus.mzcr.cz/dulezite-kontakty-odkazy/")
    }

    fun openFaq() {
        context?.openChromeTab("https://koronavirus.mzcr.cz/otazky-odpovedi")
    }

    fun callEmergency() {
        context?.makeCall("1212")
    }

    fun sendEmail() {
        context?.sendEmail(getString(R.string.default_email_subject), getString(R.string.default_email_address))
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}