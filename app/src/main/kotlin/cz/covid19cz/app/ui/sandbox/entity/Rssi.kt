package cz.covid19cz.app.ui.sandbox.entity

class Rssi(val rssi : Int) {

    val timestamp : Long

    init {
        timestamp = System.currentTimeMillis()
    }
}