package cz.covid19cz.app.bt.entity

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.ext.minutesToMilis
import cz.covid19cz.app.ext.rssiToDistanceString
import java.util.*
import kotlin.collections.ArrayList


class ScanSession(var deviceId: String = DEFAULT_BUID, val mac: String) {

    companion object{
        const val DEFAULT_BUID = "UNKNOWN"
    }

    private val rssiList = ArrayList<Rssi>()
    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val currDistance = SafeMutableLiveData("")

    var avgRssi = 0
    var medRssi = 0
    val timestampStart: Long
        get() = rssiList.firstOrNull()?.timestamp ?: 0L
    val timestampEnd: Long
        get() = rssiList.lastOrNull()?.timestamp ?: 0L
    val rssiCount: Int
        get() = rssiList.size

    fun addRssi(rssiVal: Int) {
        val rssi = Rssi(rssiVal)
        rssiList.add(rssi)
        currRssi.postValue(rssiVal)
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

    private fun median(l: IntArray): Int {
        Arrays.sort(l)
        val middle = l.size / 2
        return if (l.size % 2 == 0) {
            val left = l[middle - 1]
            val right = l[middle]
            (left + right) / 2
        } else {
            l[middle]
        }
    }

    fun reset(){
        rssiList.clear()
        avgRssi = 0
        medRssi = 0
    }
}