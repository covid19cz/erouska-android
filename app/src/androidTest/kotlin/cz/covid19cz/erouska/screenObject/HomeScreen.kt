package cz.covid19cz.erouska.screenObject
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers.endsWith
import java.util.concurrent.TimeUnit

class HomeScreen {
    companion object {
        const val CANCEL_REGISTRATION_TITLE = "Registraci vašeho telefonního čísla jsme zrušili"
    }


    fun eRouskaIsActiv() {

        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.app_running_title)).check(matches(withText(R.string.dashboard_title_running)))
        }
    }

    fun cancelRegistration() {
        // open menu
        onView(withClassName(endsWith("OverflowMenuButton"))).perform(click())
        // click on button Zrusit registraci
        onView(withText(R.string.delete_registration)).perform(click())
        // click on second button Zrusit registraci
        onView(withId(R.id.confirm_button)).perform(click())
        // text assert
        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.success_title)).check(matches(withText(CANCEL_REGISTRATION_TITLE)))
        }
        // click on close button
        onView(withId(R.id.close_button)).perform(click())

    }
}