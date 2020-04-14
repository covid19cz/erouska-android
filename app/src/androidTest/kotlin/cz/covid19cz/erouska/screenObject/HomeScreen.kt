package cz.covid19cz.erouska.screenObject
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

class HomeScreen {
    companion object {
        const val TITLE = "eRouška je aktivní"
    }

    fun eRouskaIsActiv() {

        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.app_running_title)).check(matches(withText(TITLE)))
        }
    }

}
