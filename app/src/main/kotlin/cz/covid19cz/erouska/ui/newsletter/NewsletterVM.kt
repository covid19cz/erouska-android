package cz.covid19cz.erouska.ui.newsletter

import androidx.lifecycle.MutableLiveData
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.ui.newsletter.event.NewsletterEvent

class NewsletterVM : BaseArchViewModel() {

    val state = MutableLiveData<NewsletterEvent>(NewsletterEvent.NewsletterExpansion)

    fun next() {
        val next = when (state.value) {
            NewsletterEvent.NewsletterExpansion -> NewsletterEvent.NewsletterActiveNotification
            NewsletterEvent.NewsletterActiveNotification -> NewsletterEvent.NewsletterPhoneNumbers
            NewsletterEvent.NewsletterPhoneNumbers -> NewsletterEvent.NewsletterAccessible
            NewsletterEvent.NewsletterAccessible -> NewsletterEvent.NewsletterPrivacy
            else -> NewsletterEvent.NewsletterFinish
        }

        state.value = next
    }

    fun previous() {
        val prev = when (state.value) {
            NewsletterEvent.NewsletterPrivacy -> NewsletterEvent.NewsletterAccessible
            NewsletterEvent.NewsletterAccessible -> NewsletterEvent.NewsletterPhoneNumbers
            NewsletterEvent.NewsletterPhoneNumbers -> NewsletterEvent.NewsletterActiveNotification
            else -> NewsletterEvent.NewsletterExpansion
        }

        state.value = prev
    }

}