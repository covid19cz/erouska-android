package cz.covid19cz.app.bt

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.bt.entity.ScanSession
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.ScanDataEntity
import cz.covid19cz.app.ext.asHexLower
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.ext.hexAsByteArray
import cz.covid19cz.app.utils.L
import cz.covid19cz.app.utils.isBluetoothEnabled
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class BluetoothRepository(
    val context: Context,
    private val db: DatabaseRepository,
    private val btManager: BluetoothManager
) {

    private val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")
    private val GATT_CHARACTERISTIC_UUID = UUID.fromString("9472fbde-04ff-4fff-be1c-b9d3287e8f28")

    val scanResultsMap = HashMap<String, ScanSession>()
    val discoveredIosDevices = HashMap<String, ScanSession>()
    val scanResultsList = ObservableArrayList<ScanSession>()

    var isAdvertising = false
    var isScanning = false
    var isScanningIosOnBackground = false

    //private var scanDisposable: Disposable? = null
    private var gattFailDisposable: Disposable? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            L.d("On scan result")
            onScanResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            L.d("Scan failed with error $errorCode")
            isScanning = false
            BluetoothLeScannerCompat.getScanner().stopScan(this)
            super.onScanFailed(errorCode)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            L.d("Batch scan result")
            results.forEach {
                onScanResult(it)
            }
            super.onBatchScanResults(results)
        }
    }

    private val scanIosOnBackgroundCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onScanIosOnBackgroundResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanningIosOnBackground = false
            BluetoothLeScannerCompat.getScanner().stopScan(this)
            super.onScanFailed(errorCode)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach {
                onScanIosOnBackgroundResult(it)
            }
            super.onBatchScanResults(results)
        }
    }

    private val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            L.d("BLE advertising started.")
            isAdvertising = true
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            L.e("BLE advertising failed: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                L.d("GATT connected")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (discoveredIosDevices[gatt.device.address]?.deviceId == ScanSession.DEFAULT_BUID) {
                    gattFailDisposable = Observable.timer(5, TimeUnit.SECONDS).subscribe {
                        // Unlock mac address slot for retry after 5 seconds (prevent DDOSing GATT server)
                        discoveredIosDevices.remove(gatt.device.address)
                        gattFailDisposable?.dispose()
                    }
                }

                L.d("GATT disconnected")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (GATT_CHARACTERISTIC_UUID == characteristic!!.uuid) {
                val buid = characteristic.value?.asHexLower
                val mac = gatt.device?.address

                if (buid != null) {
                    L.d("GATT BUID found")
                    discoveredIosDevices[mac]?.let { s ->
                        Observable.just(s).map { session ->
                            session.deviceId = buid
                            scanResultsMap[buid] = session
                            session
                        }.execute({
                            scanResultsList.add(it)
                        }, {
                            L.e(it)
                        })
                    }
                } else {
                    L.e("GATT BUID not found")
                }
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.close()
                return
            }
            val characteristic =
                gatt.getService(SERVICE_UUID)?.getCharacteristic(GATT_CHARACTERISTIC_UUID)
            if (characteristic != null) {
                L.d("GATT characteristic found")
                gatt.readCharacteristic(characteristic)
            } else {
                L.e("GATT characteristic not found")
                gatt.close()
            }
        }
    }

    fun hasBle(c: Context): Boolean {
        return c.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isBtEnabled(): Boolean {
        return btManager.isBluetoothEnabled()
    }

    val lastScanResultTime: MutableLiveData<Long> = MutableLiveData(0)

    fun startScanning() {
        if (isScanning) {
            stopScanning()
        }

        if (!isBtEnabled()) {
            L.d("Bluetooth disabled, can't start scanning")
            return
        }

        L.d("Starting BLE scanning in mode: ${AppConfig.scanMode}")

        val settings: ScanSettings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(AppConfig.scanMode)
            .setUseHardwareFilteringIfSupported(true)
            .build()

        BluetoothLeScannerCompat.getScanner().startScan(
            //listOf(ScanFilter.Builder().build(), ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()),
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()),
            settings,
            scanCallback
        )
        isScanningIosOnBackground = true

        BluetoothLeScannerCompat.getScanner().startScan(
            listOf(ScanFilter.Builder().build()),
            settings,
            scanIosOnBackgroundCallback
        )
        isScanning = true
    }

    fun stopScanning() {
        isScanning = false
        isScanningIosOnBackground = false
        L.d("Stopping BLE scanning")
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
        BluetoothLeScannerCompat.getScanner().stopScan(scanIosOnBackgroundCallback)
        saveDataAndClearScanResults()
    }

    private fun saveDataAndClearScanResults() {
        L.d("Saving data to database")
        Observable.just(scanResultsMap.values.toTypedArray())
            .map { tempArray ->
                for (item in tempArray) {
                    item.calculate()
                    val scanResult = ScanDataEntity(
                        0,
                        item.deviceId,
                        item.timestampStart,
                        item.timestampEnd,
                        item.avgRssi,
                        item.medRssi,
                        item.rssiCount
                    )
                    L.d("Saving: $scanResult")

                    db.add(scanResult)
                }

                tempArray.size
            }.execute({
                L.d("$it records saved")
                clearScanResults()
            }, { L.e(it) })
    }

    private fun onScanResult(result: ScanResult) {
        lastScanResultTime.value = System.currentTimeMillis()
        L.d("Scan 1 result")

        if (result.scanRecord?.bytes != null) {
            if (result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true || canBeIosOnBackground(result.scanRecord?.bytes!!)) {
                val deviceId = getBuidFromAdvertising(result.scanRecord?.bytes!!)

                if (deviceId != null) {
                    // It's time to handle Android Device
                    handleAndroidDevice(result, deviceId)
                } else {
                    // It's time to handle iOS Device
                    handleIosDevice(result)
                }
            }
        }
    }

    private fun onScanIosOnBackgroundResult(result: ScanResult) {
        if (result.scanRecord?.bytes != null) {
            if (result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) != true && canBeIosOnBackground(result.scanRecord?.bytes!!)) {
                // It's time to handle iOS Device in background
                handleIosDevice(result)
            }
        }
    }

    private fun canBeIosOnBackground(bytes: ByteArray): Boolean {
        return (bytes[bytes.size - 9] == 0x00.toByte() && bytes[bytes.size - 8] == 0x02.toByte() && bytes[bytes.size - 7] == 0x00.toByte())
    }

    private fun handleAndroidDevice(result: ScanResult, deviceId: String) {
        if (!scanResultsMap.containsKey(deviceId)) {
            val newEntity = ScanSession(deviceId, result.device.address)
            newEntity.addRssi(result.rssi)
            scanResultsList.add(newEntity)
            scanResultsMap[deviceId] = newEntity
            L.d("Found new Android: $deviceId")
        }

        scanResultsMap[deviceId]?.let { entity ->
            entity.addRssi(result.rssi)
            L.d("Device $deviceId - RSSI ${result.rssi}")
        }
    }

    private fun handleIosDevice(result: ScanResult) {
        if (!discoveredIosDevices.containsKey(result.device.address)) {
            L.d("Found new iOS")
            getBuidFromGatt(result)
        } else {
            discoveredIosDevices[result.device.address]?.let {
                if (it.deviceId != ScanSession.DEFAULT_BUID && !scanResultsMap.containsKey(it.deviceId)) {
                    scanResultsMap[it.deviceId] = it
                    scanResultsList.add(it)
                }
                it.addRssi(result.rssi)
                L.d("Device ${it.deviceId} - RSSI ${result.rssi}")
            }
            return
        }
    }

    private fun getBuidFromGatt(result: ScanResult) {
        val mac = result.device.address
        val session = ScanSession(mac = mac)
        session.addRssi(result.rssi)
        L.d("Connecting to GATT")
        discoveredIosDevices[mac] = session

        val device = btManager.adapter?.getRemoteDevice(mac)
        device?.connectGatt(context, false, gattCallback)
    }

    private fun getBuidFromAdvertising(bytes: ByteArray): String? {
        val result = ByteArray(10)

        var currIndex = 0
        var len = -1
        var type: Byte

        while (currIndex < bytes.size && len != 0) {
            len = bytes[currIndex].toInt()
            type = bytes[currIndex + 1]

            if (type == 0x21.toByte()) { //128 bit Service UUID (most cases)
                // +2 (skip lenght byte and type byte), +16 (skip 128 bit Service UUID)
                bytes.copyInto(result, 0, currIndex + 2 + 16, currIndex + 2 + 16 + 10)
                break
            } else if (type == 0x16.toByte()) { //16 bit Service UUID (rare cases)
                // +2 (skip lenght byte and type byte), +2 (skip 16 bit Service UUID)
                if (bytes.size > (currIndex + 2 + 2 + 10)) {
                    bytes.copyInto(result, 0, currIndex + 2 + 2, currIndex + 2 + 2 + 10)
                }
                break
            } else if (type == 0x20.toByte()) { //32 bit Service UUID (just in case)
                // +2 (skip lenght byte and type byte), +4 (skip 32 bit Service UUID)
                bytes.copyInto(result, 0, currIndex + 2 + 4, currIndex + 2 + 4 + 10)
                break
            } else {
                currIndex += len + 1
            }
        }

        val resultHex = result.asHexLower

        return if (resultHex != "00000000000000000000") resultHex else null
    }

    fun clearScanResults() {
        scanResultsList.clear()
        scanResultsMap.clear()
        clearIosDevices()
    }

    private fun clearIosDevices() {
        // Don't clear whole iOS device cache to preventing DDOS GATT
        for (device in discoveredIosDevices) {
            device.value.reset()
        }
    }

    fun supportsAdvertising(): Boolean {
        return btManager.adapter?.isMultipleAdvertisementSupported ?: false
    }

    fun startAdvertising(buid: String) {

        val power = AppConfig.advertiseTxPower

        if (isAdvertising) {
            stopAdvertising()
        }

        if (!isBtEnabled()) {
            L.d("Bluetooth disabled, can't start advertising")
            return
        }

        L.d("Starting BLE advertising with power $power")

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

        val scanData = AdvertiseData.Builder()
            .addServiceData(parcelUuid, buid.hexAsByteArray).build()

        btManager.adapter?.bluetoothLeAdvertiser?.startAdvertising(
            settings,
            data,
            scanData,
            advertisingCallback
        )
    }

    fun stopAdvertising() {
        L.d("Stopping BLE advertising")
        isAdvertising = false
        btManager.adapter?.bluetoothLeAdvertiser?.stopAdvertising(advertisingCallback)
    }
}