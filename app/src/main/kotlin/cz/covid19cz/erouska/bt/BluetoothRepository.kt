package cz.covid19cz.erouska.bt

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseCallback.*
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.os.SystemClock
import androidx.databinding.ObservableArrayList
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.bt.entity.ObservableScanSession
import cz.covid19cz.erouska.bt.entity.ScanSession
import cz.covid19cz.erouska.db.DatabaseRepository
import cz.covid19cz.erouska.db.ScanDataEntity
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.utils.L
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import no.nordicsemi.android.support.v18.scanner.*
import no.nordicsemi.android.support.v18.scanner.ScanSettings.MATCH_MODE_AGGRESSIVE
import no.nordicsemi.android.support.v18.scanner.ScanCallback.*
import java.util.*
import kotlin.collections.HashMap


class BluetoothRepository(
    val context: Context,
    private val db: DatabaseRepository,
    private val btManager: BluetoothManager,
    private val prefs : SharedPrefsRepository
) {

    private companion object {
        val SERVICE_UUID: UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")
        val GATT_CHARACTERISTIC_UUID: UUID = UUID.fromString("9472fbde-04ff-4fff-be1c-b9d3287e8f28")
        const val APPLE_MANUFACTURER_ID = 76
    }

    private val scanResultsMap = HashMap<String, ScanSession>()
    private var discoveredIosDevices: MutableMap<String, ScanSession> = mutableMapOf()
    val scanResultsList = ObservableArrayList<ScanSession>()

    private var gattFailDisposable: Disposable? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onScanResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            L.e(ScanFailedException(errorCode))
            BluetoothLeScannerCompat.getScanner().stopScan(this)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach {
                onScanResult(it)
            }
        }
    }

    private val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            L.d("BLE advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            L.e(AdvertisingFailedException(errorCode))
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
          if (newState == BluetoothProfile.STATE_CONNECTED) {
                L.d("GATT connected. Mac: ${gatt.device.address}")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectFromGatt(gatt)
                L.d("GATT disconnected. Mac: ${gatt.device.address}")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (GATT_CHARACTERISTIC_UUID == characteristic?.uuid) {
                val tuid = characteristic.value?.asHexLower
                val mac = gatt.device?.address

                if (tuid != null) {
                    L.d("GATT TUID found. Mac ${gatt.device.address}. TUID: $tuid")
                    discoveredIosDevices[mac]?.let { session ->
                            session.deviceId = tuid
                            scanResultsMap[tuid] = session
                            session
                    }
                } else {
                    L.e("GATT TUID not found on $mac")
                }
                disconnectFromGatt(gatt)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectFromGatt(gatt)
                return
            }
            val characteristic =
                gatt.getService(SERVICE_UUID)?.getCharacteristic(GATT_CHARACTERISTIC_UUID)
            if (characteristic != null) {
                gatt.readCharacteristic(characteristic)
            } else {
                disconnectFromGatt(gatt)
            }
        }
    }

    fun hasBle(c: Context): Boolean {
        return c.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isBtEnabled(): Boolean {
        return btManager.isBluetoothEnabled()
    }

    fun startScanning() {
        resetScanning()

        if (!isBtEnabled()) {
            L.d("Bluetooth disabled, can't start scanning")
            return
        }

        L.d("Starting BLE scanning in mode: ${AppConfig.scanMode}")

        val androidScannerSettings: ScanSettings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(AppConfig.scanMode)
            .setMatchMode(MATCH_MODE_AGGRESSIVE)
            .build()

        BluetoothLeScannerCompat.getScanner().startScan(
            listOf(
                ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build(),
                ScanFilter.Builder()
                    .setManufacturerData(APPLE_MANUFACTURER_ID, byteArrayOf(), byteArrayOf())
                    .build()
            ),
            androidScannerSettings,
            scanCallback
        )

    }

    fun stopScanning() {
        L.d("Stopping BLE scanning")
        resetScanning()

        saveDataAndClearScanResults()
    }

    private fun resetScanning() {
        if (btManager.isBluetoothEnabled()) {
            BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
        }
        gattFailDisposable?.dispose()
    }

    private fun saveDataAndClearScanResults() {
        L.d("Saving data to database")
        scanResultsMap.values.toList().map {
            it.fold(AppConfig.collectionSeconds * 1000)
        }.flatten().apply {
            Observable.fromIterable(this)
                .map { session ->
                    session.calculate()
                    val scanResult = ScanDataEntity(
                        0,
                        session.deviceId,
                        session.timestampStart,
                        session.timestampEnd,
                        session.avgRssi,
                        session.medRssi,
                        session.rssiCount
                    )
                    L.d("Saving: $scanResult")
                    db.add(scanResult)
                }.execute({
                    L.d("$it records saved")
                    clearScanResults()
                    dbCleanup()
                }, { L.e(it) })
        }
    }

    private fun onScanResult(result: ScanResult) {
        result.scanRecord?.let { scanRecord ->
            if (isServiceUUIDMatch(result) || canBeIosOnBackground(scanRecord)) {
                val deviceId = scanRecord.bytes?.let {
                    getTuidFromAdvertising(scanRecord)
                }
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

    private fun isServiceUUIDMatch(result: ScanResult): Boolean {
        return result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true
    }

    /**
     * iOS doesn't send UUID with screen off and app in background so we have to find another way
     * how to detect if there is an iOS device on the other side.
     *
     * iOS devices send manufacturer specific data that look something like this:
     * 0x4C000100000000000000000200000000000000
     *
     * This checks for the apple inc. key and then matches the data against the above
     */
    private fun canBeIosOnBackground(scanRecord: ScanRecord?): Boolean {
        return scanRecord?.manufacturerSpecificData?.get(APPLE_MANUFACTURER_ID)?.let { data ->
            data.size > 10 && data[0] == 0x01.toByte() && data[1] == 0x00.toByte() && data[9] == 0x02.toByte()
        } ?: false
    }

    private fun handleAndroidDevice(result: ScanResult, deviceId: String) {
        if (!scanResultsMap.containsKey(deviceId)) {
            val newEntity = ObservableScanSession(deviceId, result.device.address)
            newEntity.addRssi(result.rssi, result.absoluteTimestampMillis())
            scanResultsList.add(newEntity)
            scanResultsMap[deviceId] = newEntity
            L.d("Found new Android device: $deviceId")
        }

        scanResultsMap[deviceId]?.let { entity ->
            entity.addRssi(result.rssi, result.absoluteTimestampMillis())
            L.d("Device (Android) $deviceId - RSSI ${result.rssi}")
        }
    }

    private fun handleIosDevice(result: ScanResult) {
        if (!discoveredIosDevices.containsKey(result.device.address)) {
            L.d("Found new iOS: Mac: ${result.device.address}")
            registerIOSDevice(result)
        } else {
            discoveredIosDevices[result.device.address]?.let {
                if (it.deviceId == ScanSession.UNKNOWN_TUID) {
                    if (it.gattAttemptTimestamp + 5000 < System.currentTimeMillis()) {
                        getTuidFromGatt(it)
                    }
                } else if (!scanResultsMap.containsKey(it.deviceId)) {
                    scanResultsMap[it.deviceId] = it
                } else {
                    if (!scanResultsList.contains(it)) {
                        scanResultsList.add(it)
                    }
                }
                it.addRssi(result.rssi, result.absoluteTimestampMillis())
                L.d("Device (iOS) ${it.deviceId} - RSSI ${result.rssi}")
            }
            return
        }
    }

    private fun registerIOSDevice(result: ScanResult) {
        val mac = result.device.address
        val session = ObservableScanSession(mac = mac)
        session.addRssi(result.rssi, result.absoluteTimestampMillis())
        discoveredIosDevices[mac] = session
        getTuidFromGatt(session)
        scanResultsList.add(session)
    }

    private fun getTuidFromGatt(session: ScanSession) {
        connectToGatt(session.mac)
        session.gattAttemptTimestamp = System.currentTimeMillis()
    }

    private fun connectToGatt(mac: String) {
        L.d("Connecting to GATT . Mac:${mac}")
        val device = btManager.adapter?.getRemoteDevice(mac)
        device?.connectGatt(context, false, gattCallback)
    }

    private fun disconnectFromGatt(gatt: BluetoothGatt) {
        L.d("Disconnecting from GATT . Mac:${gatt.device.address}")
        gatt.disconnect()
        gatt.close()
    }

    private fun getTuidFromAdvertising(scanRecord: ScanRecord): String? {
        val bytes = scanRecord.bytes!!
        val result = ByteArray(10)

        var currIndex = 0
        var len = -1
        var type: Byte

        while (currIndex < bytes.size && len != 0) {
            len = bytes[currIndex].toInt()
            type = bytes[currIndex + 1]

            if (type == 0x21.toByte()) { //128 bit Service UUID (most cases)
                // +2 (skip lenght byte and type byte), +16 (skip 128 bit Service UUID)
                if (bytes.size >= (currIndex + 2 + 16 + 10)) {
                    bytes.copyInto(result, 0, currIndex + 2 + 16, currIndex + 2 + 16 + 10)
                }
                break
            } else if (type == 0x16.toByte()) { //16 bit Service UUID (rare cases)
                // +2 (skip lenght byte and type byte), +2 (skip 16 bit Service UUID)
                if (bytes.size >= (currIndex + 2 + 2 + 10)) {
                    bytes.copyInto(result, 0, currIndex + 2 + 2, currIndex + 2 + 2 + 10)
                }
                break
            } else if (type == 0x20.toByte()) { //32 bit Service UUID (just in case)
                // +2 (skip lenght byte and type byte), +4 (skip 32 bit Service UUID)
                if (bytes.size >= (currIndex + 2 + 4 + 10)) {
                    bytes.copyInto(result, 0, currIndex + 2 + 4, currIndex + 2 + 4 + 10)
                }
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
        // Don't clear whole iOS device cache to preventing DDOS GATT, but remove UNKNOWN devices
        discoveredIosDevices = discoveredIosDevices.filterValues {
            it.deviceId != ScanSession.UNKNOWN_TUID
        }.apply {
            forEach { it.value.reset() }
        }.toMutableMap()
    }

    fun supportsAdvertising(): Boolean {
        return btManager.adapter?.isMultipleAdvertisementSupported ?: false
    }

    fun startAdvertising(tuid: String) {
        val power = AppConfig.advertiseTxPower

        stopAdvertising()

        if (!isBtEnabled()) {
            L.d("Bluetooth disabled, can't start advertising")
            return
        }

        L.d("Starting BLE advertising with power $power")

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AppConfig.advertiseMode)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(power)
            .build()

        val parcelUuid = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(parcelUuid)
            .build()

        val scanData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(parcelUuid, tuid.hexAsByteArray).build()

        btManager.adapter?.bluetoothLeAdvertiser?.startAdvertising(
            settings,
            data,
            scanData,
            advertisingCallback
        )
    }

    fun stopAdvertising() {
        L.d("Stopping BLE advertising")
        btManager.adapter?.bluetoothLeAdvertiser?.stopAdvertising(advertisingCallback)
    }

    private fun dbCleanup(){
        if (System.currentTimeMillis() - prefs.getLastDbCleanupTimestamp() > 24.hoursToMilis()) {
            L.d("Deleting data older than ${AppConfig.persistDataDays} days")
            val rows = db.deleteOldData()
            L.d("$rows records deleted")
            prefs.saveLastDbCleanupTimestamp(System.currentTimeMillis())
        }
    }

    private fun ScanResult.absoluteTimestampMillis(): Long {
        // scan timestamp is in nanoseconds since device boot
        return System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() - this.timestampNanos) / 1000000
    }

    class ScanFailedException(errorCode: Int): Exception(errorCodeToMessage(errorCode)) {
        companion object {
            fun errorCodeToMessage(errorCode: Int): String {
               return when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "SCAN_FAILED_ALREADY_STARTED"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"
                    SCAN_FAILED_INTERNAL_ERROR -> "SCAN_FAILED_INTERNAL_ERROR"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "SCAN_FAILED_FEATURE_UNSUPPORTED"
                    SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES"
                    SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> "SCAN_FAILED_SCANNING_TOO_FREQUENTLY"
                    else -> "UNKNOWN ERROR"
                }.run {
                    "Scan failed with error: $this"
                }
            }
        }
    }

    class AdvertisingFailedException(errorCode: Int): Exception(errorCodeToMessage(errorCode)) {
        companion object {
            fun errorCodeToMessage(errorCode: Int): String {
                return when (errorCode) {
                    ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE"
                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                    ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED"
                    ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR"
                    ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                    else -> "UNKNOWN ERROR"
                }.run {
                    "Advertising failed with error: $this"
                }
            }
        }
    }
}