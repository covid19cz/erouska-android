package cz.covid19cz.erouska.ui.contacts

import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent.Command.*

class ContactsVM : BaseVM() {

    fun important() {
        publish(ContactsCommandEvent(IMPORTANT))
    }

    fun faq() {
        publish(ContactsCommandEvent(FAQ))
    }

    fun openChatBot() {
        publish(ContactsCommandEvent(CHATBOT))
    }

    fun getFaqUrl() : String {
        return AppConfig.faqLink
    }

    fun getImportantUrl() : String {
        return AppConfig.importantLink
    }

    fun getChatBotLink() : String {
        return AppConfig.chatBotLink
    }
}