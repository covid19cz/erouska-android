package cz.covid19cz.erouska.utils

import android.os.Build
import cz.covid19cz.erouska.AppConfig
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

    fun getTutorialMarkdown(): String? {
        return urls[Build.MANUFACTURER.toLowerCase(Locale("cs"))]
    }

}