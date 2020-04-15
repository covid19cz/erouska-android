package cz.covid19cz.erouska.screenObject

import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import cz.covid19cz.erouska.R

class BluetoothPermissionScreen {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun allowPermission() {
        onView(withText(R.string.permission_onboarding_title)).check(matches(isDisplayed()))
        onView(withId(R.id.enable_bluetooth_btn)).perform(click())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val allowLocation = findUiObject("com.android.permissioncontroller:id/permission_allow_foreground_only_button")
            allowLocation.click()
        } else {
            val allowButton = findUiObject("com.android.packageinstaller:id/permission_allow_button")
            allowButton.click()
        }
    }

    private fun findUiObject(resourceId: String) = device.findObject(UiSelector().resourceId(resourceId))
}