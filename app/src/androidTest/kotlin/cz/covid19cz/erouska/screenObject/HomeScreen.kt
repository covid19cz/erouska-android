package cz.covid19cz.erouska.screenObject
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers.endsWith
import java.util.concurrent.TimeUnit

class HomeScreen {
    companion object {
        const val CANCEL_REGISTRATION_TITLE = "Registraci vašeho telefonního čísla jsme zrušili"
    }


    fun isErouskaActive() {

        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withText(R.string.dashboard_title_running)).checkDisplayed()
        }
    }

    fun cancelRegistration() {
        // open menu
        onView(withClassName(endsWith("OverflowMenuButton"))).click()
        // click on button Zrusit registraci
        onView(withText(R.string.delete_registration)).click()
        // click on second button Zrusit registraci
        onView(withId(R.id.confirm_button)).click()
        // text assert
        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.success_title)).checkMatchesString(CANCEL_REGISTRATION_TITLE)
        }
        // click on close button
        onView(withId(R.id.close_button)).click()
    }
}