package cz.covid19cz.erouska.screenObject

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.typeText

object PhoneNumberScreen {
    const val PHONE_NUMBER = "731 000 000"

    fun typePhoneNumber() {
        checkDisplayed(R.id.login_desc)
        typeText(R.id.login_verif_phone_input, PHONE_NUMBER)
    }

    fun acceptWithAgreements() {
        click(R.id.login_checkbox)
    }

    fun continueToSMSVerify() {
        click(R.id.login_verif_activate_btn)
    }
}