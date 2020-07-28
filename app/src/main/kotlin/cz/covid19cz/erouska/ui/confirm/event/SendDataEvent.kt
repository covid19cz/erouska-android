package cz.covid19cz.erouska.ui.confirm.event

import arch.event.LiveEvent

class SendDataCommandEvent(val command: Command) : LiveEvent() {

    enum class Command {
        INIT,
        PROCESSING,
        CODE_VALID,
        CODE_INVALID,
        CODE_EXPIRED,
        DATA_SEND_FAILURE,
        DATA_SEND_SUCCESS
    }

}

sealed class SendDataState
object SendDataInitState : SendDataState()
object SendDataFailedState : SendDataState()
object SendDataSuccessState : SendDataState()