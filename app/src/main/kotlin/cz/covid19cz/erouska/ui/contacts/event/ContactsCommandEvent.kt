package cz.covid19cz.erouska.ui.contacts.event

import cz.covid19cz.erouska.ui.contacts.Contact

sealed class ContactsEvent {
    class ContactsLoadedEvent(val contacts: List<Contact>) : ContactsEvent()
}