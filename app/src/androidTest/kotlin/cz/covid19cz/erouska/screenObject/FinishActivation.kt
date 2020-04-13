package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R

class FinishActivation {
    companion object {
        const val TITLE = "eRouška potřebuje běžet i když s ní právě nepracujete"
    }

    fun finish() {
    /*
        await().dontCatchUncaughtExceptions().atLeast(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.battery_opt_title)).check(matches(isDisplayed()))
        }
    */

        onView(withId(R.id.done_btn)).perform(click())
    }

}