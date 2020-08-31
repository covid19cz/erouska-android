package cz.covid19cz.erouska.ui.update.playservices.event

import arch.event.LiveEvent

class UpdatePlayServicesEvent(val command: Command) : LiveEvent() {

    enum class Command {
        PLAY_STORE
    }

}