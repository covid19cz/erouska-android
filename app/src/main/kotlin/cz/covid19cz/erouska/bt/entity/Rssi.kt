package cz.covid19cz.erouska.bt.entity

class Rssi(val rssi : Int) {

    val timestamp : Long

    init {
        timestamp = System.currentTimeMillis()
    }
}