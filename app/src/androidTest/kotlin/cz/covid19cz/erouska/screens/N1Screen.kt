package cz.covid19cz.erouska.screens

import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.ClickableLink
import cz.covid19cz.erouska.helpers.verifyMultipleLinks

object N1Screen {
    private const val EROUSKA_BASE_URL = "https://erouska.cz/"
    private const val TERMS_OF_USE_URL = "${EROUSKA_BASE_URL}podminky-pouzivani"
    private const val AUDIT_URL = "${EROUSKA_BASE_URL}audit-kod"
    private const val COVIDCZ_GITHUB_URL = "https://github.com/covid19cz/"
    private const val IOS_GITHUB_URL = "${COVIDCZ_GITHUB_URL}erouska-ios"
    private const val ANDROID_GITHUB_URL = "${COVIDCZ_GITHUB_URL}erouska-android"
    private const val APPSTORE_URL = "https://apps.apple.com/cz/app/erou%C5%A1ka/id1509210215"
    private const val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=cz.covid19cz.erouska"
    private const val APPLE_TRACKING_URL = "https://www.apple.com/covid19/contacttracing"
    private const val GOOGLE_TRACKING_URL = "https://www.google.com/covid19/exposurenotifications/"
    private const val CHYTRA_KARANTENA_URL = "https://chytrakarantena.cz/"
    private const val COVID_MZCR_URL = "https://koronavirus.mzcr.cz/"

    fun checkScreenAndLink() {
        val descriptionElement = withId(R.id.help_desc)
        val helpClickableLinks = arrayListOf(
            ClickableLink("${EROUSKA_BASE_URL}vyhodnoceni-rizika","Spolehlivost vyhodnocení rizikového kontaktu"),
            ClickableLink("${TERMS_OF_USE_URL}#technicke","Technické podmínky v Podmínkách zpracování"),
            ClickableLink(GOOGLE_PLAY_URL,"Google Play (Android)"),
            ClickableLink(APPSTORE_URL, "App Store (iOS)"),
            ClickableLink(TERMS_OF_USE_URL,"Informacích o zpracování osobních údajů v aplikaci eRouška 2.0"),
            ClickableLink(COVID_MZCR_URL,"na webu Ministerstva zdravotnictví ČR"),
            ClickableLink(CHYTRA_KARANTENA_URL,"chytré karantény"),
            ClickableLink(APPLE_TRACKING_URL,"Apple (anglicky)"),
            ClickableLink(GOOGLE_TRACKING_URL,"Google (česky)"),
            ClickableLink(TERMS_OF_USE_URL,"Informace o zpracování osobních údajů v aplikaci eRouška 2.0"),
            ClickableLink(AUDIT_URL,"Audit zdrojového kódu aplikace"),
            ClickableLink(TERMS_OF_USE_URL,"Informacích o zpracování osobních údajů v rámci aplikace eRouška 2.0"),
            ClickableLink(ANDROID_GITHUB_URL,"Android"),
            ClickableLink(IOS_GITHUB_URL,"iOS"),
            ClickableLink(AUDIT_URL,"prověřují nezávislé autority"),
            ClickableLink(TERMS_OF_USE_URL,"nepracuje s osobními údaji"),
            ClickableLink(TERMS_OF_USE_URL,"Informacích o zpracování osobních údajů v rámci aplikace eRouška")
        )

        verifyMultipleLinks(descriptionElement, helpClickableLinks)
    }
}