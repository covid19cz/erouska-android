package cz.covid19cz.app.bt

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.databinding.ObservableArrayList
import arch.livedata.SafeMutableLiveData
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.bt.entity.ScanSession
import cz.covid19cz.app.utils.Log
import io.reactivex.disposables.Disposable
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap


class BluetoothRepository(context: Context) {

    val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")

    private val btManager: BluetoothManager
    val rxBleClient: RxBleClient

    val scanResultsMap = HashMap<String, ScanSession>()
    val scanResultsList = ObservableArrayList<ScanSession>()

    private val serverCallback = object: AdvertiseCallback(){
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                isAdvertising = true
                Log.d("BLE advertising started.")
                super.onStartSuccess(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                isAdvertising = false
                Log.d("BLE advertising failed: $errorCode")
                super.onStartFailure(errorCode)
            }
    }

    var isAdvertising = false
    var isScanning = false

    var scanDisposable: Disposable? = null

    init {
        btManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        rxBleClient = RxBleClient.create(context)
    }

    fun hasBle(c: Context): Boolean {
        return c.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isBtEnabled(): Boolean {
        return btManager.adapter?.isEnabled ?: false
    }

    fun enableBt() {
        btManager.adapter?.enable()
    }

    fun ensureBtEnabled(){
        if (!isBtEnabled()){
            enableBt()
        }
    }

    fun startScanning() {
        if (isScanning) {
            stopScanning()
        }

        ensureBtEnabled()

        Log.d("Starting BLE scanning in mode: ${AppConfig.scanMode}")
        scanDisposable = rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                    .setScanMode(AppConfig.scanMode).build(),
                // Empty Scan filter needed for background scanning since Android 8.1
                ScanFilter.Builder().build()
            )
            .subscribe ({ scanResult ->
                onScanResult(scanResult)
            }, {
                isScanning = false
                Log.e(it)})
        isScanning = true
    }

    fun stopScanning() {
        isScanning = false
        Log.d("Stopping BLE scanning")
        scanDisposable?.dispose()
        scanDisposable = null
    }

    private fun onScanResult(result: ScanResult) {

        Log.d("Scan result: ${result.bleDevice.name} (${result.bleDevice.macAddress})")

        result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))?.let { data ->

            val deviceId = String(
                data,
                Charset.forName("utf-8")
            )

            if (!scanResultsMap.containsKey(deviceId)) {
                val newEntity = ScanSession(deviceId, result.bleDevice.macAddress)
                scanResultsList.add(newEntity)
                scanResultsMap[deviceId] = newEntity
                Log.d("New Device: ${deviceId}, RSSI = ${result.rssi}")
            }

            scanResultsMap[deviceId]?.let { entity ->
                entity.addRssi(result.rssi)
                Log.d("Known Device:: ${deviceId}, RSSI = ${result.rssi}")
            }
        }
    }

    fun clearScanResults() {
        scanResultsList.clear()
        scanResultsMap.clear()
    }

    fun isServerAvailable(): Boolean {
        return btManager.adapter?.isMultipleAdvertisementSupported ?: false
    }

    fun startServer(deviceId: String, power: Int) {
        if (isAdvertising) {
            stopServer()
        }

        ensureBtEnabled()

        Log.d("Starting BLE advertising with power $power")

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AppConfig.advertiseMode)
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

        btManager.adapter?.bluetoothLeAdvertiser?.startAdvertising(
            settings,
            data,
            scanData,
            serverCallback
        );

        //btManager.openGattServer(c, serverCallback).addService(service)
    }

    fun stopServer() {
        Log.d("Stopping BLE advertising")
        btManager.adapter?.bluetoothLeAdvertiser?.stopAdvertising(serverCallback)
    }




}