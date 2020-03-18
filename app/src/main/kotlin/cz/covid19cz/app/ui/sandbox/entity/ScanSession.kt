package cz.covid19cz.app.ui.sandbox.entity

import arch.livedata.SafeMutableLiveData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScanSession(val deviceId: String, val mac: String) {

    private val rssiList = ArrayList<Rssi>()
    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val liveMinRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val liveMaxRssi = SafeMutableLiveData(Int.MIN_VALUE)
    val liveAvgRssi = SafeMutableLiveData(0)
    val liveMedRssi = SafeMutableLiveData(0)
    val inRange = SafeMutableLiveData(false)
    val sessionStart = SafeMutableLiveData(0L)
    val latestUpdate = SafeMutableLiveData(0L)
    val sessionDuration = SafeMutableLiveData(0L)
    val sessionDurationString = SafeMutableLiveData("")
    val latestUpdateString = SafeMutableLiveData("")

    var minRssi = Int.MAX_VALUE
    var maxRssi = Int.MIN_VALUE
    var avgRssi = 0
    var medRssi = 0
    val timestampStart: Long
        get() = rssiList.firstOrNull()?.timestamp ?: 0L
    val timestampEnd: Long
        get() = rssiList.lastOrNull()?.timestamp ?: 0L


    fun addRssi(rssiVal: Int) {
        val rssi = Rssi(rssiVal)
        if (rssiList.size == 0) {
            sessionStart
        }

        rssiList.add(rssi)
        currRssi.postValue(rssiVal)
        latestUpdate.postValue(rssi.timestamp)
        latestUpdateString.postValue(
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                Date(
                    rssi.timestamp
                )
            )
        )

        calculateSessionDuration()
        recalculate()
        checkOutOfRange()
    }

    fun recalculate() {
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

    fun calculateSessionDuration() {
        if (rssiList.size > 0) {
            val secondsTotal = (rssiList.last().timestamp - rssiList.first().timestamp) / 1000
            sessionDuration.postValue(secondsTotal)

            val minutes = secondsTotal / 60
            val seconds = secondsTotal % 60

            sessionDurationString.postValue("${minutes}:${seconds.toString().padStart(2, '0')}")
        }
    }

    fun checkOutOfRange() {
        if (rssiList.size > 0) {
            inRange.postValue(System.currentTimeMillis() - rssiList.last().timestamp < 10000)
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