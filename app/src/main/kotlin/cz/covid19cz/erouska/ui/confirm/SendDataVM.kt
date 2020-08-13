package cz.covid19cz.erouska.ui.confirm

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.confirm.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.confirm.event.SendDataInitState
import cz.covid19cz.erouska.ui.confirm.event.SendDataState
import cz.covid19cz.erouska.ui.confirm.event.SendDataSuccessState

class SendDataVM : BaseVM() {

    val state = MutableLiveData<SendDataState>()

    init {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        state.value = SendDataInitState
    }

    fun verifyAndConfirm(input: String?) {
        // Check if code is valid
        if (input == null || !isCodeValid(input)) {
            publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_INVALID))
            return
        }

        publish(SendDataCommandEvent(SendDataCommandEvent.Command.PROCESSING))

        // Try to send data
        sendData()
    }

    fun reset() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        state.value = SendDataInitState
    }

    fun sendData() {
        // TODO Try to send data
        // Check if code is expired
        // TODO Code expiration should be verified by BE, so it will probably be distinguished by response code/message (source: Jan Sticha)
        // TODO Just a mock expiration, delete after implementation of BE <-> AN communication
        val codeExpired = false
        if (codeExpired) {
            publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_EXPIRED))
            return
        }

        publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_SUCCESS))
        state.value = SendDataSuccessState
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 8 && code.isDigitsOnly()
    }

    fun debugCodeExpired() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_EXPIRED))
    }

    fun debugSendSuccess() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_SUCCESS))
    }

    fun debugInit() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        state.value = SendDataInitState
    }

    fun debugCodeInvalid() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_INVALID))
    }

}
