package cz.covid19cz.erouska.ui.help.data

import androidx.annotation.DrawableRes

interface IconifiedCategory {

    @DrawableRes
    fun getIconDrawable(): Int?

}