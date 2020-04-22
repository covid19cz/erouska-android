package cz.covid19cz.erouska.helpers

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.hamcrest.CoreMatchers.containsString

private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun ViewInteraction.click(): ViewInteraction = perform(ViewActions.click())

fun click(id: Int): ViewInteraction =  onView(withId(id)).click()

fun clickUiAutomator(id: String) = device.findObject(UiSelector().resourceId(id)).click()

fun checkMatchesString(id: Int, @StringRes stringId: String): ViewInteraction = onView(withId(id)).check(
    ViewAssertions.matches(withText(stringId))
)

fun checkMatchesSubString(id: Int, @StringRes stringId: String): ViewInteraction = onView(withId(id)).check(
    ViewAssertions.matches(withSubstring(stringId))
)

fun checkMatchesContainsString(id: Int, @StringRes stringId: String): ViewInteraction = onView(withId(id)).check(
    ViewAssertions.matches(withText(containsString(stringId)))
)

fun checkDisplayed(id: Int): ViewInteraction = onView(withId(id)).check(
    ViewAssertions.matches(
        isDisplayed()
    )
)

fun typeText(id: Int, @StringRes text: String): ViewInteraction = onView(withId(id)).perform(ViewActions.typeText(text),
    ViewActions.closeSoftKeyboard()
)