package cz.covid19cz.erouska.utils

import android.view.View
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show

fun View.showOrHide(show: Boolean) {
    if (show) {
        show()
    } else {
        hide()
    }
}