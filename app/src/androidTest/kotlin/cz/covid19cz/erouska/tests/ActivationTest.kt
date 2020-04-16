package cz.covid19cz.erouska.tests

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.screenObject.*
import cz.covid19cz.erouska.testRules.DisableAnimationsRule
import cz.covid19cz.erouska.ui.main.MainActivity
import org.awaitility.Awaitility
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ActivationTest {
    private val welcomeScreen = WelcomeScreen()
    private val bluetoothPermissionScreen = BluetoothPermissionScreen()
    private val phoneNumberScreen = PhoneNumberScreen()
    private val smsScreen = SMSScreen()
    private val finishActivation = FinishActivationScreen()
    private val homeScreen = HomeScreen()

    @get:Rule
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule
    val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun activation() {
        // how to work
        welcomeScreen.howToWorkScreen()
        // activation
        welcomeScreen.startActivation()
        bluetoothPermissionScreen.allowPermission()
        phoneNumberScreen.run {
            typePhoneNumber()
            acceptWithAgreements()
            continueToSMSVerify()
        }
        smsScreen.run {
            typeSMSCode()
            verifySMSCode()
        }
        // samsung baterry saver screen
        finishActivation.finish()
        homeScreen.isErouskaActive()

        //deactivation
        homeScreen.cancelRegistration()

        // activation without sms code, wait 30s
        welcomeScreen.startActivation()
        phoneNumberScreen.run {
            typePhoneNumber()
            acceptWithAgreements()
            continueToSMSVerify()
        }
        Awaitility.await().ignoreExceptions().atMost(35, TimeUnit.SECONDS).untilAsserted {
            onView(ViewMatchers.withId(R.id.login_verify_later_button)).perform(click())
        }
        // samsung baterry saver screen
        finishActivation.finish()
        homeScreen.isErouskaActive()

    }
}
