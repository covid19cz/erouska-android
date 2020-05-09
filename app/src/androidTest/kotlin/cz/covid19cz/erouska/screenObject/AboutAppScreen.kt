package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.matcher.ViewMatchers
import cz.covid19cz.erouska.helpers.verifyLink

object AboutAppScreen {

    fun verifyLinkedInLink() = verifyLink(ViewMatchers.withText("Komenda"), "https://www.linkedin.com/in/vojtech-komenda-71365b15/")
}