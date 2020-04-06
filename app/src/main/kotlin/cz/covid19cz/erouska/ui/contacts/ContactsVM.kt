package cz.covid19cz.erouska.ui.contacts

import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent.Command.WEB
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent.Command.FAQ
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent.Command.IMPORTANT

class ContactsVM : BaseVM() {

    fun important() {
        publish(ContactsCommandEvent(IMPORTANT))
    }

    fun faq() {
        publish(ContactsCommandEvent(FAQ))
    }

    fun openWeb() {
        publish(ContactsCommandEvent(WEB))
    }

    fun getFaqUrl() : String {
        return AppConfig.faqLink
    }

    fun getImportantUrl() : String {
        return AppConfig.importantLink
    }

    fun getEmergencyNumber() : String {
        return AppConfig.emergencyNumber
    }

    fun getHomepageLink() : String {
        return AppConfig.homepageLink
    }
}