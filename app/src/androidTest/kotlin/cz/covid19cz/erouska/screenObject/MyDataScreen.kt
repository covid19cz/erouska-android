package cz.covid19cz.erouska.screenObject

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.DatabaseFiller.MEDIAN_RSSI
import cz.covid19cz.erouska.helpers.DatabaseFiller.TIMESTAMP_START
import cz.covid19cz.erouska.helpers.DatabaseFiller.TUID
import cz.covid19cz.erouska.helpers.RETRY_TIMEOUT
import cz.covid19cz.erouska.helpers.checkDisplayed
import cz.covid19cz.erouska.helpers.click
import org.awaitility.Awaitility.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object MyDataScreen {

    private val dateFormatter = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val date = Date(TIMESTAMP_START)

    fun areDataPresent() {

        checkDisplayed(dateFormatter.format(date))
        checkDisplayed(timeFormatter.format(date))
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