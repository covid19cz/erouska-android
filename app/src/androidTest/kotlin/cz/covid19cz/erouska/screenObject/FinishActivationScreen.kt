package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

class FinishActivationScreen {
    companion object {
        const val TITLE = "eRouška potřebuje běžet i když s ní právě nepracujete"
    }

    fun finish() {

        await().ignoreExceptions().atMost(15, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.battery_opt_title)).checkMatchesString(TITLE)
        }

        onView(withId(R.id.done_btn)).click()
    }

}
