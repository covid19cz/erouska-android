package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.scrollTo
import cz.covid19cz.erouska.helpers.verifyLink

object ContactsScreen {

    fun verifyLinks() {
        verifyLink(withId(R.id.contacts_important_btn), "https://koronavirus.mzcr.cz/dulezite-kontakty-odkazy")
        verifyLink(withId(R.id.contacts_faq_btn), "https://koronavirus.mzcr.cz/otazky-a-odpovedi")
        scrollTo(R.id.contacts_email)
        verifyLink(withId(R.id.contacts_email), "https://erouska.cz")
    }
}