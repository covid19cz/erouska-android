package cz.covid19cz.erouska.ext

import android.os.Build
import android.os.PowerManager

fun PowerManager.batterySaverRestrictsLocation(): Boolean {
    return isPowerSaveMode && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationPowerSaveMode == PowerManager.LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF
    } else {
        true
    }
}