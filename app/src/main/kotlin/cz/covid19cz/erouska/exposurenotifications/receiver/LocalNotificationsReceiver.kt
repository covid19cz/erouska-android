package cz.covid19cz.erouska.exposurenotifications.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.main.MainActivity
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject


class LocalNotificationsReceiver : BroadcastReceiver(), KoinComponent {

    val exposureNotificationsRepository: ExposureNotificationsRepository by inject()
    val prefs: SharedPrefsRepository by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        checkExposure(context)
        checkErouskaPaused(context)
        checkOutdatedKeyData(context)
    }

    private fun checkErouskaPaused(context: Context) {
        GlobalScope.launch {
            runCatching {
                exposureNotificationsRepository.isEnabled()
            }.onSuccess {
                if (!it) {
                    showErouskaPausedNotification(context)
                }
            }.onFailure {
                showErouskaPausedNotification(context)
            }
        }
    }

    private fun checkExposure(context: Context){
        GlobalScope.launch {
            runCatching {
                return@runCatching exposureNotificationsRepository.getLastRiskyExposure()?.daysSinceEpoch

            }.onSuccess {lastExposure ->
                val lastNotifiedExposure = prefs.getLastNotifiedExposure()
                if (lastExposure != null && lastNotifiedExposure != 0 && lastExposure != lastNotifiedExposure) {
                    showNotification(
                        R.string.notification_exposure_title,
                        R.string.notification_exposure_text,
                        CHANNEL_ID_EXPOSURE,
                        context
                    )
                    prefs.setLastNotifiedExposure(lastExposure)
                }
            }.onFailure {
                L.e(it)
            }
        }
    }

    private fun showErouskaPausedNotification(context: Context) {
        showNotification(
            R.string.notification_exposure_notifications_off_title,
            R.string.notification_exposure_notifications_off_text,
            CHANNEL_ID_NOT_RUNNING,
            context
        )
    }

    private fun checkOutdatedKeyData(context: Context) {
        if (prefs.hasOutdatedKeyData()) {
            showNotification(
                R.string.notification_data_outdated_title,
                R.string.notification_data_outdated_text,
                CHANNEL_ID_OUTDATED_DATA,
                context
            )
        }
    }

    private fun showNotification(
        @StringRes title: Int,
        @StringRes text: Int,
        channelId: String,
        context: Context
    ) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            NotificationCompat.Builder(context, channelId)
        } else {
            NotificationCompat.Builder(context)
        }
        builder.setContentTitle(context.getString(title)).setContentText(context.getString(text))
            .setSmallIcon(R.drawable.ic_notification_normal)
            .setContentIntent(contentIntent)

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
            when (channelId) {
                CHANNEL_ID_EXPOSURE -> REQ_ID_EXPOSURE
                CHANNEL_ID_OUTDATED_DATA -> REQ_ID_OUTDATED_DATA
                CHANNEL_ID_NOT_RUNNING -> REQ_ID_NOT_RUNNING
                else -> 0
            }, builder.build()
        )
    }

    companion object {
        const val CHANNEL_ID_EXPOSURE = "EXPOSURE"
        const val CHANNEL_ID_OUTDATED_DATA = "OUTDATED_DATA"
        const val CHANNEL_ID_NOT_RUNNING = "NOT_RUNNING"

        const val REQ_ID_EXPOSURE = 100
        const val REQ_ID_OUTDATED_DATA = 101
        const val REQ_ID_NOT_RUNNING = 102

        @RequiresApi(Build.VERSION_CODES.O)
        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                createNotificationChannel(
                    CHANNEL_ID_EXPOSURE,
                    context.getString(R.string.notification_channel_exposure),
                    NotificationManager.IMPORTANCE_MAX,
                    context
                )
                createNotificationChannel(
                    CHANNEL_ID_NOT_RUNNING,
                    context.getString(R.string.notification_channel_exposure_notifications_off),
                    NotificationManager.IMPORTANCE_DEFAULT,
                    context
                )
                createNotificationChannel(
                    CHANNEL_ID_OUTDATED_DATA,
                    context.getString(R.string.notification_channel_outdated_data),
                    NotificationManager.IMPORTANCE_DEFAULT,
                    context
                )
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            id: String,
            name: String,
            importance: Int,
            context: Context
        ): NotificationChannel {
            val channel = NotificationChannel(id, name, importance)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            return channel
        }
    }
}