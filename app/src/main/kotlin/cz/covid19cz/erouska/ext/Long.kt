package cz.covid19cz.erouska.ext

import java.text.SimpleDateFormat
import java.util.*

fun Long.timestampToDate(): String {
    return SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(this))
}

fun Long.timestampToTime(): String {
    return SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(this))
}

fun Long.timestampToDateTime(): String {
    return SimpleDateFormat("d.M.yyyy H:mm", Locale.getDefault()).format(Date(this))
}