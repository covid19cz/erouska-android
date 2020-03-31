package cz.covid19cz.erouska.utils

import android.bluetooth.BluetoothManager

fun BluetoothManager.isBluetoothEnabled(): Boolean {
    return adapter?.isEnabled ?: false
}