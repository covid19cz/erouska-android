package cz.covid19cz.erouska.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R

class ShortcutsManager(private val context: Context) {

    companion object {
        object ShortcutsActions {
            private val SCHEME = "erouska" + if (BuildConfig.FLAVOR == "dev") "-dev" else ""

            val URL_PAUSE: Uri = Uri.parse("${SCHEME}://service/pause")
            val URL_RESUME: Uri = Uri.parse("${SCHEME}://service/resume")
        }
    }

    fun handleShortcut(intent: Intent) {
        if (intent.action == Intent.ACTION_RUN) {
            when (intent.data) {
                ShortcutsActions.URL_PAUSE -> {

                }
                ShortcutsActions.URL_RESUME -> {

                }
            }
        }
    }

    fun updateShortcuts(isRunning: Boolean) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)

        val shortcut = if (isRunning) {
            ShortcutInfoCompat.Builder(context, "erouska-service")
                .setShortLabel(context.getString(R.string.pause_app))
                .setLongLabel(context.getString(R.string.pause_app))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_pause))
                .setIntent(
                    Intent(
                        Intent.ACTION_RUN,
                        ShortcutsActions.URL_PAUSE
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                .build()
        } else {
            ShortcutInfoCompat.Builder(context, "erouska-service")
                .setShortLabel(context.getString(R.string.start_app))
                .setLongLabel(context.getString(R.string.start_app))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_resume))
                .setIntent(
                    Intent(
                        Intent.ACTION_RUN,
                        ShortcutsActions.URL_RESUME
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                .build()
        }

        ShortcutManagerCompat.addDynamicShortcuts(context, listOf(shortcut))
    }
}