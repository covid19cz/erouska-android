package cz.covid19cz.app.ext

import kotlin.math.pow

fun Int.rssiToDistance(): Double {
    return 10.0.pow(((-65 - this) / (10.0 * 2)))
}

fun Int.rssiToDistanceString(): String {
    return String.format("%.1f m", rssiToDistance())
}