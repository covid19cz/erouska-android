package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withText
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.RETRY_TIMEOUT
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.checkMatchesString
import cz.covid19cz.erouska.helpers.click
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers.endsWith
import java.util.concurrent.TimeUnit

object HomeScreen {
    private const val CANCEL_REGISTRATION_TITLE = "Registraci vašeho telefonního čísla jsme zrušili"

    fun isErouskaActive() {

        await().ignoreExceptions().atMost(RETRY_TIMEOUT, TimeUnit.SECONDS).untilAsserted {
            checkDisplayed(R.id.app_running_title)
        }
    }

    fun cancelRegistration() {
        // open menu
        click(withClassName(endsWith("OverflowMenuButton")))
        // click on button Zrusit registraci
        click(withText(R.string.delete_registration))
        // click on second button Zrusit registraci
        click(R.id.confirm_button)
        // text assert
        await().ignoreExceptions().atMost(RETRY_TIMEOUT, TimeUnit.SECONDS).untilAsserted {
            checkMatchesString(R.id.success_title, CANCEL_REGISTRATION_TITLE)
        }
        // click on close button
        click(R.id.close_button)
    }
}