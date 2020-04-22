package cz.covid19cz.erouska.screenObject

import android.os.Build
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.clickUiAutomator

object BluetoothPermissionScreen {

    fun allowPermission() {
        checkDisplayed(R.id.bluetooth_onboard_title)
        click(R.id.enable_bluetooth_btn)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            clickUiAutomator("com.android.permissioncontroller:id/permission_allow_foreground_only_button")
        } else {
            clickUiAutomator("com.android.packageinstaller:id/permission_allow_button")
        }
    }
}