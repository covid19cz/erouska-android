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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.bt.BluetoothRepository
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.execute
import cz.covid19cz.erouska.ext.isLocationEnabled
import cz.covid19cz.erouska.receiver.BatterSaverStateReceiver
import cz.covid19cz.erouska.receiver.BluetoothStateReceiver
import cz.covid19cz.erouska.receiver.LocationStateReceiver
import cz.covid19cz.erouska.ui.notifications.CovidNotificationManager
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

        const val EXTRA_HIDE_NOTIFICATION = "HIDE_NOTIFICATION"
        const val EXTRA_CLEAR_DATA = "CLEAR_DATA"

        fun startService(c: Context): Intent {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_START
            return serviceIntent
        }

        fun stopService(
            c: Context,
            hideNotification: Boolean = false,
            clearData: Boolean = false
        ): Intent {
            val serviceIntent = Intent(c, CovidService::class.java)
            serviceIntent.action = ACTION_STOP
            serviceIntent.putExtra(EXTRA_HIDE_NOTIFICATION, hideNotification)
            serviceIntent.putExtra(EXTRA_CLEAR_DATA, clearData)
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

    private lateinit var deviceBuid: String
    private var servicePaused = false

    override fun onCreate() {
        super.onCreate()
        deviceBuid = prefs.getDeviceBuid() ?: "00000000000000000000"
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
        } else {
            prefs.setAppPaused(true)
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
    }

    private fun unsubscribeFromReceivers() {
        unregisterReceiver(locationStateReceiver)
        unregisterReceiver(bluetoothStateReceiver)
        unregisterReceiver(batterySaverStateReceiver)
    }

    private fun startBleScanning() {
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
        bleAdvertisingDisposable = Observable.just(true)
            .doOnNext {
                if (btUtils.isBtEnabled()) {
                    btUtils.startAdvertising(deviceBuid)
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

    private fun batterySaverRestrictsLocation(): Boolean {
        return powerManager.isPowerSaveMode && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            powerManager.locationPowerSaveMode == PowerManager.LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF
        } else {
            true
        }
    }
}