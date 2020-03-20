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
import android.os.Looper
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
        const val SERVICE_STATE_CHECK_PERIOD : Long = 2000

        private val handler = android.os.Handler(Looper.getMainLooper())

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
    var serviceState: ServiceState? = null
        set(value) {
            val oldState = field
            if (field != value) {
                Log.d("service state = $value")
                field = value
                onNewServiceState(oldState)
                // TODO: broadcast/notify listeners that we have a new state (make visual adjustments to persistent notification)
            }
        }
    private var wakeLock: PowerManager.WakeLock? = null

    // runnable which switches state of the service based on current conditions
    private val checkStateRunnable = object : Runnable {
        override fun run() {
            if (serviceState != ServiceState.STOPPED) {
                // check in which state we are
                if (btUtils.isBtEnabled()) {
                    if (serviceState == ServiceState.RUNNING_NO_EFFECT) {
                        // do state transition
                        serviceState = ServiceState.RUNNING
                    } // else: OK
                } else {
                    // bluetooth is not enabled
                    if (serviceState == ServiceState.RUNNING) {
                        // do state transition
                        serviceState = ServiceState.RUNNING_NO_EFFECT
                    }
                }
                // reschedule ourselves
                handler.postDelayed(this, SERVICE_STATE_CHECK_PERIOD)
            }
        }
    }

    enum class ServiceState {
        STOPPED, RUNNING, RUNNING_NO_EFFECT
    }

    override fun onCreate() {
        super.onCreate()
        serviceState = ServiceState.STOPPED
        deviceBuid = prefs.getDeviceBuid() ?: "UNREGISTERED"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // null intent is in case service is restarted by system
            ACTION_START, null -> {
                createNotificationChannel()
                val notificationIntent = Intent(this, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0, notificationIntent, 0
                )
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .build()
                startForeground(1, notification)
                //
                serviceState = ServiceState.RUNNING
                // start service state checker
                handler.post(checkStateRunnable)
            }
            ACTION_STOP -> {
                stopForeground(true)
                serviceState = ServiceState.STOPPED
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceState = ServiceState.STOPPED
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun onNewServiceState(prevState: ServiceState?) {
        when (serviceState) {
            ServiceState.RUNNING ->
                // start the BLE machinery
                startBleMachinery()
            ServiceState.RUNNING_NO_EFFECT ->
                // stop the BLE machinery
                stopBleMachinery()
            ServiceState.STOPPED ->
                // stop the BLE machinery
                if (prevState == ServiceState.RUNNING) stopBleMachinery()
        }
    }

    private fun startBleMachinery() {
        startBleAdvertising()
        startBleScanning()
        wakeLock(true)
    }


    private fun stopBleMachinery() {
        btUtils.stopAdvertising()
        btUtils.stopScanning()
        bleScanningDisposable?.dispose()
        bleScanningDisposable = null
        bleAdvertisingDisposable?.dispose()
        bleAdvertisingDisposable = null
        wakeLock(false)
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
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    fun startBleScanning() {
        bleScanningDisposable = Observable.just(true)
            //Give IU Thread time to clean data
            .delay(1, TimeUnit.SECONDS)
            .map {
                btUtils.startScanning()
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
                btUtils.startAdvertising(deviceBuid, AppConfig.advertiseTxPower)
            }
            .execute({
                Log.d("Starting BLE advertising")
            }, {
                Log.e(it)
            })
    }

    fun saveData() {
        Log.d("Saving data to database")
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
        Log.d("${tempArray.size} records saved")
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