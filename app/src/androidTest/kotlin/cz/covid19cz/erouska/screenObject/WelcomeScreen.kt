package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R

class WelcomeScreen {
    companion object {
        const val PART_OF_HELP_TITLE = "Proč je eRouška potřeba?"
    }
    fun startActivation() {
        onView(withText(R.string.welcome_title)).check(matches(isDisplayed()))
        onView(withId(R.id.welcome_continue_btn)).click()
    }

    fun howToWorkScreen() {
        onView(withId(R.id.welcome_help_btn)).click()
        onView(withId(R.id.help_desc)).checkMatchesContainsString(PART_OF_HELP_TITLE)
        onView(withContentDescription("Přejít nahoru")).click()
    }
}