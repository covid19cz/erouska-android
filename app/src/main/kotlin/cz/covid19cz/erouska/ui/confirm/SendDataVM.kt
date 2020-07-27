package cz.covid19cz.erouska.ui.confirm

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.confirm.event.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SendDataVM : BaseVM() {

    val mutableState = MutableLiveData<SendDataState>()

    init {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        mutableState.value = SendDataInitState
    }

    fun verifyAndConfirm(input: String?) {
        // Check if code is valid
        if (input == null || !isCodeValid(input)) {
            publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_INVALID))
            return
        }

        publish(SendDataCommandEvent(SendDataCommandEvent.Command.PROCESS))

        // Try to send data
        sendData()
    }

    fun initState() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        mutableState.value = SendDataInitState
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
        mutableState.value = SendDataSuccessState
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 8 && code.isDigitsOnly()
    }

}
