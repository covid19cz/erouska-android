package cz.covid19cz.erouska.ui.newsletter.event

sealed class NewsletterEvent {
    object NewsletterExpansion : NewsletterEvent()
    object NewsletterActiveNotification : NewsletterEvent()
    object NewsletterPhoneNumbers : NewsletterEvent()
    object NewsletterAccessible : NewsletterEvent()
    object NewsletterPrivacy : NewsletterEvent()
    object NewsletterFinish : NewsletterEvent()
}
