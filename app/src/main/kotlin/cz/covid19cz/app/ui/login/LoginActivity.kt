package cz.covid19cz.app.ui.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import cz.covid19cz.app.R
import cz.covid19cz.app.ext.getViewModel
import cz.covid19cz.app.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity : BaseActivity() {

    private lateinit var vm: LoginViewModel
    private lateinit var views : List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupListeners()
        vm = getViewModel()
        vm.state.observe(this) {
            updateState(it)
        }
        views = listOf(vRegister, vSendCode, vProgress, vPhoneNumber, vError, vCode)
    }

    private fun setupListeners() {
        vRegister.setOnClickListener {
            verifyPhoneNumber()
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            EnterPhoneNumber -> show(vPhoneNumber, vRegister)
            AutoVerificationProgress -> show(vProgress)
            EnterCode -> show(vCode, vSendCode)
            SigningProgress -> show(vProgress)
            is LoginError -> showError(state.exception)
            is SignedIn -> showSignedIn(state.user)
        }
    }

    private fun showSignedIn(user: FirebaseUser?) {
        vError.text = "Signed in. User: ${user?.toString()}"
        show(vError)
    }

    private fun showError(exception: Exception) {
        vError.text = exception.message
        show(vError)
    }

    private fun verifyPhoneNumber() {
        val phoneNumber = vPhoneNumber.text.toString()
        vm.state.postValue(AutoVerificationProgress)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            30, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            vm.verificationCallbacks) // OnVerificationStateChangedCallbacks
    }

    private fun show(vararg views: View) {
        views.forEach {
            it.visibility = View.VISIBLE
        }
        this.views.subtract(views.toList()).forEach {
            it.visibility = View.GONE
        }
    }
}
