package cz.covid19cz.app.ui.contacts.event

import arch.event.LiveEvent

class ContactsCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        IMPORTANT,
        FAQ,
        EMERGENCY,
        EMAIL,
    }

}