package cz.covid19cz.erouska.ui.login

import android.os.Build
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.functions.ktx.functions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.AppConfig.FIREBASE_REGION
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.toText
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class LoginVM(
    private val sharedPrefsRepository: SharedPrefsRepository
) : BaseVM() {

    private val mutableState = MutableLiveData<LoginState>(EnterPhoneNumber(false))
    val state = mutableState as LiveData<LoginState>
    val verificationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            val smsCode = credential.smsCode
            if (smsCode == null) {
                signInWithPhoneAuthCredential(credential)
            } else {
                autoVerifiedCredential = credential
                mutableState.postValue(CodeReadAutomatically(smsCode))
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            L.d("onVerificationFailed")
            handleError(e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later
            this@LoginVM.verificationId = verificationId
            resendToken = token
            mutableState.postValue(EnterCode(false, phoneNumber))
            smsCountDownTimer.cancel()
            smsCountDownTimer.start()

        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            this@LoginVM.verificationId = verificationId
        }
    }
    private var autoVerifiedCredential: PhoneAuthCredential? = null
    private lateinit var verificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val functions = Firebase.functions(FIREBASE_REGION)
    private lateinit var phoneNumber: String
    private var smsCountDownTimer: CountDownTimer = object: CountDownTimer(AppConfig.smsErrorTimeoutSeconds * 1000, 1000) {
        override fun onFinish() {
            mutableState.postValue(LoginError(R.string.login_session_expired.toText()))
        }

        override fun onTick(millisUntilFinished: Long) {
            val df = SimpleDateFormat("mm:ss")
            remainingTime.postValue(df.format(millisUntilFinished))
        }

    }

    val remainingTime = MutableLiveData<String>("")

    init {
        auth.setLanguageCode("cs")
        if (auth.currentUser != null) {
            if (sharedPrefsRepository.getDeviceBuid() == null) {
                registerDevice()
            } else {
                getUser()
            }
        }
    }

    fun phoneNumberEntered(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        if (phoneNumber.length >= 8) {
            mutableState.postValue(StartVerification)
        } else {
            mutableState.postValue(EnterPhoneNumber(true))
        }
    }

    fun codeEntered(code: String) {
        if (code.trim().length != 6) {
            mutableState.postValue(EnterCode(true, phoneNumber))
        } else {
            mutableState.postValue(SigningProgress)
            val credential =
                autoVerifiedCredential ?: PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    override fun onCleared() {
        smsCountDownTimer.cancel()
        super.onCleared()
    }

    fun backPressed() {
        smsCountDownTimer.cancel()
        mutableState.postValue(EnterPhoneNumber(false))
    }

    fun getTermsAndConditions() = AppConfig.termsAndConditionsLink

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    registerDevice()
                } else {
                    // Sign in failed, display a message and update the UI
                    L.d("signInWithCredential:failure")
                    task.exception?.let { handleError(it) }
                }
            }
    }

    private fun handleError(e: Exception) {
        L.e(e)
        if (e is FirebaseAuthInvalidCredentialsException) {
            L.d("Error code: ${e.errorCode}")
        }
        if (e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_INVALID_PHONE_NUMBER") {
            mutableState.postValue(EnterPhoneNumber(true))
        } else if (e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_INVALID_VERIFICATION_CODE") {
            mutableState.postValue(EnterCode(true, phoneNumber))
        } else if (e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_TOO_MANY_REQUESTS") {
            mutableState.postValue(LoginError(R.string.login_too_many_attempts_error.toText()))
        } else if (e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_SESSION_EXPIRED") {
            mutableState.postValue(LoginError(R.string.login_session_expired.toText()))
        } else if (e is FirebaseNetworkException) {
            mutableState.postValue(LoginError(R.string.login_network_error.toText()))
        } else {
            mutableState.postValue(LoginError(e.message?.toText()))
        }
    }

    private fun registerDevice() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { handleError(it) }
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val pushToken = task.result?.token
                val data = hashMapOf(
                    "platform" to "android",
                    "platformVersion" to Build.VERSION.RELEASE,
                    "manufacturer" to Build.MANUFACTURER,
                    "model" to Build.MODEL,
                    "locale" to Locale.getDefault().toString(),
                    "pushRegistrationToken" to pushToken
                )
                functions.getHttpsCallable("registerBuid").call(data).addOnSuccessListener {
                    val buid = JSONObject(it.data.toString()).getString("buid")
                    sharedPrefsRepository.putDeviceBuid(buid)
                    getUser()
                }.addOnFailureListener {
                    handleError(it)
                }
            })
    }

    private fun getUser() {
        val fuid = checkNotNull(auth.uid)
        val phoneNumber = checkNotNull(auth.currentUser?.phoneNumber)
        val buid = checkNotNull(sharedPrefsRepository.getDeviceBuid())
        mutableState.postValue(SignedIn(fuid, phoneNumber, buid))
    }
}
