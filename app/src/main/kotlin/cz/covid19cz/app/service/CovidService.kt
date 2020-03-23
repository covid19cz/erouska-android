package cz.covid19cz.app.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.*
import androidx.core.content.ContextCompat
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.ext.isLocationEnabled
import cz.covid19cz.app.receiver.BatterSaverStateReceiver
import cz.covid19cz.app.receiver.BluetoothStateReceiver
import cz.covid19cz.app.receiver.LocationStateReceiver
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

        fun update(c: Context) {
            val stopIntent = Intent(c, CovidService::class.java)
            stopIntent.action = ACTION_UPDATE
            c.startService(stopIntent)
        }
    }

    private val locationStateReceiver by inject<LocationStateReceiver>()
    private val bluetoothStateReceiver by inject<BluetoothStateReceiver>()
    private val batterySaverStateReceiver by inject<BatterSaverStateReceiver>()
    private val btUtils by inject<BluetoothRepository>()
    private val prefs by inject<SharedPrefsRepository>()
    private val wakeLockManager by inject<WakeLockManager>()
    private val powerManager by inject<PowerManager>()
    private val notificationManager = CovidNotificationManager(this)

    private var bleAdvertisingDisposable: Disposable? = null
    private var bleScanningDisposable: Disposable? = null

    private lateinit var deviceBuid: String

    override fun onCreate() {
        super.onCreate()
        deviceBuid = prefs.getDeviceBuid() ?: "00000000000000000000"
        subscribeToReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // null intent is in case service is restarted by system
            ACTION_START, null -> {
                createNotification()
                turnMaskOn()
                wakeLockManager.acquire()
            }
            ACTION_STOP -> {
                wakeLockManager.release()
                btUtils.stopScanning()
                stopForeground(true)
                stopSelf()
            }
            ACTION_UPDATE -> {
                createNotification()
                if (isLocationEnabled() && btUtils.isBtEnabled()) {
                    turnMaskOn()
                } else {
                    turnMaskOff()
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
        startBleAdvertising()
        startBleScanning()
    }

    private fun turnMaskOff() {
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
            .map {
                if (btUtils.isBtEnabled() && isLocationEnabled()) {
                    btUtils.startScanning()
                } else {
                    bleScanningDisposable?.dispose()
                }
            }
            .delay(AppConfig.collectionSeconds, TimeUnit.SECONDS)
            .map {
                btUtils.stopScanning()
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
}