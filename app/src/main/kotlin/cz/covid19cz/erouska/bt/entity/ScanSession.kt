package cz.covid19cz.erouska.bt.entity

import arch.livedata.SafeMutableLiveData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ScanSession(deviceId: String = UNKNOWN_BUID, val mac: String) {

    companion object{
        const val UNKNOWN_BUID = "UNKNOWN"
    }

    var deviceId: String = deviceId
        set(value) {
            field = value
            observableDeviceId.postValue(value)
        }

    private val rssiList = ArrayList<Rssi>()
    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val lastGattAttempt = SafeMutableLiveData("")
    val observableDeviceId = SafeMutableLiveData(deviceId)

    var avgRssi = 0
    var medRssi = 0
    val timestampStart: Long
        get() = rssiList.firstOrNull()?.timestamp ?: 0L
    val timestampEnd: Long
        get() = rssiList.lastOrNull()?.timestamp ?: 0L
    val rssiCount: Int
        get() = rssiList.size
    var gattAttemptTimestamp: Long = 0

    fun addRssi(rssiVal: Int) {
        val rssi = Rssi(rssiVal)
        rssiList.add(rssi)
        currRssi.postValue(rssiVal)
        lastGattAttempt.postValue(lastGattAttemptAsString())
    }

    fun updatedDeviceId(deviceId: String) {
        this.deviceId = deviceId
        observableDeviceId.postValue(deviceId)
    }

    fun calculate() {
        var sum = 0

        for (rssi in rssiList) {
            sum += rssi.rssi
        }
        if (rssiList.size != 0) {
            if (sum != 0) {
                avgRssi = sum / rssiList.size
            }
            medRssi = median(rssiList.map { it.rssi }.toIntArray())
        }
    }

    private fun median(values: IntArray): Int {
        Arrays.sort(values)
        val middle = values.size / 2
        return if (values.size % 2 == 0) {
            val left = values[middle - 1]
            val right = values[middle]
            (left + right) / 2
        } else {
            values[middle]
        }
    }

    fun reset(){
        rssiList.clear()
        avgRssi = 0
        medRssi = 0
    }

    fun lastGattAttemptAsString() : String {
        if (gattAttemptTimestamp == 0L) {
            return "N/A (Android)"
        }
        Date(gattAttemptTimestamp).apply {
            return SimpleDateFormat("hh:mm:ss").format(this)
        }
    }

}