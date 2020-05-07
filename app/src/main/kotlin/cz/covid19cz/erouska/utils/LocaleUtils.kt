package cz.covid19cz.erouska.utils

import cz.covid19cz.erouska.BuildConfig
import java.util.*

object LocaleUtils {

    fun getLocale(): String {
        return Locale.getDefault().toString()
    }

    fun getSupportedLanguage(): String {
        val currentLanguage = Locale.getDefault().language
        return if (BuildConfig.SUPPORTED_LANGUAGES.contains(currentLanguage)) {
            currentLanguage
        } else {
            "en"
        }
    }
}