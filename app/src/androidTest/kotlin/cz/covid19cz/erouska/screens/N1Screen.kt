package cz.covid19cz.erouska.screens

import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.ClickableLink
import cz.covid19cz.erouska.helpers.verifyLink
import cz.covid19cz.erouska.helpers.verifyMultipleLinks

object N1Screen {

    private const val GDPR_URL = "https://erouska.cz/gdpr"
    private const val AUDIT_URL = "https://erouska.cz/audit-kod"
    private const val DATA_PRIVACY_URL = "https://www.covid19cz.cz/covid19-cz/zavazek-datove-duvery"

    fun checkScreenAndLink() {

        verifyLink(withId(R.id.chat_container), "https://erouska.cz/#chat-open")

        val descriptionElement = withId(R.id.help_desc)
        val helpClickableLinks = arrayListOf(
            ClickableLink("https://koronavirus.mzcr.cz/", "na webu Ministerstva zdravotnictví ČR"),
            ClickableLink("https://www.covid19cz.cz/covid19-cz/manifest/chytra-karantena", "chytré karantény"),
            ClickableLink("https://erouska.cz/navody#instalace", "PDF či video návodu"),
            ClickableLink("mailto:info@erouska.cz", "info@erouska.cz"),
            ClickableLink("https://erouska.cz/navody#nastaveni", "Na stránce s návody najdete stručné postupy"),
            ClickableLink("https://developer.android.com/guide/topics/connectivity/bluetooth#Permissions", "na webu OS Android pro vývojáře"),
            ClickableLink( GDPR_URL, "Podmínkách zpracování osobních údajů při užívání aplikace eRouška.cz"),
            ClickableLink(DATA_PRIVACY_URL, "Závazku datové důvěry iniciativy COVID19CZ"),
            ClickableLink(AUDIT_URL, "prověřeny nezávislými autoritami"),
            ClickableLink(GDPR_URL, "Podmínky zpracování osobních údajů při užívání aplikace eRouška.cz"),
            ClickableLink(AUDIT_URL, "Audit zdrojového kódu aplikace"),
            ClickableLink(DATA_PRIVACY_URL, "Závazek datové důvěry iniciativy COVID19CZ"),
            ClickableLink("https://github.com/covid19cz/erouska-android", "Android"),
            ClickableLink("https://github.com/covid19cz/erouska-ios", "iOS")
        )

        verifyMultipleLinks(descriptionElement, helpClickableLinks)
    }
}