package cz.covid19cz.app.utils

import cz.covid19cz.app.R

object ColorUtils {

    fun rssiToColor(rssi : Int) : Int{
        return when{
            rssi > -60 -> R.color.exposition_level_8
            rssi > -65 -> R.color.exposition_level_7
            rssi > -70 -> R.color.exposition_level_6
            rssi > -75 -> R.color.exposition_level_5
            rssi > -80 -> R.color.exposition_level_4
            rssi > -85 -> R.color.exposition_level_3
            rssi > -90 -> R.color.exposition_level_2
            else -> R.color.exposition_level_1
        }
    }

    fun rssiToColorBackground(rssi : Int) : Int{
        return when{
            rssi > -60 -> R.color.exposition_level_8_bg
            rssi > -65 -> R.color.exposition_level_7_bg
            rssi > -70 -> R.color.exposition_level_6_bg
            rssi > -75 -> R.color.exposition_level_5_bg
            rssi > -80 -> R.color.exposition_level_4_bg
            rssi > -85 -> R.color.exposition_level_3_bg
            rssi > -90 -> R.color.exposition_level_2_bg
            else -> R.color.exposition_level_1_bg

        }
    }
}