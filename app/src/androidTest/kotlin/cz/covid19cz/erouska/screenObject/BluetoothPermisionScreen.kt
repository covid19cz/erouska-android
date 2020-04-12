package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import cz.covid19cz.erouska.R

class BluetoothPermissionScreen {
    fun allowPermission() {
        onView(withText(R.string.permission_onboarding_title)).check(matches(isDisplayed()))
        onView(withId(R.id.enable_bluetooth_btn)).perform(click())
        // Initialize UiDevice instance
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val cancelButton = device.findObject(
            UiSelector().resourceId("com.android.packageinstaller:id/permission_allow_button")
        )
        cancelButton.click()
    }
}