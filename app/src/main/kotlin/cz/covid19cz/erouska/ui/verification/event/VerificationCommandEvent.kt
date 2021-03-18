package cz.covid19cz.erouska.ui.verification.event

import arch.event.LiveEvent
import java.util.*

class VerificationCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        NAV_SYMPTOMS,
        NAV_NO_CODE
    }

}
