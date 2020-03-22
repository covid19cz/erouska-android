package cz.covid19cz.app.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
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

        @StringRes val title: Int
        @StringRes val text: Int
        @DrawableRes val icon: Int
        @ColorRes val color: Int
        val notificationIntent = Intent(service, MainActivity::class.java)

        if (serviceStatus.isOk()) {
            title = R.string.notification_title
            text = R.string.notification_text
            icon = R.drawable.ic_notification_normal
            color = R.color.green
        } else {
            title = R.string.notification_title_error
            icon = R.drawable.ic_notification_error
            color = R.color.red

            builder.setChannelId(ALERT_CHANNEL_ID)
            builder.setStyle(NotificationCompat.BigTextStyle())

            @StringRes val actionText: Int?
            val actionIntent: Intent

            when {
                serviceStatus.batterySaverEnabled -> {
                    text = R.string.notification_text_battery_saver_enabled
                    actionIntent = getBatterySaverSettingsIntent()
                    actionText = R.string.notification_action_disable_battery_saver
                }
                !serviceStatus.bluetoothEnabled -> {
                    text = R.string.notification_text_bluetooth_disabled
                    actionIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    actionText = R.string.notification_action_enable_bluetooth
                }
                else -> {
                    text = R.string.notification_text_location_disabled
                    actionIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    actionText = R.string.notification_action_enable_location
                }
            }
            val actionPendingIntent = PendingIntent.getActivity(service, 0, actionIntent, 0)
            builder.addAction(0, service.getString(actionText), actionPendingIntent)

        }

        val notificationPendingIntent =
            PendingIntent.getActivity(service, 0, notificationIntent, 0)

        with(service) {
            builder.setContentTitle(getString(title))
                .setContentText(getString(text))
                .setSmallIcon(icon)
                .setColor(ContextCompat.getColor(this, color))
                .setContentIntent(notificationPendingIntent)
                .build()
                .run { startForeground(NOTIFICATION_ID, this) }
        }
    }

    fun getBatterySaverSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
    }

    data class ServiceStatus(
        val bluetoothEnabled: Boolean,
        val locationEnabled: Boolean,
        val batterySaverEnabled: Boolean
    ) {
        fun isOk() = bluetoothEnabled && locationEnabled && !batterySaverEnabled
    }

}
