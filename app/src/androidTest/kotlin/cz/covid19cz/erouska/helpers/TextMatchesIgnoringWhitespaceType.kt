package cz.covid19cz.erouska.helpers

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class TextMatchesIgnoringWhitespaceType(string: String?) :
    TypeSafeMatcher<String>() {
    private val string: String
    override fun matchesSafely(item: String): Boolean {
        return normalizeWhitespaces(string).equals(normalizeWhitespaces(item), ignoreCase = true)
    }

    override fun describeTo(description: Description) {
        description.appendText("Expected same strings")
    }

    private fun normalizeWhitespaces(string: String): String {
        return string.replace("\\s".toRegex(), "")
    }

    init {
        requireNotNull(string) { "Non-null value required by TextMatchesIgnoringWhitespaceType()" }
        this.string = string
    }
}