package cz.covid19cz.erouska.ext

import android.bluetooth.BluetoothManager

fun BluetoothManager.isBluetoothEnabled(): Boolean {
    return adapter?.isEnabled ?: false
}