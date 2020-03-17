package cz.covid19cz.app.utils

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


object BtUtils {

    val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")

    lateinit var btManager: BluetoothManager
    lateinit var btAdapter: BluetoothAdapter

    val scanResults = ArrayList<ScanResult>()
    private val clientCallback = BleClientCallback()
    private val serverCallback = BleServerCallback()
    var scanning = false
    val scanningHandler = Handler()

    fun init(c: Context) {
        btManager = c.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
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
        val filters = ArrayList<ScanFilter>()
        filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build())
        var settings: ScanSettings? = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        btAdapter.bluetoothLeScanner.startScan(filters, settings, clientCallback)
        scanning = true
        scanningHandler.postDelayed(this::stopScan, 20000)
    }

    private fun stopScan() {
        if (scanning && btAdapter.isEnabled) {
            btAdapter.bluetoothLeScanner.stopScan(clientCallback)
            onScanCompleted()
        }
        scanning = false
    }

    private fun onScanCompleted() {
        for (result in scanResults) {
            Log.d("Found device: ${result.device.address}")
        }
        scanResults.clear()
    }

    fun isServerAvailable() : Boolean{
        return btAdapter.isMultipleAdvertisementSupported
    }

    fun startServer(c : Context) {

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

    private class BleClientCallback : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (scanResult in scanResults) {
                addScanResult(scanResult)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE Scan Failed with code $errorCode");
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address

            var found = false
            for (item in scanResults) {
                if (item.device.address == deviceAddress) {
                    found = true
                    break
                }
            }

            if (!found) {
                scanResults.add(result)
                Log.d("New BLE Device found ${device.name}, TX Power = ${result.txPower}, RSSI = ${result.rssi}, Distance = " + String.format("%.4f", calculateDistance(result.txPower, result.rssi) * 10000))
                Log.d("Device ID ${String(result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))!!, Charset.forName("utf-8"))}")
            }
        }

        private fun calculateDistance(txPower: Int, rssi: Int): Double {
            if (rssi == 0) {
                return -1.0 // if we cannot determine distance, return -1.
            }
            val ratio = rssi * 1.0 / txPower
            return if (ratio < 1.0) {
                Math.pow(ratio, 10.0)
            } else {
                0.89976 * Math.pow(ratio, 7.7095) + 0.111
            }
        }
    }

    private class BleServerCallback : AdvertiseCallback(){

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