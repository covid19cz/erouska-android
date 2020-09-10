package cz.covid19cz.erouska.ext

import android.location.LocationManager

fun LocationManager.isLocationProvided(): Boolean {
    allProviders.forEach {
        if (isProviderEnabled(it)) return true
    }
    return false
}