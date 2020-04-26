package cz.covid19cz.erouska.helpers

import java.lang.reflect.Method


object ManufacturerHelper {

    private val BATTERY_OPTIMIZATION_MANUFACTURERS = setOf("huawei", "asus", "lenovo", "samsung", "sony") // do not list xiaomi here is handled based on system
    private val PHONE_MANUFACTURER: String = android.os.Build.MANUFACTURER.toLowerCase()

    fun isBatteryTutorialNeeded(): Boolean = BATTERY_OPTIMIZATION_MANUFACTURERS.contains(PHONE_MANUFACTURER) || isMiUi()

    /**
     * code used from https://stackoverflow.com/questions/47610456/how-to-detect-miui-rom-programmatically-in-android
     * do not expect Battery tutorial for xiaomi with Android one - because there was already bug in Trello
     */
    private fun isMiUi(): Boolean {
        val c = Class.forName("android.os.SystemProperties")
        val get: Method = c.getMethod("get", String::class.java)
        val miui = get.invoke(c, "ro.miui.ui.version.code") as String
        return miui.isNotEmpty()
    }
}