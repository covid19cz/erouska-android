package cz.covid19cz.erouska.screens

import androidx.test.espresso.matcher.ViewMatchers.withId
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import cz.covid19cz.erouska.helpers.verifyLink

object A3Screen {

    fun checkAllPartsDisplayed() {
        checkDisplayed(R.id.img_privacy)
        checkDisplayed(R.id.privacy_header)
        checkDisplayed(R.id.privacy_body_1)
        checkDisplayed(R.id.privacy_body_2)
        checkDisplayed(R.id.activate_btn)
    }

    fun checkTermsOfUseLink() {
        verifyLink(withId(R.id.privacy_body_2), "https://www.erouska.cz", "podmínkách používání")
    }

    fun finishActivation() {
        click(R.id.activate_btn)
    }
}