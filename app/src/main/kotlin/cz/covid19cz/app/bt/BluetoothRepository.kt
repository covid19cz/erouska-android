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

    fun ensureBtEnabled() {
        if (!isBtEnabled()) {
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
            .subscribe({ scanResult ->
                onScanResult(scanResult)
            }, {
                isScanning = false
                Log.e(it)
            })
        isScanning = true
    }

    fun stopScanning() {
        isScanning = false
        Log.d("Stopping BLE scanning")
        scanDisposable?.dispose()
        scanDisposable = null
    }

    private fun onScanResult(result: ScanResult) {

        if (result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true) {

            var deviceId =
                if (result.scanRecord.deviceName == "Covid-19") {
                    Log.d("Probabaly iPhone detected")
                    //TODO: Handle iPhone
                    "iPhone"
                } else if (result.scanRecord?.serviceData?.containsKey(ParcelUuid(SERVICE_UUID)) == true) {
                    // BUID is in the map under our UUID
                    Log.d("BUID is in the map under our UUID")
                    String(
                        result.scanRecord.getServiceData(ParcelUuid(SERVICE_UUID))!!,
                        Charset.forName("utf-8")
                    )
                } else if (result.scanRecord.serviceData.isNotEmpty()) {
                    // BUID is in the map under different UUID
                    Log.d("BUID is in the map under different UUID")
                    String(result.scanRecord.serviceData.values.first(), Charset.forName("utf-8"))
                } else {
                    // BUID isn't in map
                    val hackyBuidBytes = ByteArray(10)
                    var lastByteIndex = -1

                    result.scanRecord?.bytes?.let {
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
                            Log.d("BUID isn't in the map")
                            String(hackyBuidBytes, Charset.forName("utf-8"))
                        } else {
                            Log.d("BUID not found")
                            null
                        }
                    }
                }

            // This is fokin dirty, I'll refactor it, I promise :D
            if (deviceId != null && deviceId?.length != 10) {
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
                    Log.d("Found existing device: $deviceId")
                }
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

    fun startAdvertising(deviceId: String) {

        val power = AppConfig.advertiseTxPower

        if (isAdvertising) {
            stopAdvertising()
        }

        ensureBtEnabled()

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
            .addServiceData(parcelUuid, deviceId.toByteArray(Charset.forName("utf-8"))).build()

        btManager.adapter?.bluetoothLeAdvertiser?.startAdvertising(
            settings,
            data,
            scanData,
            serverCallback
        );

        //btManager.openGattServer(c, serverCallback).addService(service)
    }

    fun stopAdvertising() {
        Log.i("Stopping BLE advertising")
        btManager.adapter?.bluetoothLeAdvertiser?.stopAdvertising(serverCallback)
    }


}