package cz.covid19cz.erouska.screenObject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.ManufacturerHelper
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

object BatterySaverInfoScreen {
    const val TITLE = "eRouška potřebuje běžet i když s ní právě nepracujete"

    fun finish() {

        // skip this screen for devices that does not require it
        if(!ManufacturerHelper.isBatteryTutorialNeeded()) return

        await().ignoreExceptions().atMost(15, TimeUnit.SECONDS).untilAsserted {
            onView(withId(R.id.battery_opt_title)).checkMatchesString(TITLE)
        }

        onView(withId(R.id.done_btn)).click()
    }

}
