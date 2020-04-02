package cz.covid19cz.erouska.utils

import android.annotation.SuppressLint
import android.os.Build
import cz.covid19cz.erouska.AppConfig
import java.lang.reflect.Method
import java.util.*

object BatteryOptimization {

    private val urls = mapOf(
        "huawei" to AppConfig.batteryOptimizationHuaweiLink,
        "asus" to AppConfig.batteryOptimizationAsusLink,
        "lenovo" to AppConfig.batteryOptimizationLenovoLink,
        "samsung" to AppConfig.batteryOptimizationSamsungLink,
        "sony" to AppConfig.batteryOptimizationSonyLink,
        "xiaomi" to AppConfig.batteryOptimizationXiaomiLink
    )

    fun isTutorialNeeded(): Boolean {
        return AppConfig.showBatteryOptimizationTutorial && getTutorialLink() != null
    }

    fun getTutorialLink(): String? {
        return urls[Build.MANUFACTURER.toLowerCase(Locale("cs"))]
    }

    @SuppressLint("PrivateApi")
    fun isMiUI(): Boolean {
        val c = Class.forName("android.os.SystemProperties")
        val get: Method = c.getMethod("get", String::class.java)
        return (get.invoke(c, "ro.miui.ui.version.code") as? String)?.run {
            isNotEmpty()
        } ?: false
    }
}