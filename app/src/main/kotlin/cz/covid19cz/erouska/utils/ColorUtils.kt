package cz.covid19cz.erouska.utils

import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R

object ColorUtils {

    private val LEVEL_8 = AppConfig.criticalExpositionRssi + 15
    private val LEVEL_7 = LEVEL_8 - 5
    private val LEVEL_6 = LEVEL_7 - 5
    private val LEVEL_5 = LEVEL_6 - 5 // Level 5 is criticalRss value from remote config
    private val LEVEL_4 = LEVEL_5 - 2
    private val LEVEL_3 = LEVEL_4 - 2
    private val LEVEL_2 = LEVEL_3 - 2

    fun rssiToColor(rssi : Int) : Int{
        return when{
            rssi >= LEVEL_8 -> R.color.exposition_level_8
            rssi >= LEVEL_7 -> R.color.exposition_level_7
            rssi >= LEVEL_6 -> R.color.exposition_level_6
            rssi >= LEVEL_5 -> R.color.exposition_level_5
            rssi >= LEVEL_4 -> R.color.exposition_level_4
            rssi >= LEVEL_3 -> R.color.exposition_level_3
            rssi >= LEVEL_2 -> R.color.exposition_level_2
            else -> R.color.exposition_level_1
        }
    }

    fun rssiToColorBackground(rssi : Int) : Int{
        return when{
            rssi >= LEVEL_8 -> R.color.exposition_level_8_bg
            rssi >= LEVEL_7 -> R.color.exposition_level_7_bg
            rssi >= LEVEL_6 -> R.color.exposition_level_6_bg
            rssi >= LEVEL_5 -> R.color.exposition_level_5_bg
            rssi >= LEVEL_4 -> R.color.exposition_level_4_bg
            rssi >= LEVEL_3 -> R.color.exposition_level_3_bg
            rssi >= LEVEL_2 -> R.color.exposition_level_2_bg
            else -> R.color.exposition_level_1_bg

        }
    }
}