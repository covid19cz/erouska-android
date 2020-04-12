package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R


class PhoneNumberScreen {
    companion object {
        const val PHONE_NUMBER = "666 666 666"
    }
    fun typePhoneNumber() {
        Espresso.onView(withId(R.id.login_desc)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.login_verif_phone_input)).perform(typeText(PHONE_NUMBER.replace(" ", "")),closeSoftKeyboard())
    }

    fun acceptWithAgreements() {
        onView(withId(R.id.login_checkbox)).perform(click())
    }

    fun continueToSMSVerify() {
        onView(withId(R.id.login_verif_activate_btn)).perform(click())
        Thread.sleep(2000)
    }
}