package cz.covid19cz.erouska.ui.contacts.event

sealed class ContactsEvent {
    class ContactLinkClicked(val link: String) : ContactsEvent()
}