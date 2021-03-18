package cz.covid19cz.erouska.exposurenotifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.isNetworkAvailable
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.ui.main.MainActivity
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notifications @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPrefsRepository,
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository
) {

    companion object {
        const val CHANNEL_ID_EXPOSURE = "EXPOSURE"
        const val CHANNEL_ID_OUTDATED_DATA = "OUTDATED_DATA"
        const val CHANNEL_ID_NOT_RUNNING = "NOT_RUNNING"
        const val CHANNEL_ID_DOWNLOADING = "DOWNLOADING"

        const val REQ_ID_EXPOSURE = 100
        const val REQ_ID_OUTDATED_DATA = 101
        const val REQ_ID_NOT_RUNNING = 102
        const val REQ_ID_DOWNLOADING = 103
    }

    fun showErouskaPausedNotification() {
        showNotification(
            R.string.dashboard_title_paused,
            R.string.notification_exposure_notifications_off_text,
            CHANNEL_ID_NOT_RUNNING
        )
    }

    fun showRiskyExposureNotification() {
        showNotification(
            R.string.notification_exposure_title,
            R.string.notification_exposure_text,
            CHANNEL_ID_EXPOSURE,
            autoCancel = true,
            color = Color.RED,
            priority = PRIORITY_MAX
        )
    }

    fun showOutdatedDataNotification() {
        showNotification(
            context.getString(R.string.notification_data_outdated_title),
            AppConfig.recentExposureNotificationTitle,
            CHANNEL_ID_OUTDATED_DATA
        )

    }

    private fun showNotification(
        @StringRes title: Int,
        @StringRes text: Int,
        channelId: String,
        autoCancel: Boolean = false,
        color: Int? = null,
        priority: Int? = null
    ) {
        showNotification(
            context.getString(title),
            context.getString(text),
            channelId,
            autoCancel,
            color,
            priority
        )
    }

    private fun showNotification(
        title: String,
        text: String,
        channelId: String,
        autoCancel: Boolean = false,
        color: Int? = null,
        priority: Int? = null
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification_normal)
            .setContentIntent(getContentIntent())
            .setAutoCancel(autoCancel)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
                    .setBigContentTitle(title)
            )

        color?.let {
            builder.setColorized(true)
            builder.color = color
        }
        priority?.let {
            builder.priority = priority
        }

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
            when (channelId) {
                CHANNEL_ID_EXPOSURE -> REQ_ID_EXPOSURE
                CHANNEL_ID_OUTDATED_DATA -> REQ_ID_OUTDATED_DATA
                CHANNEL_ID_NOT_RUNNING -> REQ_ID_NOT_RUNNING
                else -> 0
            }, builder.build()
        )
    }

    fun dismissNotRunningNotification() {
        dismissNotification(REQ_ID_NOT_RUNNING)
    }

    fun dismissOudatedDataNotification() {
        dismissNotification(REQ_ID_OUTDATED_DATA)
    }

    fun getDownloadingNotification(workId: UUID): Notification {
        val cancelIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workId)
        return NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOADING)
            .setContentTitle(context.getString(R.string.notification_downloading_title))
            .setContentText(context.getString(R.string.notification_downloading_description))
            .setContentIntent(getContentIntent())
            .setSmallIcon(R.drawable.ic_notification_normal)
            .addAction(
                android.R.drawable.ic_delete,
                context.getString(android.R.string.cancel),
                cancelIntent
            )
            .setOngoing(true)
            .build()
    }

    suspend fun getCurrentPushToken(): String {
        val pushToken = FirebaseMessaging.getInstance().token.await()
        L.d("Push token=$pushToken")
        return pushToken
    }

    private fun dismissNotification(id: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            id
        )
    }

    private fun getContentIntent(): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        return PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            createNotificationChannel(
                CHANNEL_ID_DOWNLOADING,
                context.getString(R.string.notification_channel_downloading),
                NotificationManager.IMPORTANCE_MIN,
                context
            )
        }
        if (!prefs.isPushTokenRegistered() && context.isNetworkAvailable() && FirebaseAuth.getInstance().currentUser != null) {
            GlobalScope.launch {
                try {
                    firebaseFunctionsRepository.changePushToken(getCurrentPushToken())
                } catch (e: Throwable) {
                    L.e(e)
                }
            }
        }
        if (!prefs.isPushTopicRegistered() && context.isNetworkAvailable()) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    Firebase.messaging.subscribeToTopic("budicek").await()
                    prefs.setPushTopicRegistered()
                    L.d("Topic 'budicek' registered")
                } catch (e: Throwable) {
                    L.e(e)
                }
            }
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