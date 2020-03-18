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
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.db.ExpositionEntity
import cz.covid19cz.app.db.ExpositionRepository
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.ui.main.MainActivity
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.utils.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class CovidService : Service() {

    companion object {

        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val ARG_DEVICE_ID = "DEVICE_ID"
        const val ARG_POWER = "POWER"

        fun startService(c: Context, deviceId: String, power: Int = AppConfig.advertiseTxPower) {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.putExtra(ARG_DEVICE_ID, deviceId)
            serviceIntent.putExtra(ARG_POWER, power)
            ContextCompat.startForegroundService(c, serviceIntent)
        }

        fun stopService(c: Context) {
            val serviceIntent = Intent(c, CovidService::class.java)
            c.stopService(serviceIntent)
        }
    }

    lateinit var deviceId: String
    var power: Int = 0
    val btUtils by inject<BluetoothRepository>()
    val db by inject<ExpositionRepository>()
    var scanDisposable: Disposable? = null
    var saveDataDisposable: Disposable? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceId = intent?.getStringExtra(ARG_DEVICE_ID) ?: "Unknown Device ID"
        power = intent?.getIntExtra(ARG_POWER, -1) ?: -1

        createNotificationChannel();
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        );
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build();
        startForeground(1, notification);

        startBleServer()
        startBleClient()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        btUtils.stopScan()
        btUtils.stopServer()
        saveDataDisposable?.dispose()
        saveDataDisposable = null
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

    private fun startBleServer() {
        if (btUtils.isServerAvailable()) {
            if (power != -1) {
                btUtils.startServer(deviceId, power)
            } else {
                btUtils.startServer(deviceId, AppConfig.advertiseTxPower)
            }
        }
    }

    fun startBleClient() {
        saveDataDisposable = Observable.just(true)
            //Give IU Thread time to clean data
            .delay(1, TimeUnit.SECONDS)
            .map {
                Log.d("Start scanning")
                btUtils.startScan()
                return@map it
            }.delay(AppConfig.collectionSeconds, TimeUnit.SECONDS)
            .map {
                Log.d("Stop scanning")
                btUtils.stopScan()
                Log.d("Save data to database")
                val rowCount = saveData()
                Log.d("$rowCount rows saved")
                return@map it
            }.delay(AppConfig.waitingSeconds, TimeUnit.SECONDS)
            .repeat().execute({
                Log.d("Clean scan data")
                btUtils.clear()
            }, {
                Log.e(it)
            })
    }

    fun saveData(): Int {
        val tempArray = btUtils.scanResultsList.toTypedArray()
        for (item in tempArray) {
            item.calculate()
            val rowId = db.add(
                ExpositionEntity(
                    0,
                    item.deviceId,
                    item.timestampStart,
                    item.timestampEnd,
                    item.minRssi,
                    item.maxRssi,
                    item.avgRssi,
                    item.medRssi
                )
            )
            Log.d("DB: Inserted row $rowId")
        }
        return tempArray.size
    }
}