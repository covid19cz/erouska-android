package cz.covid19cz.erouska.ui.activation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivationVM(
    sharedPrefsRepository: SharedPrefsRepository,
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository
) : BaseVM() {

    private val mutableState = MutableLiveData<ActivationState>()
    val state = mutableState as LiveData<ActivationState>

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        auth.setLanguageCode(LocaleUtils.getSupportedLanguage())
        if (sharedPrefsRepository.isActivated()) {
            mutableState.postValue(ActivationFinished)
        }
    }

    fun activate() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableState.postValue(ActivationStart)
            try {
                firebaseFunctionsRepository.register()
                mutableState.postValue(ActivationFinished)
            } catch (e: Exception) {
                if(e is ApiException) {
                    publish(GmsApiErrorEvent(e.status))
                    return@launch
                }
                L.e(e)
                mutableState.postValue(ActivationFailed)
            }
        }
    }

    fun backPressed() {
        mutableState.postValue(ActivationInit)
    }

}
