package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.checkMatchesContainsString
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.verifyLink

object WelcomeScreen {
    private const val PART_OF_HELP_TITLE = "Proč je eRouška potřeba?"
    const val AUDIT_URL = "https://erouska.cz/audit-kod"

    fun startActivation() {
        checkDisplayed(R.id.welcome_title)
        click(R.id.welcome_continue_btn)
    }

    fun goToHelpScreen() {
        click(R.id.welcome_help_btn)
        checkMatchesContainsString(R.id.help_desc, PART_OF_HELP_TITLE)
    }

    fun checkAuditLink() {
        verifyLink(withId(R.id.welcome_desc), AUDIT_URL, "Nezávislý audit")
    }
}