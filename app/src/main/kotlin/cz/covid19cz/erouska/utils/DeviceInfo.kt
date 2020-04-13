package cz.covid19cz.erouska.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.jaredrummler.android.device.DeviceName
import java.util.*

class DeviceInfo(
    private val context: Context
) {

    @SuppressLint("DefaultLocale")
    fun getManufacturer(): String {
        return Build.MANUFACTURER.capitalize()
    }

    fun getDeviceName(): String {
        return DeviceName.getDeviceInfo(context).marketName
    }

    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    fun getLocale(): String {
        return Locale.getDefault().toString()
    }
}
