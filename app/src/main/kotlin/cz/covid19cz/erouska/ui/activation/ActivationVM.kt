package cz.covid19cz.erouska.ui.activation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.user.ActivationRepository
import cz.covid19cz.erouska.user.ActivationResponse
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.LocaleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ActivationVM(
    private val activationRepository: ActivationRepository,
    private val sharedPrefsRepository: SharedPrefsRepository,
    private val deviceInfo: DeviceInfo
) : BaseVM() {

    private val mutableState = MutableLiveData<ActivationState>()
    val state = mutableState as LiveData<ActivationState>

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        auth.setLanguageCode(LocaleUtils.getSupportedLanguage())
        // TODO Handle situation when user already has eHRID -> is activated
//        if (Auth.isSignedIn() && Auth.isPhoneNumberVerified()) {
//            mutableState.postValue(SignedIn)
//        }
    }

    fun activate() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableState.postValue(ActivationStart)
            when (activationRepository.activate()) {
                ActivationResponse.SUCCESS -> mutableState.postValue(ActivationFinished)
                else -> mutableState.postValue(ActivationFailed)
            }
        }
    }

    fun backPressed() {
        mutableState.postValue(ActivationInit)
    }

}
