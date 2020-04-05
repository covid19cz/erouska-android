package cz.covid19cz.erouska.utils

import android.annotation.SuppressLint
import android.os.Build
import cz.covid19cz.erouska.AppConfig
import java.lang.reflect.Method
import java.util.*

object BatteryOptimization {

    private val urls = mapOf(
        "huawei" to AppConfig.batteryOptimizationHuaweiMarkdown,
        "asus" to AppConfig.batteryOptimizationAsusMarkdown,
        "lenovo" to AppConfig.batteryOptimizationLenovoMarkdown,
        "samsung" to AppConfig.batteryOptimizationSamsungMarkdown,
        "sony" to AppConfig.batteryOptimizationSonyMarkdown,
        "xiaomi" to AppConfig.batteryOptimizationXiaomiMarkdown
    )

    fun isTutorialNeeded(): Boolean {
        return AppConfig.showBatteryOptimizationTutorial && getTutorialMarkdown() != null
    }

    fun getTutorialMarkdown(): String? {
        return urls[Build.MANUFACTURER.toLowerCase(Locale("cs"))]?.replace("\\n", "\n")
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