package cz.covid19cz.erouska.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {
    const val KEY_EXPORT_DOWNLOAD_STARTED = "key_export_download_started"
    const val KEY_EXPORT_DOWNLOAD_FINISHED = "key_export_download_finished"

    const val KEY_SHARE_APP = "click_share_app"
    const val KEY_PAUSE_APP = "click_pause_app"
    const val KEY_RESUME_APP = "click_resume_app"

    const val KEY_HOME = "click_tab_home"
    const val KEY_NEWS = "click_tab_news"
    const val KEY_CONTACTS = "click_tab_contacts"
    const val KEY_HELP = "click_tab_help"

    const val KEY_CURRENT_MEASURES = "click_current_measures"
    
    fun logEvent(context: Context, key: String) {
        val analytics = FirebaseAnalytics.getInstance(context)
        analytics.logEvent(key, Bundle())
    }
}