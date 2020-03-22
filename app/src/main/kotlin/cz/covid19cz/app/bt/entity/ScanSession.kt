package cz.covid19cz.app.bt.entity

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.AppConfig
import java.util.*
import kotlin.collections.ArrayList

class ScanSession(val deviceId: String, val mac: String) {

    private val rssiList = ArrayList<Rssi>()
    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)

    // TODO: keep only necessary live data, just for testing
    val liveMinRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val liveMaxRssi = SafeMutableLiveData(Int.MIN_VALUE)
    val liveAvgRssi = SafeMutableLiveData(0)
    val liveMedRssi = SafeMutableLiveData(0)
    val inRange = SafeMutableLiveData(true)

    var minRssi = Int.MAX_VALUE
    var maxRssi = Int.MIN_VALUE
    var avgRssi = 0
    var medRssi = 0
    val timestampStart: Long
        get() = rssiList.firstOrNull()?.timestamp ?: 0L
    val timestampEnd: Long
        get() = rssiList.lastOrNull()?.timestamp ?: 0L
    val rssiCount: Int
        get() = rssiList.size
    val avgUpdateSeconds: Long
        get() {
            var sum: Long = 0
            if (rssiList.size > 1) {
                for (i in 0 until rssiList.size - 1) {
                    sum += (rssiList[i + 1].timestamp - rssiList[i].timestamp) / 1000
                }
                return (sum / rssiList.size - 1)
            }
            return 0
        }

    fun addRssi(rssiVal: Int) {
        val rssi = Rssi(rssiVal)

        rssiList.add(rssi)
        currRssi.postValue(rssiVal)

        //calculate()
        //checkOutOfRange()
    }

    fun calculate() {
        var sum: Int = 0
        var min: Int = Int.MAX_VALUE
        var max: Int = Int.MIN_VALUE

        for (rssi in rssiList) {
            sum += rssi.rssi
            if (rssi.rssi > max) {
                max = rssi.rssi
            }
            if (rssi.rssi < min) {
                min = rssi.rssi
            }
        }
        if (rssiList.size != 0) {
            avgRssi = sum / rssiList.size
            medRssi = median(rssiList.map { it.rssi }.toIntArray())
        }
        minRssi = min
        maxRssi = max

        liveMinRssi.postValue(minRssi)
        liveMaxRssi.postValue(maxRssi)
        liveAvgRssi.postValue(avgRssi)
        liveMedRssi.postValue(medRssi)

        liveMedRssi.postValue(medRssi)
    }

    fun checkOutOfRange() {
        if (rssiList.size > 0) {
            inRange.postValue(System.currentTimeMillis() - rssiList.last().timestamp < AppConfig.BLE_OUT_OF_RANGE_TIMEOUT * 1000)
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

}