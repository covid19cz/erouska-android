package cz.covid19cz.app.ui.login

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import cz.covid19cz.app.db.ExpositionRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.utils.boolean
import cz.covid19cz.app.utils.sharedPrefs
import java.util.*

class LoginVM(val app: Application, val deviceRepository: ExpositionRepository) : BaseVM() {

    var userSignedIn by app.sharedPrefs().boolean()
    val data = deviceRepository.data
    val state = MutableLiveData<LoginState>(EnterPhoneNumber)
    val verificationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)
            state.postValue(LoginError(e))
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            this@LoginVM.verificationId = verificationId
            resendToken = token
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            Log.d(TAG, "onCodeAutoRetrievalTimeOut:$verificationId")
            this@LoginVM.verificationId = verificationId
            state.postValue(EnterCode)
        }
    }
    private val TAG = "Login"
    private lateinit var verificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val functions = Firebase.functions("europe-west2")

    init {
        auth.setLanguageCode("cs")
        if (auth.currentUser != null) {
            getUser()
        }
    }

    fun codeEntered(code: String) {
        state.postValue(SigningProgress)
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    registerDevice()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    state.postValue(LoginError(checkNotNull(task.exception)))
                    //if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    //}
                }
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
            getUser()
        }.addOnFailureListener {
            state.postValue(LoginError(it))
        }
    }

    private fun getUser() {
        val uid = checkNotNull(auth.uid)
        val phoneNumber = checkNotNull(auth.currentUser?.phoneNumber)
        db.collection("users").document(uid).get().addOnCompleteListener { response ->
            val snapshot = response.result;
            if (snapshot != null) {
                val buid = snapshot.data?.get("buid") as String
                state.postValue(SignedIn(uid, phoneNumber, buid))
            }
        }
    }
}
