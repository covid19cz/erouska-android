package cz.covid19cz.erouska.ui.recentexposures.entity

import java.text.SimpleDateFormat
import java.util.*

class RecentExposureGroupHeaderItem(val timestamp : Long) {

    fun getDateString() : String{
        return SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun getTimeString() : String{
        return SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(timestamp))
    }
}