package cz.covid19cz.app.ext

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

fun Context.isLocationEnabled(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // This is new method provided in API 28
        getSystemService<LocationManager>()?.isLocationEnabled ?: false
    } else {
        // This is Deprecated in API 28
        val mode = Settings.Secure.getInt(
            this.contentResolver, Settings.Secure.LOCATION_MODE,
            Settings.Secure.LOCATION_MODE_OFF
        )
        mode != Settings.Secure.LOCATION_MODE_OFF
    }
}