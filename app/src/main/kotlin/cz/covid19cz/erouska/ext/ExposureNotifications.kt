package cz.covid19cz.erouska.ext

import java.text.SimpleDateFormat
import java.util.*

fun Int.daysSinceEpochToDateString(pattern: String = "d. M. yyyy"): String{
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (toLong() * 24 * 60 * 60 * 1000)
        }
        return formatter.format(dateTime.time)
}