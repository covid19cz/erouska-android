package cz.covid19cz.erouska.ui.how.event

import arch.event.LiveEvent

class HowItWorksEvent(val command: Command) : LiveEvent() {

    enum class Command {
        WRITE_EMAIL,
        CLOSE
    }
}