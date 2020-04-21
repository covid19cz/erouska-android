package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkMatchesContainsString
import cz.covid19cz.erouska.helpers.click

object WelcomeScreen {
    private const val PART_OF_HELP_TITLE = "Proč je eRouška potřeba?"

    fun startActivation() {
        onView(withText(R.string.welcome_title)).check(matches(isDisplayed()))
        click(R.id.welcome_continue_btn)
    }

    fun howToWorkScreen() {
        click(R.id.welcome_help_btn)
        checkMatchesContainsString(R.id.help_desc, PART_OF_HELP_TITLE)
        onView(withContentDescription("Přejít nahoru")).click()
    }
}