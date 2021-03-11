package cz.covid19cz.erouska.ui.noverificationcode.event

import arch.event.LiveEvent

class NoVerificationCodeEvent(val command: Command) : LiveEvent() {

    enum class Command {
        WRITE_EMAIL
    }
}