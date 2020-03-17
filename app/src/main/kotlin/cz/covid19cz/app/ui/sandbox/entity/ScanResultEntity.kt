package cz.covid19cz.app.ui.sandbox.entity

import arch.livedata.SafeMutableLiveData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScanResultEntity(val deviceId: String, val mac: String) {

    private val rssiList = ArrayList<Rssi>()
    val currRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val minRssi = SafeMutableLiveData(Int.MAX_VALUE)
    val maxRssi = SafeMutableLiveData(Int.MIN_VALUE)
    val avgRssi = SafeMutableLiveData(Int.MIN_VALUE)
    val medRssi = SafeMutableLiveData(Int.MIN_VALUE)
    val inRange = SafeMutableLiveData(false)
    val sessionStart = SafeMutableLiveData(0L)
    val latestUpdate = SafeMutableLiveData(0L)
    val sessionDuration = SafeMutableLiveData(0L)
    val sessionDurationString = SafeMutableLiveData("")
    val latestUpdateString = SafeMutableLiveData("")

    fun addRssi(rssiVal: Int) {
        val rssi = Rssi(rssiVal)
        if (rssiList.size == 0){
            sessionStart
        }

        rssiList.add(rssi)
        currRssi.postValue(rssiVal)
        latestUpdate.postValue(rssi.timestamp)
        latestUpdateString.postValue(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(rssi.timestamp)))

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
        if (rssiList.size!=0) {
            avgRssi.postValue(sum / rssiList.size)
        }
        minRssi.postValue(min)
        maxRssi.postValue(max)

        val intList = rssiList.map { it.rssi }
        medRssi.postValue(median(intList.toIntArray()))
    }

    fun calculateSessionDuration(){
        if (rssiList.size > 0) {
            val secondsTotal = (rssiList.last().timestamp - rssiList.first().timestamp) / 1000
            sessionDuration.postValue(secondsTotal)

            val minutes = secondsTotal / 60
            val seconds = secondsTotal % 60

            sessionDurationString.postValue("${minutes}:${seconds.toString().padStart(2, '0')}")
        }
    }

    fun checkOutOfRange(){
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