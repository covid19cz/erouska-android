package cz.covid19cz.app.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import androidx.databinding.ObservableArrayList
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import cz.covid19cz.app.ui.sandbox.entity.ScanResultEntity
import io.reactivex.disposables.Disposable
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object BtUtils {

    val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")

    lateinit var btManager: BluetoothManager
    lateinit var btAdapter: BluetoothAdapter
    lateinit var rxBleClient: RxBleClient

    val scanResultsMap = HashMap<String, ScanResultEntity>()
    val scanResultsList = ObservableArrayList<ScanResultEntity>()
    private val serverCallback = BleServerCallback()

    var scanDisposable: Disposable? = null

    fun init(c: Context) {
        btManager = c.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        rxBleClient = RxBleClient.create(c)
    }

    fun hasBle(c: Context): Boolean {
        return c.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isBtEnabled(): Boolean {
        return btAdapter.isEnabled
    }

    fun startScan() {

        stopScan()

        scanDisposable = rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build(),
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(SERVICE_UUID))
                    .build()
            )
            .subscribe { scanResult ->
                onScanResult(scanResult)
            }
    }

    fun stopScan() {
        scanDisposable?.dispose()
        scanDisposable = null
        scanResultsMap.clear()
        scanResultsList.clear()
    }

    private fun onScanResult(result: ScanResult) {

        result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))?.let { data ->

            val deviceId = String(
                data,
                Charset.forName("utf-8")
            )

            if (!scanResultsMap.containsKey(deviceId)){
                val newEntity = ScanResultEntity(deviceId, result.bleDevice.macAddress)
                scanResultsList.add(newEntity)
                scanResultsMap[deviceId] = newEntity
                Log.d("New Device: ${deviceId}, RSSI = ${result.rssi}")
            }

            scanResultsMap[deviceId]?.let { entity ->
                entity.addRssi(result.rssi)
                Log.d("Updating: ${deviceId}, RSSI = ${result.rssi}")
            }
        }
    }

    fun isServerAvailable(): Boolean {
        return btAdapter.isMultipleAdvertisementSupported
    }

    fun startServer(deviceId: String, power : Int) {
        stopServer()

        Log.d("Starting server with power $power")

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(power)
            .build()

        val parcelUuid = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(parcelUuid)
            .build()

        /*val arr =  ByteArray(13)
        for (i in 0 .. 12) {
            arr[i] = 68.toByte()
        }*/

        val scanData = AdvertiseData.Builder()
            //.addServiceData(parcelUuid, arr).build()
            .addServiceData(parcelUuid, deviceId.toByteArray(Charset.forName("utf-8"))).build()

        btAdapter.bluetoothLeAdvertiser.startAdvertising(settings, data, scanData, serverCallback);

        //btManager.openGattServer(c, serverCallback).addService(service)
    }

    fun stopServer(){
        btAdapter.bluetoothLeAdvertiser.stopAdvertising(serverCallback)
    }

    private class BleServerCallback : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("Peripheral advertising started.")
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            Log.d("Peripheral advertising failed: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

}