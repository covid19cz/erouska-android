package cz.covid19cz.app.utils

import android.bluetooth.BluetoothManager

fun BluetoothManager.isBluetoothEnabled(): Boolean {
    return adapter?.isEnabled ?: false
}