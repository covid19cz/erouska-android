package cz.covid19cz.app.bt.entity

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.AppConfig
import java.util.*
import kotlin.collections.ArrayList

class ScanSession(var deviceId: String, val mac: String) {

    private val rssiList = ArrayList<Rssi>()
    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)

    var maxRssi = Int.MIN_VALUE
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
        var sum: Int = 0
        var max: Int = Int.MIN_VALUE

        for (rssi in rssiList) {
            sum += rssi.rssi
            if (rssi.rssi > max) {
                max = rssi.rssi
            }
        }
        if (rssiList.size != 0) {
            medRssi = median(rssiList.map { it.rssi }.toIntArray())
        }
        maxRssi = max
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

}