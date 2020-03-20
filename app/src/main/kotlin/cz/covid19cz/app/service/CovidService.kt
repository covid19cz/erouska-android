package cz.covid19cz.app.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.ScanResultEntity
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.ui.main.MainActivity
import cz.covid19cz.app.utils.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class CovidService : Service() {

    companion object {

        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        fun startService(c: Context) {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_START
            ContextCompat.startForegroundService(c, serviceIntent)
        }

        fun stopService(c: Context) {
            val stopIntent = Intent(c, CovidService::class.java)
            stopIntent.action = ACTION_STOP
            c.startService(stopIntent)
        }
    }

    lateinit var deviceBuid: String
    val btUtils by inject<BluetoothRepository>()
    val db by inject<DatabaseRepository>()
    val prefs by inject<SharedPrefsRepository>()

    var bleAdvertisingDisposable: Disposable? = null
    var bleScanningDisposable: Disposable? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        deviceBuid = prefs.getDeviceBuid() ?: "UNREGISTERED"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // null intent is in case service is restarted by system
            ACTION_START, null -> {
                createNotification()

                startBleAdvertising()
                startBleScanning()
                wakeLock(true)
            }
            ACTION_STOP -> {
                wakeLock(false)
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    fun createNotification(){

        val btEnabled = btUtils.isBtEnabled()
        val locationEnabled = isLocationEnabled()

        Log.d("Bluetooth Enabled = $btEnabled")
        Log.d("Location Enabled = $btEnabled")

        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        var title = R.string.notification_title
        var text = R.string.notification_text
        var icon = R.drawable.ic_notification

        if (btEnabled && locationEnabled){
            title = R.string.notification_title
            text = R.string.notification_text
            icon = R.drawable.ic_notification
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(title))
            .setContentText(getString(text))
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .build();
        startForeground(1, notification)
    }

    override fun onDestroy() {
        btUtils.stopScanning()
        btUtils.stopAdvertising()
        bleScanningDisposable?.dispose()
        bleScanningDisposable = null
        bleAdvertisingDisposable?.dispose()
        bleAdvertisingDisposable = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.foreground_service_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun startBleScanning() {
        bleScanningDisposable = Observable.just(true)
            //Give IU Thread time to clean data
            .delay(1, TimeUnit.SECONDS)
            .map {
                if (btUtils.isBtEnabled()) {
                    btUtils.startScanning()
                } else {

                }
                return@map it
            }
            .delay(AppConfig.collectionSeconds, TimeUnit.SECONDS)
            .map {
                btUtils.stopScanning()
                saveData()
                return@map it
            }
            .delay(AppConfig.waitingSeconds, TimeUnit.SECONDS)
            .repeat()
            .execute({
                btUtils.clearScanResults()
            }, {
                Log.e(it)
            })
    }

    fun startBleAdvertising() {
        bleAdvertisingDisposable = Observable.just(true)
            .delay(1, TimeUnit.SECONDS)
            .map {
                btUtils.startAdvertising(deviceBuid)
            }
            .delay(AppConfig.advertiseRestartMinutes, TimeUnit.MINUTES)
            .map {
                btUtils.stopAdvertising()
            }
            .repeat()
            .execute({
                Log.i("Restarting BLE advertising")
            }, {
                Log.e(it)
            })
    }

    fun saveData() {
        Log.i("Saving data to database")
        val tempArray = btUtils.scanResultsList.toTypedArray()
        for (item in tempArray) {
            item.calculate()
            db.add(
                ScanResultEntity(
                    0,
                    item.deviceId,
                    item.timestampStart,
                    item.timestampEnd,
                    item.minRssi,
                    item.maxRssi,
                    item.avgRssi,
                    item.medRssi,
                    item.rssiCount
                )
            )
        }
        Log.i("${tempArray.size} records saved")
    }

    @SuppressLint("InvalidWakeLockTag", "WakelockTimeout")
    private fun wakeLock(enable: Boolean) {
        if (enable) {
            var tag = "$packageName:LOCK"

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && Build.MANUFACTURER == "Huawei") {
                tag = "LocationManagerService"
            }

            wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                tag
            )
            wakeLock?.let {
                if (!it.isHeld){
                    it.acquire()
                }
            }
        } else {
            wakeLock?.let {
                if (it.isHeld){
                    it.release()
                }
            }
        }
    }
}