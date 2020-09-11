package cz.covid19cz.erouska.screens

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.clickUiAutomatorByResourceId

object A2Screen {


    fun checkAllPartsDisplayed() {
        checkDisplayed(R.id.notifications_img)
        checkDisplayed(R.id.notifications_title)
        checkDisplayed(R.id.notifications_body_1)
        checkDisplayed(R.id.notifications_body_2)
        checkDisplayed(R.id.enable_btn)
    }

    fun turnOnNotifications() {
        click(R.id.enable_btn)
    }

    fun acceptCovidActivation() {
        clickUiAutomatorByResourceId("android:id/button1")
    }
}