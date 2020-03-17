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
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


object BtUtils {

    val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")

    lateinit var btManager: BluetoothManager
    lateinit var btAdapter: BluetoothAdapter
    lateinit var rxBleClient: RxBleClient

    val scanResults = ArrayList<ScanResult>()
    private val serverCallback = BleServerCallback()
    var scanning = false
    val scanningHandler = Handler()

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

    fun enable() {
        btAdapter.enable()
    }

    fun disable() {
        btAdapter.disable()
    }

    fun startScan() {

        stopScan()

        scanDisposable = rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build(),
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(SERVICE_UUID))
                    .build()
            )
            .subscribe { scanResult ->
                onScanResult(scanResult)
            }
    }

    private fun stopScan() {
        scanDisposable?.dispose()
        scanDisposable = null
    }

    private fun onScanResult(result: ScanResult) {
        val device = result.bleDevice
        val deviceAddress = device.macAddress

        var found = false
        for (item in scanResults) {
            if (item.bleDevice.macAddress == deviceAddress) {
                found = true
                break
            }
        }

        if (!found) {
            scanResults.add(result)
            Log.d("New BLE Device found ${device.name}, RSSI = ${result.rssi}")
            Log.d(
                "Device ID ${String(
                    result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))!!,
                    Charset.forName("utf-8")
                )}"
            )
        }
    }

    fun isServerAvailable(): Boolean {
        return btAdapter.isMultipleAdvertisementSupported
    }

    fun startServer(c: Context) {

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()

        val parcelUuid = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(parcelUuid)
            .build()

        val scanData = AdvertiseData.Builder()
            .addServiceData(parcelUuid, "Hello World".toByteArray(Charset.forName("utf-8"))).build()

        btAdapter.bluetoothLeAdvertiser.startAdvertising(settings, data, scanData, serverCallback);

        //btManager.openGattServer(c, serverCallback).addService(service)
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