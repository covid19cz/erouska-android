package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import org.hamcrest.CoreMatchers.containsString

class WelcomeScreen {
    companion object {
        const val PART_OF_HELP_TITLE = "Proč je eRouška potřeba?"
    }
    fun startActivation() {
        onView(withText(R.string.welcome_title)).check(matches(isDisplayed()))
        onView(withId(R.id.welcome_continue_btn)).perform(click())
    }

    fun howToWorkScreen() {
        onView(withId(R.id.welcome_help_btn)).perform(click())
        onView(withId(R.id.help_desc)).check(matches(withText(containsString(PART_OF_HELP_TITLE))))
        onView(withContentDescription("Přejít nahoru")).perform(click())
    }
}