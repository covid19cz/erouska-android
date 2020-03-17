package cz.covid19cz.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cz.covid19cz.app.BtTracingApplication
import cz.covid19cz.app.R
import cz.covid19cz.app.ui.dash.DashActivity
import cz.covid19cz.app.utils.BtUtils
import okhttp3.internal.Internal.instance
import org.kodein.di.Kodein
import org.kodein.di.LazyKodein
import org.kodein.di.android.closestKodein
import org.kodein.di.erased.instance


class BtTracingService : Service() {

    val CHANNEL_ID = "ForegroundServiceChannel"

    companion object{

        fun startService(c : Context) {
            val serviceIntent = Intent(c, BtTracingService::class.java)
            ContextCompat.startForegroundService(c, serviceIntent)
        }

        fun stopService(c : Context) {
            val serviceIntent = Intent(c, BtTracingService::class.java)
            c.stopService(serviceIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel();
        val notificationIntent = Intent(this, DashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
        0, notificationIntent, 0);
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build();
        startForeground(1, notification);

        startBleClient()
        startBleServer()
        return START_NOT_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startBleClient() {
        BtUtils.startScan()
    }

    private fun startBleServer(){
        BtUtils.startServer(this)
    }
}