package cz.covid19cz.erouska.ui.contacts.event

sealed class ContactsEvent {
    data class ContactLinkClicked(val link: String) : ContactsEvent()
}