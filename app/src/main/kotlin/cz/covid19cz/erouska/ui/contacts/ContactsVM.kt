package cz.covid19cz.erouska.ui.contacts

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent
import cz.covid19cz.erouska.ui.contacts.event.ContactsCommandEvent.Command.*
import cz.covid19cz.erouska.ui.exposure.entity.SymptomsData

class ContactsVM : BaseVM() {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val symptoms: SymptomsData = Gson().fromJson(AppConfig.contactsContentJson, SymptomsData::class.java)
    }

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
        return ""
//        return AppConfig.faqLink
    }

    fun getImportantUrl() : String {
        return ""
//        return AppConfig.importantLink
    }

    fun getChatBotLink() : String {
        return AppConfig.chatBotLink
    }
}