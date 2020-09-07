package cz.covid19cz.erouska.utils

import java.text.DecimalFormat
import java.text.NumberFormat

inline fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

object SignNumberFormat {
    @JvmStatic
    fun format(value: Number): String {
        return NumberFormat.getInstance().also { it.showSign() }.format(value)
    }
}

fun NumberFormat.showSign() {
    if (this is DecimalFormat) {
        this.positivePrefix = "+"
    }
}