package cz.covid19cz.erouska.ui.recentexposures.entity

import cz.covid19cz.erouska.ext.timestampToDate
import cz.covid19cz.erouska.ext.timestampToTime

class RecentExposureGroupHeaderItem(val timestamp : Long) {

    fun getDateString() : String{
        return timestamp.timestampToDate()
    }

    fun getTimeString() : String{
        return timestamp.timestampToTime()
    }
}