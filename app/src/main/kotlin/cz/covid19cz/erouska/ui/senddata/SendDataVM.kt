package cz.covid19cz.erouska.ui.senddata

import androidx.core.text.isDigitsOnly
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.model.VerifyCodeResponse
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.senddata.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.senddata.event.SendDataInitState
import cz.covid19cz.erouska.ui.senddata.event.SendDataState
import cz.covid19cz.erouska.ui.senddata.event.SendDataSuccessState
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class SendDataVM @ViewModelInject constructor(private val exposureNotificationRepo: ExposureNotificationsRepository) :
    BaseVM() {

    val state = MutableLiveData<SendDataState>()
    val code = SafeMutableLiveData("")

    init {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        state.value = SendDataInitState
    }

    fun verifyAndConfirm() {
        if (!isCodeValid(code.value)) {
            publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_INVALID))
            return
        }
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.PROCESSING))
        sendData()
    }

    fun reset() {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        state.value = SendDataInitState
    }

    private fun sendData() {
        viewModelScope.launch {
            runCatching {
                if (!exposureNotificationRepo.isEnabled()) {
                    exposureNotificationRepo.start()
                }
                exposureNotificationRepo.reportExposureWithVerification(code.value)
            }.onSuccess {
                state.value = SendDataSuccessState
                publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_SUCCESS))
            }.onFailure {
                handleSendDataErrors(it)
            }
        }
    }

    private fun handleSendDataErrors(exception: Throwable) {
        when (exception) {
            is ApiException -> publish(GmsApiErrorEvent(exception))
            is NotEnoughKeysException -> publish(SendDataCommandEvent(SendDataCommandEvent.Command.NOT_ENOUGH_KEYS))
            is VerifyException -> {
                when (exception.code) {
                    VerifyCodeResponse.ERROR_CODE_EXPIRED_CODE -> {
                        publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_EXPIRED))
                    }
                    VerifyCodeResponse.ERROR_CODE_INVALID_CODE -> {
                        publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_INVALID))
                    }
                    VerifyCodeResponse.ERROR_CODE_EXPIRED_USED_CODE -> {
                        publish(SendDataCommandEvent(SendDataCommandEvent.Command.CODE_EXPIRED_OR_USED))
                    }
                    else -> {
                        L.e(exception)
                        publish(
                            SendDataCommandEvent(
                                SendDataCommandEvent.Command.DATA_SEND_FAILURE,
                                exception.message + " (${exception.code})"
                            )
                        )
                    }
                }
            }
            is ReportExposureException -> publish(
                SendDataCommandEvent(
                    SendDataCommandEvent.Command.DATA_SEND_FAILURE,
                    exception.error + " (${exception.code})"
                )
            )
            is UnknownHostException -> publish(SendDataCommandEvent(SendDataCommandEvent.Command.NO_INTERNET))
            else -> {
                L.e(exception)
                publish(
                    SendDataCommandEvent(
                        SendDataCommandEvent.Command.DATA_SEND_FAILURE,
                        exception.message
                    )
                )
            }
        }
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 8 && code.isDigitsOnly()
    }
}
