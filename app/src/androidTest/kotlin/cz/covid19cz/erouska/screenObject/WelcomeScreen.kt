package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R

class WelcomeScreen {
    fun continueToActivation() {
        Espresso.onView(withText(R.string.welcome_title)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.welcome_continue_btn)).perform(click())
    }
}