package cz.covid19cz.erouska.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.jaredrummler.android.device.DeviceName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfo @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @SuppressLint("DefaultLocale")
    fun getManufacturer(): String {
        return Build.MANUFACTURER.capitalize()
    }

    fun getDeviceName(): String {
        return try {
            DeviceName.getDeviceInfo(context).marketName
        } catch (t: Throwable){
            Build.MODEL
        }
    }

    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }
}
