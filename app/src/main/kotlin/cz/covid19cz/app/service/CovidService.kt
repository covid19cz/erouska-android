package cz.covid19cz.app.service

import android.app.ActivityManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.*
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.receiver.BatterSaverStateReceiver
import cz.covid19cz.app.receiver.BluetoothStateReceiver
import cz.covid19cz.app.receiver.LocationStateReceiver
import cz.covid19cz.app.receiver.ScreenStateReceiver
import cz.covid19cz.app.ui.notifications.CovidNotificationManager
import cz.covid19cz.app.utils.L
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class CovidService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_SCREEN_STATE_CHANGE = "ACTION_SCREEN_STATE_CHANGE"

        const val ACTION_MASK_STARTED = "action_service_started"
        const val ACTION_MASK_STOPPED = "action_service_stopped"

        const val EXTRA_SCREEN_STATE = "SCREEN_STATE"

        fun startService(c: Context): Intent {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_START
            return serviceIntent
        }

        fun stopService(c: Context): Intent {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_STOP
            return serviceIntent
        }

        fun update(c: Context) {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_UPDATE
            c.startService(serviceIntent)
        }

        fun pause(c: Context): Intent {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_PAUSE
            return serviceIntent
        }

        fun resume(c: Context): Intent {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_RESUME
            return serviceIntent
        }

        fun screenStateChange(c: Context, newState: String) {
            val intent = Intent(c, CovidService::class.java)
            intent.action = ACTION_SCREEN_STATE_CHANGE
            intent.putExtra(EXTRA_SCREEN_STATE, newState)
            c.startService(intent)
        }

        fun isRunning(context: Context): Boolean {
            val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
                if (CovidService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }

    private val locationStateReceiver by inject<LocationStateReceiver>()
    private val bluetoothStateReceiver by inject<BluetoothStateReceiver>()
    private val batterySaverStateReceiver by inject<BatterSaverStateReceiver>()
    private val screenStateReceiver by inject<ScreenStateReceiver>()
    private val btUtils by inject<BluetoothRepository>()
    private val prefs by inject<SharedPrefsRepository>()
    private val wakeLockManager by inject<WakeLockManager>()
    private val powerManager by inject<PowerManager>()
    private val localBroadcastManager by inject<LocalBroadcastManager>()
    private val notificationManager = CovidNotificationManager(this)

    private var bleAdvertisingDisposable: Disposable? = null
    private var bleScanningDisposable: Disposable? = null

    private lateinit var deviceBuid: String
    private var useScanFilter: Boolean = false
    private var servicePaused = false

    private var screenOfDetectionTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        deviceBuid = prefs.getDeviceBuid() ?: "00000000000000000000"
        subscribeToReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // null intent is in case service is restarted by system
            ACTION_START, null -> {
                servicePaused = false
                prefs.setAppPaused(false)
                createNotification()
                turnMaskOn()
                wakeLockManager.acquire()
            }
            ACTION_STOP -> {
                wakeLockManager.release()
                servicePaused = true
                prefs.setAppPaused(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    stopForeground(true)
                }
                stopSelf()
                createNotification()
            }
            ACTION_UPDATE -> {
                createNotification()
                if (isLocationEnabled() && btUtils.isBtEnabled()) {
                    turnMaskOn()
                } else {
                    turnMaskOff()
                }
            }
            ACTION_PAUSE -> {
                servicePaused = true
                prefs.setAppPaused(true)
                createNotification()
                turnMaskOff()
            }
            ACTION_RESUME -> {
                servicePaused = false
                prefs.setAppPaused(false)
                createNotification()
                turnMaskOn()
            }
            ACTION_SCREEN_STATE_CHANGE -> {
                L.d("Screen state change: ${intent.getStringExtra(EXTRA_SCREEN_STATE)}")

                when (intent.getStringExtra(EXTRA_SCREEN_STATE)) {
                    Intent.ACTION_SCREEN_OFF -> startScreenOffScanningCheck()
                    Intent.ACTION_SCREEN_ON -> screenOfDetectionTimer?.cancel()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        wakeLockManager.release()
        turnMaskOff()
        unsubscribeFromReceivers()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun turnMaskOn() {
        if (isLocationEnabled() && btUtils.isBtEnabled()) {
            localBroadcastManager.sendBroadcast(Intent(ACTION_MASK_STARTED))
            startBleAdvertising()
            startBleScanning()
        } else {
            turnMaskOff()
        }
    }

    private fun turnMaskOff() {
        localBroadcastManager.sendBroadcast(Intent(ACTION_MASK_STOPPED))
        btUtils.stopScanning()
        btUtils.stopAdvertising()

        bleScanningDisposable?.dispose()
        bleScanningDisposable = null
        bleAdvertisingDisposable?.dispose()
        bleAdvertisingDisposable = null
    }

    private fun createNotification() {
        notificationManager.postNotification(
            CovidNotificationManager.ServiceStatus(
                servicePaused,
                btUtils.isBtEnabled(),
                isLocationEnabled(),
                batterySaverRestrictsLocation()
            )
        )
    }

    private fun subscribeToReceivers() {
        val locationFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationStateReceiver, locationFilter)

        val btFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, btFilter)

        val batterySaverFilter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        registerReceiver(batterySaverStateReceiver, batterySaverFilter)

        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenStateReceiver, screenStateFilter)
    }

    private fun unsubscribeFromReceivers() {
        unregisterReceiver(locationStateReceiver)
        unregisterReceiver(bluetoothStateReceiver)
        unregisterReceiver(batterySaverStateReceiver)
        unregisterReceiver(screenStateReceiver)
    }

    private fun startBleScanning() {
        bleScanningDisposable = Observable.just(true)
            .map {
                if (btUtils.isBtEnabled() && isLocationEnabled()) {
                    btUtils.startScanning(useScanFilter)
                } else {
                    bleScanningDisposable?.dispose()
                }
            }
            .delay(AppConfig.collectionSeconds, TimeUnit.SECONDS)
            .map {
                btUtils.stopScanning()
                screenOfDetectionTimer?.cancel()
            }
            .delay(AppConfig.waitingSeconds, TimeUnit.SECONDS)
            .repeat()
            .execute(
                { L.d("Restarting BLE scanning") },
                { L.e(it) }
            )
    }

    private fun startBleAdvertising() {
        bleAdvertisingDisposable = Observable.just(true)
            .map {
                if (btUtils.isBtEnabled()) {
                    btUtils.startAdvertising(deviceBuid)
                } else {
                    bleAdvertisingDisposable?.dispose()
                }
            }
            .delay(AppConfig.advertiseRestartMinutes, TimeUnit.MINUTES)
            .map { btUtils.stopAdvertising() }
            .repeat()
            .execute(
                { L.d("Restarting BLE advertising") },
                { L.e(it) }
            )
    }

    private fun batterySaverRestrictsLocation(): Boolean {
        return powerManager.isPowerSaveMode && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            powerManager.locationPowerSaveMode == PowerManager.LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF
        } else {
            true
        }
    }

    /**
     * Starts regular check to ensure we're reading data when the screen is off.
     */
    private fun startScreenOffScanningCheck() {
        Handler(Looper.getMainLooper()).post {
            val switchedOffSince = System.currentTimeMillis()

            /**
             * Wait up-to 30s, try every 10 seconds.
             * If the check passes, this timer is stopped.
             * If onFinish is invoked, it means the test didn't passed => we have to restart scanning.
             */
            screenOfDetectionTimer =
                object : CountDownTimer(30 * 1000, 10 * 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        btUtils.lastScanResultTime.value?.let { lastResultTime ->
                            L.d("Checking if it's still scanning")
                            if (lastResultTime > switchedOffSince + 1000) { // 1s tolerance
                                // Device does support scanning on background with empty filter
                                screenOfDetectionTimer?.cancel() // cancel this check, it's not needed anymore
                            }
                        }
                    }

                    override fun onFinish() {
                        L.d("Device does NOT support scanning on background with empty filter, restarting")
                        useScanFilter = true
                        btUtils.stopScanning()
                        btUtils.startScanning(useScanFilter = true)
                    }
                }.start()
        }
    }
}