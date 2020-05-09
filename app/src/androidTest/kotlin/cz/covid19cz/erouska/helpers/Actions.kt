package cz.covid19cz.erouska.helpers

import android.app.Instrumentation
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

const val RETRY_TIMEOUT = 10L

private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun click(element: Matcher<View>): ViewInteraction = onView(element).perform(ViewActions.click())

fun click(id: Int): ViewInteraction =  click(withId(id))

fun scrollTo(id: Int): ViewInteraction = onView(withId(id)).perform(ViewActions.scrollTo())

fun clickUiAutomator(buttonText: String) = device.findObject(UiSelector().clickable(true).textStartsWith(buttonText)).click() // startsWith because it is case insensitive

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

fun checkDisplayed(text: String): ViewInteraction = onView(withText(text)).check(
    ViewAssertions.matches(
        isDisplayed()
    )
)

fun typeText(id: Int, @StringRes text: String): ViewInteraction = onView(withId(id)).perform(ViewActions.typeText(text),
    ViewActions.closeSoftKeyboard()
)


/**
 * verify link
 * @param element element that has link
 * @param url link that should be open
 * @param clickableText optional pass if only part of the element is clickable
 */
fun verifyLink(element: Matcher<View>, url: String, clickableText: String? = null) {

    Intents.init()
    val expectedIntent = AllOf.allOf(
        IntentMatchers.hasAction(Intent.ACTION_VIEW),
        IntentMatchers.hasData(url)
    )
    Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

    if(clickableText.isNullOrBlank()) {
        onView(element).perform(ViewActions.click())
    } else {
        onView(element).perform(ViewActions.openLinkWithText(clickableText))
    }

    Intents.intended(expectedIntent)
    Intents.release()
}