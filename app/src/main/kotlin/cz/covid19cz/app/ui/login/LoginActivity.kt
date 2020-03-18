package cz.covid19cz.app.ui.login

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.google.firebase.auth.PhoneAuthProvider
import cz.covid19cz.app.R
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val vm: LoginVM by viewModel()

    private lateinit var views: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupListeners()
        vm.state.observe(this) {
            updateState(it)
        }
        views = listOf(vRegister, vSendCode, vProgress, vPhoneNumber, vError, vCode, phone_verif_image, login_title, login_desc, login_phone_verif, login_statement)

        vm.deviceRepository.data.observe(this, Observer {
            // Todo: Populate the recyclerView here
            it.forEach { device ->
                Log.d(LoginActivity::class.simpleName, device.toString())
            }
        })
    }

    private fun setupListeners() {
        vRegister.setOnClickListener {
            hideKeyboard(vRegister)
            verifyPhoneNumber()
        }
        vSendCode.setOnClickListener {
            hideKeyboard(vSendCode)
            vm.codeEntered(vCode.text.toString())
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            EnterPhoneNumber -> show(vPhoneNumber, vRegister)
            AutoVerificationProgress -> show(vProgress)
            EnterCode -> show(vCode, vSendCode)
            SigningProgress -> show(vProgress)
            is LoginError -> showError(state.exception)
            is SignedIn -> showSignedIn(state)
        }
    }

    private fun showSignedIn(user: SignedIn) {
        vError.text = "Přihlášeno.\n\nFUID: ${user.fuid}\n" +
                "BUID: ${user.buid}\nTel. č.: ${user.phoneNumber}"
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
            10, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            vm.verificationCallbacks
        )
    }

    private fun show(vararg views: View) {
        views.forEach {
            it.visibility = View.VISIBLE
        }
        this.views.subtract(views.toList()).forEach {
            it.visibility = View.GONE
        }
    }

    private fun hideKeyboard(view: View) {
        val im = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(view.windowToken, 0);
    }
}
