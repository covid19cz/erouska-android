package cz.covid19cz.erouska.tests

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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

    @get:Rule
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule
    val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun activation() {
        // how to work
        WelcomeScreen.howToWorkScreen()
        // activation
        WelcomeScreen.startActivation()
        BluetoothPermissionScreen.allowPermission()
        PhoneNumberScreen.run {
            typePhoneNumber()
            acceptWithAgreements()
            continueToSMSVerify()
        }
        SMSScreen.run {
            typeSMSCode()
            verifySMSCode()
        }

        BatterySaverInfoScreen.finish()
        HomeScreen.isErouskaActive()

        //deactivation
        HomeScreen.cancelRegistration()

        // activation without sms code, wait 30s
        WelcomeScreen.startActivation()
        PhoneNumberScreen.run {
            typePhoneNumber()
            acceptWithAgreements()
            continueToSMSVerify()
        }

        SMSScreen.verifyLater()

        BatterySaverInfoScreen.finish()
        HomeScreen.isErouskaActive()

    }
}
