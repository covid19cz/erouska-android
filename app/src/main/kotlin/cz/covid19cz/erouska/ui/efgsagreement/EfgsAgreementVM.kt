package cz.covid19cz.erouska.ui.efgsagreement

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.verification.NoKeysException
import cz.covid19cz.erouska.ui.verification.ReportExposureException
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class EfgsAgreementVM @ViewModelInject constructor(val prefs: SharedPrefsRepository, val exposureNotificationsRepo: ExposureNotificationsRepository) :
    BaseVM() {

    val traveller = prefs.isTraveller()
    val loading = SafeMutableLiveData(false)

    fun agree() {
        prefs.setConsentToFederation(true)
        publishKeys()
    }

    fun disagree() {
        prefs.setConsentToFederation(false)
        publishKeys()
    }

    fun publishKeys() {
        loading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepo.publishKeys()
            }.onSuccess {
                loading.value = false
                prefs.deletePublishKeysTemporaryData()
                prefs.setLastDataSentDate()
                navigate(EfgsAgreementFragmentDirections.actionNavEfgsAgreementToNavPublishSuccess(it > 1))
            }.onFailure {
                loading.value = false
                handleSendDataErrors(it)
            }
        }
    }

    private fun handleSendDataErrors(exception: Throwable) {
        when (exception) {
            is ApiException -> publish(GmsApiErrorEvent(exception))
            is NoKeysException -> navigate(EfgsAgreementFragmentDirections.actionNavEfgsAgreementToNavPublishSuccess(false))
            is ReportExposureException -> {
                //TODO: Integrate with Tomas's error screen
            }
            is UnknownHostException -> {
                //TODO: Integrate with Tomas's error screen
            }
            else -> {
                L.e(exception)
                //TODO: Integrate with Tomas's error screen
            }
        }
    }


}