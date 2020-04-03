package cz.covid19cz.erouska.utils

import android.os.Build
import com.jaredrummler.android.device.DeviceName
import cz.covid19cz.erouska.App
import java.util.*

object DeviceInfo {

    fun getManufacturer(): String {
        return Build.MANUFACTURER.capitalize()
    }

    fun getDeviceName(): String {
        return DeviceName.getDeviceInfo(App.instance).marketName
    }

    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    fun getLocale(): String {
        return Locale.getDefault().toString()
    }
}