package cz.covid19cz.erouska.utils

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ext.isLocationEnabled
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class DeviceUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isBtEnabled(): Boolean {
        return context.isBtEnabled()
    }

    fun isLocationEnabled(): Boolean {
        return context.isLocationEnabled()
    }
}