package cz.covid19cz.erouska.ui.activation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.exposurenotifications.Notifications
import cz.covid19cz.erouska.ext.isNetworkAvailable
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivationVM @Inject constructor(
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository,
    @ApplicationContext
    private val context: Context,
    private val notifications: Notifications
) : BaseVM() {

    private val mutableState = MutableLiveData<ActivationState>()
    val state = mutableState as LiveData<ActivationState>

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        auth.setLanguageCode(LocaleUtils.getSupportedLanguage())
        if (auth.currentUser != null) {
            mutableState.postValue(ActivationFinished)
        }
    }

    fun activate() {
        if (context.isNetworkAvailable()) {
            viewModelScope.launch(Dispatchers.IO) {
                mutableState.postValue(ActivationStart)
                try {
                    firebaseFunctionsRepository.register(notifications.getCurrentPushToken())
                    mutableState.postValue(ActivationFinished)
                } catch (e: Exception) {
                    if (e is ApiException) {
                        publish(GmsApiErrorEvent(e))
                        return@launch
                    }
                    L.e(e)
                    mutableState.postValue(ActivationFailed(e.message))
                }
            }
        } else {
            mutableState.postValue(NoInternet)
        }
    }

    fun backPressed() {
        mutableState.postValue(ActivationInit)
    }

}
