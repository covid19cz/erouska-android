package cz.covid19cz.erouska.screens

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click

object A1Screen {

    fun startActivation() {
        click(R.id.welcome_continue_btn)
    }

    fun checkAllPartsDisplayed() {
        checkDisplayed(R.id.welcome_title)
        checkDisplayed(R.id.welcome_desc)
        checkDisplayed(R.id.welcome_help_btn)
        checkDisplayed(R.id.mzcr_icon)
        checkDisplayed(R.id.welcome_continue_btn)
    }

    fun goToHelp() {
        click(R.id.welcome_help_btn)
    }
}