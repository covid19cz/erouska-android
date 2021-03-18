package cz.covid19cz.erouska.ui.symptomdate.event

import arch.event.LiveEvent
import java.util.*

class SymptomDateCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        NAV_TRAVELLER,
        NAV_EFGS_AGREEMENT
    }

}
