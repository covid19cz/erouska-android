package cz.covid19cz.erouska.ui.confirm

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.confirm.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.confirm.event.SendDataInitState
import cz.covid19cz.erouska.ui.confirm.event.SendDataState
import cz.covid19cz.erouska.ui.confirm.event.SendDataSuccessState
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class SendDataVM(val exposureNotificationRepo : ExposureNotificationsRepository) : BaseVM() {

    val state = MutableLiveData<SendDataState>()
    val code = SafeMutableLiveData("")

    init {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        state.value = SendDataInitState
    }

    fun verifyAndConfirm() {
        // Check if code is valid
        if (!isCodeValid(code.value)) {
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

    private fun sendData() {
        // TODO Code expiration should be verified by BE, so it will probably be distinguished by response code/message (source: Jan Sticha)

        viewModelScope.launch {
            runCatching {
                return@runCatching if (BuildConfig.FLAVOR == "dev" && code.value == "00000000"){
                    exposureNotificationRepo.reportExposureWithoutVerification()
                } else {
                    exposureNotificationRepo.reportExposureWithVerification(code.value)
                }
            }.onSuccess {
                state.value = SendDataSuccessState
                publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_SUCCESS))
            }.onFailure {
                when(it){
                    is ApiException -> publish(GmsApiErrorEvent(it.status))
                    is VerifyException -> publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_FAILURE))
                    is ReportExposureException -> publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_FAILURE))
                    else -> publish(SendDataCommandEvent(SendDataCommandEvent.Command.DATA_SEND_FAILURE))
                }
                L.e(it)
            }
        }
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 8 && code.isDigitsOnly()
    }
}
