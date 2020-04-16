package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.awaitility.Awaitility.await
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.screenObject.PhoneNumberScreen.Companion.PHONE_NUMBER
import java.util.concurrent.TimeUnit

class SMSScreen {
    companion object {
        const val SMS_CODE = "000000"
    }

    fun typeSMSCode() {
        await().ignoreExceptions().atMost(5, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.phone_number_code)).checkMatchesSubString(PHONE_NUMBER)
        }
        onView(withId(R.id.login_verif_code_input)).typeText(SMS_CODE)
    }

    fun verifySMSCode() {
        onView(withId(R.id.login_verif_code_send_btn)).click()
    }
}