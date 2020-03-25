package cz.covid19cz.app.ui.contacts

import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent.Command.EMAIL
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent.Command.EMERGENCY
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent.Command.FAQ
import cz.covid19cz.app.ui.contacts.event.ContactsCommandEvent.Command.IMPORTANT

class ContactsVM : BaseVM() {

    fun important() {
        publish(ContactsCommandEvent(IMPORTANT))
    }

    fun faq() {
        publish(ContactsCommandEvent(FAQ))
    }

    fun emergency() {
        publish(ContactsCommandEvent(EMERGENCY))
    }

    fun email() {
        publish(ContactsCommandEvent(EMAIL))
    }

    fun getFaqUrl() : String {
        return AppConfig.faqDynamicLink
    }

    fun getImportantUrl() : String {
        return AppConfig.importantDynamicLink
    }

    fun getEmergencyNumber() : String {
        return AppConfig.emergencyNumber
    }
}