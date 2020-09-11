package cz.covid19cz.erouska.screens

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.RETRY_TIMEOUT
import cz.covid19cz.erouska.helpers.checkDisplayed
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

object B1Screen {

    fun checkActiveScreen() {
        await().ignoreExceptions().atMost(RETRY_TIMEOUT, TimeUnit.SECONDS).untilAsserted {
            checkDisplayed(R.id.app_running_image)
        }
        checkDisplayed(R.id.app_running_title)
        checkDisplayed(R.id.app_running_body)
        checkDisplayed(R.id.app_running_body_secondary)
        checkDisplayed(R.id.buttonStop)
    }
}