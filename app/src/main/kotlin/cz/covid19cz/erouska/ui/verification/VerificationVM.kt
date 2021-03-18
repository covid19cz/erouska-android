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
import cz.covid19cz.erouska.ui.verification.event.VerificationCommandEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class VerificationVM @ViewModelInject constructor(
    private val exposureNotificationRepo: ExposureNotificationsRepository,
    private val prefs: SharedPrefsRepository
) :
    BaseVM() {

    val code = SafeMutableLiveData("")
    val error = MutableLiveData<Int>(null)
    val lastDataSentDate = MutableLiveData<String>(prefs.getLastDataSentDateString())
    val loading = SafeMutableLiveData(false)

    init {
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

    fun navigateToVerificationCode() {
        publish(VerificationCommandEvent(VerificationCommandEvent.Command.NAV_NO_CODE))
    }

    private fun validate() {
        if (prefs.isCodeValidated(code.value)) {
            navigateToSymptomsScreen()
        } else {
            startLoading()
            viewModelScope.launch {
                runCatching {
                    exposureNotificationRepo.verifyCode(code.value)
                }.onSuccess {
                    stopLoading()
                    navigateToSymptomsScreen()
                }.onFailure {
                    stopLoading()
                    handleSendDataErrors(it)
                }
            }
        }
    }

    private fun startLoading() {
        loading.value = true
    }

    private fun stopLoading() {
        loading.value = false
    }

    private fun handleSendDataErrors(exception: Throwable) {
        val errorAndCode: Pair<ErrorType, String> = when (exception) {
            is VerifyException -> mapExceptionToErrorType(exception)
            is UnknownHostException -> createNoInternetErrorType()
            else -> {
                L.e(exception)
                createNoInternetErrorType()
            }
        }

        showError(errorAndCode.first, errorAndCode.second)
    }


    private fun mapExceptionToErrorType(exception: VerifyException): Pair<ErrorType, String> {
        if (exception.code == null) {
            return createNoInternetErrorType()
        }
        val errorCodeMap: Map<String, ErrorType> = mutableMapOf(
            VerifyCodeResponse.ERROR_CODE_EXPIRED_CODE to ErrorType.EXPIRED_OR_USED_CODE,
            VerifyCodeResponse.ERROR_CODE_EXPIRED_USED_CODE to ErrorType.EXPIRED_OR_USED_CODE,
            VerifyCodeResponse.ERROR_CODE_INVALID_CODE to ErrorType.INVALID_CODE,
        )
        val type = errorCodeMap.getOrElse(exception.code, { ErrorType.GENERAL_ERROR })
        val message = "${exception.message} ${exception.code}"
        return Pair(type, message)
    }

    private fun createNoInternetErrorType() = Pair(ErrorType.NO_INTERNET, "")

    private fun showError(errorType: ErrorType, errorMessage: String) {
        navigate(
            VerificationFragmentDirections.actionNavVerificationToNavError(
                type = errorType,
                errorCode = errorMessage
            )
        )
    }

    private fun navigateToSymptomsScreen() {
        publish(VerificationCommandEvent(VerificationCommandEvent.Command.NAV_SYMPTOMS))
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 8 && code.isDigitsOnly()
    }
}
