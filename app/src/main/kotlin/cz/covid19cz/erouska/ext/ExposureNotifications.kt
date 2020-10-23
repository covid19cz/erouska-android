package cz.covid19cz.erouska.ext

import java.text.SimpleDateFormat
import java.util.*

fun Int.daysSinceEpochToDateString(): String{
        val formatter = SimpleDateFormat("d. M. yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (toLong() * 24 * 60 * 60 * 1000)
        }
        return formatter.format(dateTime.time)
}