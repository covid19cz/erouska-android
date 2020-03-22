package cz.covid19cz.app.bt

import android.bluetooth.*
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


class BluetoothRepository(val context: Context, private val db: DatabaseRepository) {

    private val SERVICE_UUID = UUID.fromString("1440dd68-67e4-11ea-bc55-0242ac130003")
    private val GATT_CHARACTERISTIC_UUID = UUID.fromString("9472fbde-04ff-4fff-be1c-b9d3287e8f28")

    private val btManager = context.getSystemService<BluetoothManager>()
    private val rxBleClient: RxBleClient = RxBleClient.create(context)

    val scanResultsMap = HashMap<String, ScanSession>()
    val discoveredIosDevices = HashMap<String, ScanSession>()
    val scanResultsList = ObservableArrayList<ScanSession>()

    var isAdvertising = false
    var isScanning = false

    private var scanDisposable: Disposable? = null
    private var outOfRangeChecker: Disposable? = null

    private val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("BLE advertising started.")
            isAdvertising = true
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            Log.e("BLE advertising failed: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("GATT connected")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("GATT disconnected")
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
                gatt.disconnect()

                if (buid != null){
                    Log.d("BUID found in characteristic")
                    discoveredIosDevices[mac]?.let { s ->
                        Observable.just(s).map { session ->
                            session.deviceId = buid
                            scanResultsMap[buid] = session
                            session
                        }.execute({
                            scanResultsList.add(it)
                        }, {
                            Log.e(it)
                        })
                    }
                } else {
                    Log.e("BUID not found in characteristic")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect()
                return
            }
            val characteristic = gatt.getService(SERVICE_UUID)?.getCharacteristic(GATT_CHARACTERISTIC_UUID)
            if (characteristic != null){
                Log.d("GATT characteristic found")
                gatt.readCharacteristic(characteristic)
            } else {
                Log.e("GATT characteristic not found")
                gatt.disconnect()
            }
        }
    }

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
            Log.d("Bluetooth disabled, can't start scanning")
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
        Log.d("Saving data to database")
        Observable.just(scanResultsList.toTypedArray())
            .map { tempArray ->
                for (item in tempArray) {
                    item.calculate()
                    db.add(
                        ScanResultEntity(
                            0,
                            item.deviceId,
                            item.timestampStart,
                            item.timestampEnd,
                            item.maxRssi,
                            item.medRssi,
                            item.rssiCount
                        )
                    )
                }
                tempArray.size
            }.execute({
                Log.d("$it records saved")
                clearScanResults()
            }, { Log.e(it) })
    }

    private fun onScanResult(result: ScanResult) {
        if (result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true) {

            var deviceId = getBuidFromAndroid(result.scanRecord.bytes)
            if (result.scanRecord.deviceName == "Covid-19" && deviceId == null) {
                if (!discoveredIosDevices.containsKey(result.bleDevice.macAddress)) {
                    getBuidFromIos(result)
                } else {
                    discoveredIosDevices[result.bleDevice.macAddress]?.addRssi(result.rssi)
                }
            }

            deviceId?.let {
                if (!scanResultsMap.containsKey(deviceId)) {
                    val newEntity = ScanSession(deviceId, result.bleDevice.macAddress)
                    newEntity.addRssi(result.rssi)
                    scanResultsList.add(newEntity)
                    scanResultsMap[deviceId] = newEntity
                    Log.d("Found new Android: $deviceId")
                }

                scanResultsMap[deviceId]?.let { entity ->
                    entity.addRssi(result.rssi)
                    Log.d("Found existing device: $deviceId")
                }
            }
        }
    }

    private fun getBuidFromIos(result: ScanResult) {
        val mac = result.bleDevice.macAddress
        val session = ScanSession("iOS Device", mac)
        session.addRssi(result.rssi)
        Log.d("Connecting to GATT")
        discoveredIosDevices[mac] = session

        val device = btManager?.adapter?.getRemoteDevice(mac)
        val gatt = device?.connectGatt(context, false, gattCallback)

        //val characteristic = gatt?.getService(SERVICE_UUID)?.getCharacteristic(GATT_CHARACTERISTIC_UUID)
    }

    private fun getBuidFromAndroid(bytes: ByteArray): String? {

        var result = ByteArray(10)

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
                bytes.copyInto(result, 0, currIndex + 2 + 2, currIndex + 2 + 2 + 10)
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

    private fun clearScanResults() {
        discoveredIosDevices.clear()
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
            Log.d("Bluetooth disabled, can't start advertising")
            return
        }

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

        val scanData = AdvertiseData.Builder()
            .addServiceData(parcelUuid, buid.hexAsByteArray).build()

        btManager?.adapter?.bluetoothLeAdvertiser?.startAdvertising(
            settings,
            data,
            scanData,
            advertisingCallback
        )
    }

    fun stopAdvertising() {
        Log.d("Stopping BLE advertising")
        isAdvertising = false
        btManager?.adapter?.bluetoothLeAdvertiser?.stopAdvertising(advertisingCallback)
    }


}