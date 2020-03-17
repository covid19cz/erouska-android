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
import cz.covid19cz.app.R
import cz.covid19cz.app.ui.main.MainActivity
import cz.covid19cz.app.utils.BtUtils


class BtTracingService : Service() {

    companion object{

        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val ARG_DEVICE_ID = "DEVICE_ID"
        const val ARG_POWER = "POWER"

        fun startService(c : Context, deviceId : String, power : Int) {
            val serviceIntent = Intent(c, BtTracingService::class.java)
            serviceIntent.putExtra(ARG_DEVICE_ID, deviceId)
            serviceIntent.putExtra(ARG_POWER, power)
            ContextCompat.startForegroundService(c, serviceIntent)
        }

        fun stopService(c : Context) {
            val serviceIntent = Intent(c, BtTracingService::class.java)
            c.stopService(serviceIntent)
        }
    }

    lateinit var deviceId : String
    var power : Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceId = intent?.getStringExtra(ARG_DEVICE_ID) ?: "Unknown Device ID"
        power = intent?.getIntExtra(ARG_POWER, 0) ?: 0

        createNotificationChannel();
        val notificationIntent = Intent(this, MainActivity::class.java)
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

    override fun onDestroy() {
        BtUtils.stopScan()
        BtUtils.stopServer()
        super.onDestroy()
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
        BtUtils.startServer(deviceId, power)
    }
}