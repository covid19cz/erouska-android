package cz.covid19cz.erouska.helpers

import androidx.annotation.StringRes
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import org.hamcrest.CoreMatchers.containsString

fun ViewInteraction.click(): ViewInteraction = perform(ViewActions.click())

fun ViewInteraction.checkMatchesString(@StringRes stringId: String): ViewInteraction = check(
    ViewAssertions.matches(ViewMatchers.withText(stringId))
)
fun ViewInteraction.checkMatchesSubString(@StringRes stringId: String): ViewInteraction = check(
    ViewAssertions.matches(withSubstring(stringId))
)
fun ViewInteraction.checkMatchesContainsString(@StringRes stringId: String): ViewInteraction = check(
    ViewAssertions.matches(ViewMatchers.withText(containsString(stringId)))
)

fun ViewInteraction.checkDisplayed(): ViewInteraction = check(
    ViewAssertions.matches(
        ViewMatchers.isDisplayed()
    )
)

fun ViewInteraction.typeText(): ViewInteraction = typeText()
fun ViewInteraction.typeText(@StringRes text: String): ViewInteraction = perform(ViewActions.typeText(text),
    ViewActions.closeSoftKeyboard()
)