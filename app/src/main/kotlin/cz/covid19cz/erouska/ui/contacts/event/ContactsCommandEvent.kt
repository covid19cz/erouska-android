package cz.covid19cz.erouska.ui.contacts.event

import arch.event.LiveEvent

class ContactsCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        IMPORTANT,
        FAQ,
        CHATBOT,
    }

}