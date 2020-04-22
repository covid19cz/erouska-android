package cz.covid19cz.erouska.screenObject

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.DatabaseFiller.MEDIAN_RSSI
import cz.covid19cz.erouska.helpers.DatabaseFiller.TUID
import cz.covid19cz.erouska.helpers.RETRY_TIMEOUT
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

object MyDataScreen {

    fun areDataPresent() {

        checkDisplayed("1.1.1970")
        checkDisplayed("01:00")
        checkDisplayed("...${TUID.takeLast(6)}")
        checkDisplayed("$MEDIAN_RSSI dBm")
    }

    fun sendData() {

        click(R.id.enable_bluetooth_btn)
        click(R.id.confirm_button)
        await().ignoreExceptions().atMost(RETRY_TIMEOUT, TimeUnit.SECONDS).untilAsserted {
            checkDisplayed(R.id.success_title)
        }
        click(R.id.close_button)
    }
}