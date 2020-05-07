package cz.covid19cz.erouska.bt.entity

import android.os.SystemClock
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.utils.L
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


open class ScanSession(tuid: String = UNKNOWN_TUID, val mac: String) {

    companion object{
        const val UNKNOWN_TUID = "UNKNOWN"
    }

    open var deviceId: String = tuid
    private val rssiList = ArrayList<Rssi>()

    var avgRssi = 0
    var medRssi = 0
    val timestampStart: Long
        get() = rssiList.firstOrNull()?.timestamp ?: 0L
    val timestampEnd: Long
        get() = rssiList.lastOrNull()?.timestamp ?: 0L
    val rssiCount: Int
        get() = rssiList.size
    var gattAttemptTimestamp: Long = 0

    open fun addRssi(rssiVal: Int, timestampMilis: Long) {
        val rssi = Rssi(rssiVal, timestampMilis)
        rssiList.add(rssi)
    }

    fun fold(interval: Long): List<ScanSession> {
        fun MutableList<ScanSession>.getValidScanSession(rssi: Rssi): ScanSession {
            return lastOrNull()?.takeIf {
                rssi.timestamp - it.timestampStart < interval
            } ?: ScanSession(deviceId, mac).apply {
                this@getValidScanSession.add(this)
            }
        }

        return rssiList.fold(mutableListOf(), { acc, item ->
                acc.getValidScanSession(item).apply {
                    this.addRssi(item.rssi, item.timestamp)
                }
                acc
        })


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

class ObservableScanSession(tuid: String = UNKNOWN_TUID, mac: String): ScanSession(tuid, mac) {

    override var deviceId: String = tuid
        set(value) {
            field = value
            observableDeviceId.postValue(value)
        }

    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val lastGattAttempt = SafeMutableLiveData("")
    val observableDeviceId = SafeMutableLiveData(tuid)

    override fun addRssi(rssiVal: Int, timestampMilis: Long) {
        super.addRssi(rssiVal, timestampMilis)
        currRssi.postValue(rssiVal)
        lastGattAttempt.postValue(lastGattAttemptAsString())
    }
}