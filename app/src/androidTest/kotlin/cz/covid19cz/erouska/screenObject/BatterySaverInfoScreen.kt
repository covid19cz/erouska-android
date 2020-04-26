package cz.covid19cz.erouska.screenObject

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.ManufacturerHelper
import cz.covid19cz.erouska.helpers.RETRY_TIMEOUT
import cz.covid19cz.erouska.helpers.checkMatchesString
import cz.covid19cz.erouska.helpers.click
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

object BatterySaverInfoScreen {
    private const val TITLE = "eRouška potřebuje běžet i když s ní právě nepracujete"

    fun finish() {

        // skip this screen for devices that does not require it
        if(!ManufacturerHelper.isBatteryTutorialNeeded()) return

        await().ignoreExceptions().atMost(RETRY_TIMEOUT, TimeUnit.SECONDS).untilAsserted {
            checkMatchesString(R.id.battery_opt_title, TITLE)
        }

        click(R.id.done_btn)
    }

}
