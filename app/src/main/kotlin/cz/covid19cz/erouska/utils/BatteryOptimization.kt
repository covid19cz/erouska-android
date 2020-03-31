package cz.covid19cz.erouska.utils

import android.os.Build
import cz.covid19cz.erouska.AppConfig
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
}