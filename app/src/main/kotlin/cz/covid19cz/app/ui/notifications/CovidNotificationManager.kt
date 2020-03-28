package cz.covid19cz.app.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import cz.covid19cz.app.R
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.main.MainActivity

class CovidNotificationManager(private val service: CovidService) {

    companion object {
        const val SERVICE_CHANNEL_ID = "ForegroundServiceChannel"
        const val ALERT_CHANNEL_ID = "ForegroundServiceAlertChannel"
        const val NOTIFICATION_ID = 1

        fun hideAllNotifications(context: Context) {
            context.getSystemService<NotificationManager>()?.cancelAll()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    service.getString(R.string.foreground_service_channel),
                    NotificationManager.IMPORTANCE_LOW
                ),
                NotificationChannel(
                    ALERT_CHANNEL_ID,
                    service.getString(R.string.foreground_service_alert_channel),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { enableVibration(true) }
            )
            val manager = service.getSystemService<NotificationManager>()
            channels.forEach {
                manager?.createNotificationChannel(it)
            }
        }
    }

    fun postNotification(serviceStatus: ServiceStatus) {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(service, SERVICE_CHANNEL_ID)
        builder.setStyle(NotificationCompat.BigTextStyle())

        @StringRes val title: Int
        @StringRes val text: Int
        @DrawableRes val icon: Int
        @ColorRes val color: Int
        val notificationIntent = Intent(service, MainActivity::class.java)
        @StringRes val actionText: Int?
        val actionIntent: PendingIntent

        when {
            serviceStatus.paused -> {
                title = R.string.notification_title_error
                text = R.string.notification_text_paused
                icon = R.drawable.ic_notification_normal
                color = R.color.exposition_level_6
                actionIntent = CovidService.startService(service).wrapAsForegroundService()
                actionText = R.string.notification_action_resume
            }
            serviceStatus.batterySaverEnabled -> {
                builder.setChannelId(ALERT_CHANNEL_ID)
                title = R.string.notification_title_error
                text = R.string.notification_text_battery_saver_enabled
                icon = R.drawable.ic_notification_error
                color = R.color.red
                actionIntent = getBatterySaverSettingsIntent().wrapAsActivity()
                actionText = R.string.notification_action_disable_battery_saver
            }
            !serviceStatus.bluetoothEnabled -> {
                builder.setChannelId(ALERT_CHANNEL_ID)
                title = R.string.notification_title_error
                text = R.string.notification_text_bluetooth_disabled
                icon = R.drawable.ic_notification_error
                color = R.color.red
                actionIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).wrapAsActivity()
                actionText = R.string.notification_action_enable_bluetooth
            }
            !serviceStatus.locationEnabled -> {
                builder.setChannelId(ALERT_CHANNEL_ID)
                title = R.string.notification_title_error
                text = R.string.notification_text_location_disabled
                icon = R.drawable.ic_notification_error
                color = R.color.red
                actionIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).wrapAsActivity()
                actionText = R.string.notification_action_enable_location
            }
            else -> {
                title = R.string.notification_title
                text = R.string.notification_text_resumed
                icon = R.drawable.ic_notification_normal
                color = R.color.green
                actionIntent = CovidService.stopService(service).wrapAsService()
                actionText = R.string.notification_action_pause
            }
        }
        builder.addAction(0, service.getString(actionText), actionIntent)


        val notificationPendingIntent =
            PendingIntent.getActivity(service, 0, notificationIntent, 0)

        with(service) {
            builder.setContentTitle(getString(title))
                .setContentText(getString(text))
                .setSmallIcon(icon)
                .setColor(ContextCompat.getColor(this, color))
                .setContentIntent(notificationPendingIntent)
                .setOngoing(!serviceStatus.paused)
                .build()
                .run {
                    if (serviceStatus.paused) {
                        NotificationManagerCompat.from(service).notify(NOTIFICATION_ID, this)
                    } else {
                        startForeground(NOTIFICATION_ID, this)
                    }
                }
        }
    }

    private fun getBatterySaverSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
    }

    private fun Intent.wrapAsService() = PendingIntent.getService(service, 0, this, 0)

    private fun Intent.wrapAsForegroundService(): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(service, 0, this, 0)
        } else {
            wrapAsService()
        }
    }

    private fun Intent.wrapAsActivity() = PendingIntent.getActivity(service, 0, this, 0)

    data class ServiceStatus(
        val paused: Boolean,
        val bluetoothEnabled: Boolean,
        val locationEnabled: Boolean,
        val batterySaverEnabled: Boolean
    ) {
        fun isOk() = bluetoothEnabled && locationEnabled && !batterySaverEnabled
    }
}
