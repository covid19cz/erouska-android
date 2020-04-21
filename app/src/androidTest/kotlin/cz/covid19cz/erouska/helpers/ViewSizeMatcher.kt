package cz.covid19cz.erouska.helpers

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ViewSizeMatcher(private val expectedWith: Int, private val expectedHeight: Int) :
    TypeSafeMatcher<View?>(View::class.java) {

    override fun matchesSafely(target: View?): Boolean {
        val targetWidth: Int = target!!.width
        val targetHeight: Int = target.height
        return targetWidth == expectedWith && targetHeight == expectedHeight
    }

    override fun describeTo(description: Description) {
        description.appendText("with SizeMatcher: ")
        description.appendValue(expectedWith.toString() + "x" + expectedHeight)
    }
}