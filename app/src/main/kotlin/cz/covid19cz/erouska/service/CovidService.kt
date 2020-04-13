package cz.covid19cz.erouska.service

import android.app.ActivityManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.bt.BluetoothRepository
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.batterySaverRestrictsLocation
import cz.covid19cz.erouska.ext.execute
import cz.covid19cz.erouska.ext.isLocationEnabled
import cz.covid19cz.erouska.receiver.BatterSaverStateReceiver
import cz.covid19cz.erouska.receiver.BluetoothStateReceiver
import cz.covid19cz.erouska.receiver.LocationStateReceiver
import cz.covid19cz.erouska.ui.notifications.CovidNotificationManager
import cz.covid19cz.erouska.utils.BatteryOptimization
import cz.covid19cz.erouska.utils.L
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

        const val ACTION_MASK_STARTED = "action_service_started"
        const val ACTION_MASK_STOPPED = "action_service_stopped"
        const val ACTION_TUID_ROTATED = "action_tuid_rotated"


        const val EXTRA_HIDE_NOTIFICATION = "HIDE_NOTIFICATION"
        const val EXTRA_CLEAR_DATA = "CLEAR_DATA"
        const val EXTRA_PERSIST_STATE = "PERSIST_STATE"
        const val EXTRA_TUID = "EXTRA_TUID"

        const val INVALID_TUID = "00000000000000000000"

        fun startService(context: Context): Intent {
            val serviceIntent = Intent(context, CovidService::class.java)
            serviceIntent.action = ACTION_START
            return serviceIntent
        }

        fun stopService(
            context: Context,
            hideNotification: Boolean = false,
            clearScanningData: Boolean = false,
            persistState: Boolean = true
        ): Intent {
            val serviceIntent = Intent(context, CovidService::class.java)
            serviceIntent.action = ACTION_STOP
            serviceIntent.putExtra(EXTRA_HIDE_NOTIFICATION, hideNotification)
            serviceIntent.putExtra(EXTRA_CLEAR_DATA, clearScanningData)
            serviceIntent.putExtra(EXTRA_PERSIST_STATE, persistState)
            return serviceIntent
        }

        fun update(context: Context) {
            val serviceIntent = Intent(context, CovidService::class.java)
            serviceIntent.action = ACTION_UPDATE
            context.startService(serviceIntent)
        }

        fun pause(context: Context): Intent {
            val serviceIntent = Intent(context, CovidService::class.java)
            serviceIntent.action = ACTION_PAUSE
            return serviceIntent
        }

        fun resume(context: Context): Intent {
            val serviceIntent = Intent(context, CovidService::class.java)
            serviceIntent.action = ACTION_RESUME
            return serviceIntent
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
    private val btUtils by inject<BluetoothRepository>()
    private val prefs by inject<SharedPrefsRepository>()
    private val wakeLockManager by inject<WakeLockManager>()
    private val powerManager by inject<PowerManager>()
    private val localBroadcastManager by inject<LocalBroadcastManager>()
    private val notificationManager = CovidNotificationManager(this)

    private var bleAdvertisingDisposable: Disposable? = null
    private var bleScanningDisposable: Disposable? = null

    private var servicePaused = false

    override fun onCreate() {
        super.onCreate()
        subscribeToReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // null intent is in case service is restarted by system
            ACTION_START, null -> start()
            ACTION_STOP -> stop(intent)
            ACTION_UPDATE -> update()
            ACTION_PAUSE -> pause()
            ACTION_RESUME -> resume()
        }
        return START_STICKY
    }

    private fun pause() {
        servicePaused = true
        prefs.setAppPaused(true)
        createNotification()
        turnMaskOff()
    }

    private fun update() {
        createNotification()
        if (isLocationEnabled() && btUtils.isBtEnabled()) {
            turnMaskOn()
        } else {
            turnMaskOff()
        }
    }

    private fun start() {
        resume()
        wakeLockManager.acquire()
    }

    private fun resume() {
        servicePaused = false
        prefs.setAppPaused(false)
        createNotification()
        turnMaskOn()
    }

    private fun stop(intent: Intent) {
        wakeLockManager.release()
        servicePaused = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        stopSelf()
        if (intent.getBooleanExtra(EXTRA_HIDE_NOTIFICATION, false)) {
            notificationManager.hideNotification(this)
        } else {
            createNotification()
        }
        if (intent.getBooleanExtra(EXTRA_CLEAR_DATA, false)) {
            btUtils.clearScanResults()
        }
        if (intent.getBooleanExtra(EXTRA_PERSIST_STATE, true)) {
            prefs.setAppPaused(true)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (BatteryOptimization.isMiUI()) {
            ContextCompat.startForegroundService(this, Companion.startService(this))
        }
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
            wakeLockManager.acquire()
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
        wakeLockManager.release()

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
                powerManager.batterySaverRestrictsLocation()
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
    }

    private fun unsubscribeFromReceivers() {
        unregisterReceiver(locationStateReceiver)
        unregisterReceiver(bluetoothStateReceiver)
        unregisterReceiver(batterySaverStateReceiver)
    }

    private fun startBleScanning() {
        bleScanningDisposable?.dispose()
        bleScanningDisposable = Observable.just(true)
            .doOnNext {
                if (btUtils.isBtEnabled() && isLocationEnabled()) {
                    btUtils.startScanning()
                } else {
                    bleScanningDisposable?.dispose()
                }
            }
            .delay(AppConfig.collectionSeconds, TimeUnit.SECONDS)
            .doOnNext {
                btUtils.stopScanning()
            }
            .delay(AppConfig.waitingSeconds, TimeUnit.SECONDS)
            .repeat()
            .execute(
                { L.d("Restarting BLE scanning") },
                {
                    pause(this)
                    L.e(it)
                }
            )
    }

    private fun startBleAdvertising() {
        bleAdvertisingDisposable?.dispose()
        bleAdvertisingDisposable = Observable.just(true)
            .doOnNext {
                if (btUtils.isBtEnabled()) {
                    val nextTuid = getNextTuid()
                    btUtils.startAdvertising(nextTuid)
                } else {
                    bleAdvertisingDisposable?.dispose()
                }
            }
            .delay(AppConfig.advertiseRestartMinutes, TimeUnit.MINUTES)
            .doOnNext { btUtils.stopAdvertising() }
            .repeat()
            .execute(
                { L.d("Restarting BLE advertising") },
                { L.e(it) }
            )
    }

    private fun getNextTuid(): String {
        return (prefs.getRandomTuid(prefs.getCurrentTuid()) ?: INVALID_TUID).also {
            prefs.setCurrentTuid(it)
            L.d("New broadcasted TUID: $it")
            localBroadcastManager.sendBroadcast(Intent(ACTION_TUID_ROTATED).apply {
                putExtra(
                    EXTRA_TUID,
                    it
                )
            })
        }
    }
}