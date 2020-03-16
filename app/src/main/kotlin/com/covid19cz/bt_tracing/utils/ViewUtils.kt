package com.covid19cz.bt_tracing.utils

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = getResources().getDisplayMetrics()
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT.toFloat())).roundToInt()
}
