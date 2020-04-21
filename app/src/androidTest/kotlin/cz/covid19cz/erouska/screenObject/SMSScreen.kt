package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.*
import cz.covid19cz.erouska.screenObject.PhoneNumberScreen.PHONE_NUMBER
import org.awaitility.Awaitility.await
import org.hamcrest.Matchers.not
import java.util.concurrent.TimeUnit

object SMSScreen {
    private const val SMS_CODE = "000000"

    fun typeSMSCode() {
        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.phone_number_code)).checkMatchesSubString(PHONE_NUMBER)
        }
        onView(withId(R.id.login_verif_code_input)).typeText(SMS_CODE)
    }

    fun verifySMSCode() {
        onView(withId(R.id.login_verif_code_send_btn)).click()
    }

    fun verifyLater() {

        await().ignoreExceptions()
            .atLeast(25, TimeUnit.SECONDS)
            .atMost(35, TimeUnit.SECONDS)
            .untilAsserted {
                onView(withId(R.id.login_verify_later_button)).check(
                    matches(
                        not(
                            ViewSizeMatcher(
                                0,
                                0
                            )
                        )
                    )
                )
            }
        onView(withId(R.id.login_verify_later_button)).click()
    }
}