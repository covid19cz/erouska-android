package cz.covid19cz.app.bt

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.content.getSystemService
import androidx.databinding.ObservableArrayList
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.bt.entity.ScanSession
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.ScanResultEntity
import cz.covid19cz.app.ext.asHexLower
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.ext.hexAsByteArray
import cz.covid19cz.app.utils.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class BluetoothRepository(context: Context, private val db: DatabaseRepository) {

    private val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")

    private val btManager = context.getSystemService<BluetoothManager>()
    private val rxBleClient: RxBleClient = RxBleClient.create(context)

    val scanResultsMap = HashMap<String, ScanSession>()
    val scanResultsList = ObservableArrayList<ScanSession>()

    private val serverCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            Log.i("BLE advertising started.")
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            Log.i("BLE advertising failed: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

    var isAdvertising = false
    var isScanning = false

    var scanDisposable: Disposable? = null
    var outOfRangeChecker: Disposable? = null

    fun hasBle(c: Context): Boolean {
        return c.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isBtEnabled(): Boolean {
        return btManager?.adapter?.isEnabled ?: false
    }

    private fun enableBt() {
        btManager?.adapter?.enable()
    }

    fun startScanning() {
        if (isScanning) {
            stopScanning()
        }

        if (!isBtEnabled()) {
            Log.e("Bluetooth is not disabled, can't start scanning.")
            return
        }

        Log.d("Starting BLE scanning in mode: ${AppConfig.scanMode}")

        // "Some" scan filter needed for background scanning since Android 8.1.
        // However, some devices (at least Samsung S10e...) consider empty filter == no filter.
        //TODO: We have to solve this using some list of problematic devices
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val scanFilter = builder.build()

        scanDisposable = rxBleClient.scanBleDevices(
            ScanSettings.Builder().setScanMode(AppConfig.scanMode).build(),
            ScanFilter.Builder().build()
        ).subscribe({ scanResult ->
            onScanResult(scanResult)
        }, {
            isScanning = false
            Log.e(it)
        })

        outOfRangeChecker = Observable.interval(0, 5, TimeUnit.SECONDS)
            .map {
                scanResultsList.forEach { it.checkOutOfRange() }
            }.subscribe()

        isScanning = true
    }

    fun stopScanning() {
        isScanning = false
        Log.d("Stopping BLE scanning")
        scanDisposable?.dispose()
        scanDisposable = null
        outOfRangeChecker?.dispose()
        outOfRangeChecker = null
        saveScansAndDispose()
    }

    private fun saveScansAndDispose() {
        Log.i("Saving data to database")
        Observable.just(scanResultsList.toTypedArray())
            .map { tempArray ->
                for (item in tempArray) {
                    item.calculate()

                    val scanResult = ScanResultEntity(
                        0,
                        item.deviceId,
                        item.timestampStart,
                        item.timestampEnd,
                        item.maxRssi,
                        item.medRssi,
                        item.rssiCount
                    )

                    Log.d("Saving: $scanResult")

                    db.add(scanResult)
                }

                tempArray.size
            }.execute({
                Log.i("$it records saved")
                clearScanResults()
            }, { Log.e(it) })
    }

    private fun onScanResult(result: ScanResult) {
        if (result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true) {
            var deviceId = findBuid(result.scanRecord.bytes)

            if (deviceId.isNullOrBlank()) {
                deviceId = tryHackyParseBuid(result)
            }

            if (deviceId.isNullOrBlank() && result.scanRecord.deviceName == "Covid-19") {
                Log.d("Probably iPhone detected")
                deviceId = "iPhone"
            }

            deviceId?.let {
                if (!scanResultsMap.containsKey(deviceId)) {
                    val newEntity = ScanSession(deviceId, result.bleDevice.macAddress)
                    scanResultsList.add(newEntity)
                    scanResultsMap[deviceId] = newEntity
                    Log.d("Found new device: $deviceId")
                }

                scanResultsMap[deviceId]?.let { entity ->
                    entity.addRssi(result.rssi)
                    Log.d("Device $deviceId RSSI ${result.rssi}")
                }
            }
        }
    }

    private fun findBuid(bytes: ByteArray): String? {
        val result = ByteArray(10)

        var currIndex = 0
        var len = -1
        var type: Byte

        while (currIndex < bytes.size && len != 0) {
            len = bytes[currIndex].toInt()
            type = bytes[currIndex + 1]

            if (type == 0x21.toByte()) {
                // +2 (skip lenght byte and type byte), +16 (skip Service UUID)
                bytes.copyInto(result, 0, currIndex + 2 + 16, currIndex + 2 + 16 + 10)
                break
            } else {
                currIndex += len + 1
            }
        }

        val resultHex = result.asHexLower

        return if (resultHex != "00000000000000000000") {
            Log.d("BUID = $resultHex")
            resultHex
        } else null
    }

    private fun tryHackyParseBuid(result: ScanResult): String? {
        val hackyBuidBytes = ByteArray(10)
        var lastByteIndex = -1

        return result.scanRecord?.bytes?.let {
            for (i in result.scanRecord.bytes.size - 1 downTo 0) {
                if (result.scanRecord.bytes[i] != 0x00.toByte()) {
                    lastByteIndex = i + 1
                    break
                }
            }

            if (lastByteIndex != -1) {
                result.scanRecord.bytes.copyInto(
                    hackyBuidBytes,
                    0,
                    lastByteIndex - 10,
                    lastByteIndex
                )
                Log.d("Parsed BUID from ScanResult raw data")
                hackyBuidBytes.asHexLower
            } else {
                Log.d("Could not parse BUID from ScanResult raw data")
                null
            }
        }
    }

    private fun clearScanResults() {
        scanResultsList.clear()
        scanResultsMap.clear()
    }

    fun supportsAdvertising(): Boolean {
        return btManager?.adapter?.isMultipleAdvertisementSupported ?: false
    }

    fun startAdvertising(buid: String) {

        val power = AppConfig.advertiseTxPower

        if (isAdvertising) {
            stopAdvertising()
        }

        if (!isBtEnabled()) {
            Log.e("Bluetooth is not disabled, can't start advertising.")
            return
        }

        Log.i("Starting BLE advertising with power $power")

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

        btManager?.adapter?.bluetoothLeAdvertiser?.startAdvertising(
            settings,
            data,
            scanData,
            serverCallback
        )
    }

    fun stopAdvertising() {
        Log.i("Stopping BLE advertising")
        btManager?.adapter?.bluetoothLeAdvertiser?.stopAdvertising(serverCallback)
    }


}