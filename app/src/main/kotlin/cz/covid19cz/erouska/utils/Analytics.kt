package cz.covid19cz.erouska.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {
    const val KEY_EXPORT_DOWNLOAD_STARTED = "key_export_download_started"
    const val KEY_EXPORT_DOWNLOAD_FINISHED = "key_export_download_finished"

    const val KEY_SHARE_APP = "tap_share_app"
    const val KEY_PAUSE_APP = "tap_pause_app"
    const val KEY_RESUME_APP = "tap_resume_app"

    const val KEY_HOME = "tap_tab_home"
    const val KEY_NEWS = "tap_tab_news"
    const val KEY_CONTACTS = "tap_tab_contacts"
    const val KEY_HELP = "tap_tab_help"

    const val KEY_CURRENT_MEASURES = "tap_current_measures"
    
    fun logEvent(context: Context, key: String) {
        val analytics = FirebaseAnalytics.getInstance(context)
        analytics.logEvent(key, Bundle())
    }
}