package cz.covid19cz.erouska.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import cz.covid19cz.erouska.screens.*
import cz.covid19cz.erouska.testRules.DisableAnimationsRule
import cz.covid19cz.erouska.ui.main.MainActivityOld
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ActivationTest {
    @get:Rule
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule
    val activityRule: ActivityTestRule<MainActivityOld> = ActivityTestRule(MainActivityOld::class.java)

    @Test
    fun activationTest() {
        A1Screen.run {
            checkAllPartsDisplayed()
            startActivation()
        }
        A2Screen.run {
            checkAllPartsDisplayed()
            enableBt()
            turnOnNotifications()
            acceptCovidActivation()
        }
        A3Screen.run {
            checkTermsOfUseLink()
            checkAllPartsDisplayed()
            finishActivation()
        }
        B1Screen.checkActiveScreen()
    }

    @Test
    fun checkHelpScreenTest() {
        A1Screen.goToHelp()
        N1Screen.checkScreenAndLink()
    }
}
