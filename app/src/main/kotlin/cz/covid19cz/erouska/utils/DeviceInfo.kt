package cz.covid19cz.erouska.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.jaredrummler.android.device.DeviceName
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

object DeviceInfo : KoinComponent {

    val context: Context by inject()

    @SuppressLint("DefaultLocale")
    @JvmStatic
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
