package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.typeText

object PhoneNumberScreen {
    const val PHONE_NUMBER = "731 000 000"

    fun typePhoneNumber() {
        onView(withId(R.id.login_desc)).checkDisplayed()
        onView(withId(R.id.login_verif_phone_input)).typeText(PHONE_NUMBER)
    }

    fun acceptWithAgreements() {
        onView(withId(R.id.login_checkbox)).click()
    }

    fun continueToSMSVerify() {
        onView(withId(R.id.login_verif_activate_btn)).click()
    }
}