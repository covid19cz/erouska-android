package cz.covid19cz.erouska.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.UserManager
import com.jaredrummler.android.device.DeviceName
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ext.isLocationEnabled
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
        } catch (t: Throwable) {
            Build.MODEL
        }
    }

    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    fun isBtEnabled(): Boolean {
        return context.isBtEnabled()
    }

    fun isLocationEnabled(): Boolean {
        return context.isLocationEnabled()
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    fun isBatterySaverOn(): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pwrm.isPowerSaveMode
    }

    fun supportsBLE(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isUserDeviceOwner(): Boolean {
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager
        return um.isSystemUser
    }

    fun supportsMultiAds(): Boolean {
        return BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported
    }
}
