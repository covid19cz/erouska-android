package cz.covid19cz.erouska.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {
    const val KEY_EXPORT_DOWNLOAD_STARTED = "key_export_download_started"
    const val KEY_EXPORT_DOWNLOAD_FINISHED = "key_export_download_finished"

    fun logEvent(context: Context, key: String) {
        val analytics = FirebaseAnalytics.getInstance(context)
        analytics.logEvent(key, Bundle())
    }
}