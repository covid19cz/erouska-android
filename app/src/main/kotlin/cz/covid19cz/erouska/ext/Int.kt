package cz.covid19cz.erouska.ext

import kotlin.math.pow

fun Int.rssiToDistance(): Double {
    return 10.0.pow(((-65 - this) / (10.0 * 2)))
}

fun Int.rssiToDistanceString(): String {
    return String.format("%.1f m", rssiToDistance())
}

fun Int.daysToMilis() : Long{
    return this * 86400000L
}

fun Int.hoursToMilis() : Long{
    return this * 3600000L
}

fun Int.minutesToMilis() : Long{
    return this * 60000L
}

fun Int.secondsToMilis() : Long{
    return this * 1000L
}