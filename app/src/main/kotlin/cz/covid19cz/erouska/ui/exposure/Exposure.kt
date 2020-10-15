package cz.covid19cz.erouska.ui.exposure

import cz.covid19cz.erouska.ext.daysSinceEpochToDateString

data class Exposure(
    val daysSinceEpoch: Int,
    val date: String = daysSinceEpoch.daysSinceEpochToDateString()
)
