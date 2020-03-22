package cz.covid19cz.app.ui.login

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import cz.covid19cz.app.R
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.utils.toText
import org.json.JSONObject
import java.util.*

class LoginVM(
    val app: Application,
    private val deviceRepository: DatabaseRepository,
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
            Log.w(TAG, "onVerificationFailed", e)
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
            mutableState.postValue(EnterCode(false))
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            this@LoginVM.verificationId = verificationId
        }
    }
    private var autoVerifiedCredential: PhoneAuthCredential? = null
    private val TAG = "Login"
    private lateinit var verificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val functions = Firebase.functions("europe-west2")

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
        if (phoneNumber.length >= 8) {
            mutableState.postValue(StartVerification)
        } else {
            mutableState.postValue(EnterPhoneNumber(true))
        }
    }

    fun codeEntered(code: String) {
        if (code.trim().length != 6) {
            mutableState.postValue(EnterCode(true))
        } else {
            mutableState.postValue(SigningProgress)
            val credential =
                autoVerifiedCredential ?: PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    fun backPressed() {
        mutableState.postValue(EnterPhoneNumber(false))
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    registerDevice()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    task.exception?.let { handleError(it) }
                }
            }
    }

    private fun handleError(e: Exception) {
        if (e is FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "Error code: ${e.errorCode}")
        }
        if (e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_INVALID_PHONE_NUMBER") {
            mutableState.postValue(EnterPhoneNumber(true))
        } else if (e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_INVALID_VERIFICATION_CODE") {
            mutableState.postValue(EnterCode(true))
        } else if (e is FirebaseNetworkException) {
            mutableState.postValue(LoginError(R.string.login_network_error.toText()))
        } else {
            mutableState.postValue(LoginError(e.message?.toText()))
        }
    }

    private fun registerDevice() {
        val data = hashMapOf(
            "platform" to "android",
            "platformVersion" to Build.VERSION.RELEASE,
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "locale" to Locale.getDefault().toString()
        )
        functions.getHttpsCallable("createUser").call(data).addOnSuccessListener {
            val buid = JSONObject(it.data.toString()).getString("buid")
            sharedPrefsRepository.putDeviceBuid(buid)
            getUser()
        }.addOnFailureListener {
            handleError(it)
        }
    }

    private fun getUser() {
        val fuid = checkNotNull(auth.uid)
        val phoneNumber = checkNotNull(auth.currentUser?.phoneNumber)
        val buid = checkNotNull(sharedPrefsRepository.getDeviceBuid())
        mutableState.postValue(SignedIn(fuid, phoneNumber, buid))
    }
}
