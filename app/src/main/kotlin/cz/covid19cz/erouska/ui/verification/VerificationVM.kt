package cz.covid19cz.erouska.ui.verification

import androidx.core.text.isDigitsOnly
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.model.VerifyCodeResponse
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.error.entity.ErrorType
import cz.covid19cz.erouska.ui.verification.event.SendDataCommandEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class VerificationVM @ViewModelInject constructor(private val exposureNotificationRepo: ExposureNotificationsRepository, private val prefs : SharedPrefsRepository) :
    BaseVM() {

    val code = SafeMutableLiveData("")
    val error = MutableLiveData<Int>(null)
    val lastDataSentDate = MutableLiveData<String>(prefs.getLastDataSentDateString())
    val loading = SafeMutableLiveData(false)

    init {
        publish(SendDataCommandEvent(SendDataCommandEvent.Command.INIT))
        code.observeForever {
            if (error.value != null) {
                error.value = null
            }
        }
    }

    fun verifyAndConfirm() {
        if (!isCodeValid(code.value)) {
            error.value = R.string.send_data_code_invalid
            return
        }
        validate()
    }

    private fun validate() {
        if (prefs.isCodeValidated(code.value)) {
            navigate(VerificationFragmentDirections.actionNavVerificationToNavSymptomDate())
        } else {
            loading.value = true
            viewModelScope.launch {
                runCatching {
                    if (!exposureNotificationRepo.isEnabled()) {
                        exposureNotificationRepo.start()
                    }
                    exposureNotificationRepo.verifyCode(code.value)
                }.onSuccess {
                    loading.value = false
                    navigate(VerificationFragmentDirections.actionNavVerificationToNavSymptomDate())
                }.onFailure {
                    loading.value = false
                    handleSendDataErrors(it)
                }
            }
        }
    }

    private fun handleSendDataErrors(exception: Throwable) {
        when (exception) {
            is VerifyException -> {
                exception.code?.let {

                    val errorCodeMap: Map<String, ErrorType> = mutableMapOf(
                        VerifyCodeResponse.ERROR_CODE_EXPIRED_CODE to ErrorType.EXPIRED_OR_USED_CODE,
                        VerifyCodeResponse.ERROR_CODE_EXPIRED_USED_CODE to ErrorType.EXPIRED_OR_USED_CODE,
                        VerifyCodeResponse.ERROR_CODE_INVALID_CODE to ErrorType.INVALID_CODE,
                    )

                    navigate(
                        VerificationFragmentDirections.actionNavVerificationToNavError(
                            type = errorCodeMap.getOrElse(exception.code, { ErrorType.GENERAL_ERROR }),
                            errorCode = "${exception.message} ${exception.code}"
                        ))

                } ?: navigate(
                    VerificationFragmentDirections.actionNavVerificationToNavError(
                        ErrorType.NO_INTERNET))
            }
            is UnknownHostException -> {
                navigate(
                    VerificationFragmentDirections.actionNavVerificationToNavError(
                        ErrorType.NO_INTERNET))
            }
            else -> {
                L.e(exception)
                navigate(
                    VerificationFragmentDirections.actionNavVerificationToNavError(
                        ErrorType.NO_INTERNET))
            }
        }
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 8 && code.isDigitsOnly()
    }
}
