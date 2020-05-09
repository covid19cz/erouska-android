package cz.covid19cz.erouska.screenObject


import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.verifyLink
import cz.covid19cz.erouska.screenObject.WelcomeScreen.AUDIT_URL

object HelpScreen {

    const val GDPR_URL = "https://erouska.cz/gdpr"

    fun goToAboutApp() = click(R.id.nav_about)

    fun verifyLinks() {
        val descriptionElement = withId(R.id.help_desc)
        verifyLink(descriptionElement, "https://koronavirus.mzcr.cz/", "na webu Ministerstva zdravotnictví ČR")
        verifyLink(descriptionElement, "https://www.covid19cz.cz/covid19-cz/manifest/chytra-karantena", "chytré karantény")
        verifyLink(descriptionElement, GDPR_URL, "Podmínky zpracování osobních údajů při užívání aplikace eRouška.cz")
        verifyLink(descriptionElement, AUDIT_URL, "Audit zdrojového kódu aplikace")
        verifyLink(descriptionElement, "https://www.covid19cz.cz/covid19-cz/zavazek-datove-duvery", "Závazek datové důvěry iniciativy COVID19CZ")
        verifyLink(descriptionElement, "https://github.com/covid19cz/erouska-android", "Android")
        verifyLink(descriptionElement, "https://github.com/covid19cz/erouska-ios", "iOS")
    }
}