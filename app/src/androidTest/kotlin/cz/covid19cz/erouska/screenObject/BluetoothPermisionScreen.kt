package cz.covid19cz.erouska.screenObject

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.clickUiAutomator

object BluetoothPermissionScreen {

    fun allowPermission() {
        checkDisplayed(R.id.bluetooth_onboard_title)
        click(R.id.enable_bluetooth_btn)
        clickUiAutomator("povolit")
    }
}