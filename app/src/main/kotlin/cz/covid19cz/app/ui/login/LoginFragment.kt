package cz.covid19cz.app.ui.login

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import com.google.firebase.auth.PhoneAuthProvider
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentLoginBinding
import cz.covid19cz.app.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginFragment :
    BaseFragment<FragmentLoginBinding, LoginVM>(R.layout.fragment_login, LoginVM::class) {

    private lateinit var views: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) {
            updateState(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views = listOf(
            vRegister,
            vSendCode,
            vProgress,
            vPhoneNumber,
            vError,
            vCode,
            login_verif_image,
            login_title,
            login_desc,
            login_phone_verif,
            login_statement
        )



        setupListeners()

        setToolbarTitle(R.string.login_toolbar_title)
        enableUpInToolbar(true)
    }

    private fun setupListeners() {
        vRegister.setOnClickListener {
            hideKeyboard(vRegister)

            if (vPhoneNumber.text?.trim()?.isNotEmpty() == true) {
                login_phone_verif.isErrorEnabled = false
                verifyPhoneNumber()
            } else {
                login_phone_verif.isErrorEnabled = true
                login_phone_verif.error = getString(R.string.login_phone_input_error)
            }
        }
        vSendCode.setOnClickListener {
            hideKeyboard(vSendCode)
            viewModel.codeEntered(vCode.text.toString())
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            EnterPhoneNumber -> show(
                login_verif_image,
                login_title,
                login_desc,
                login_phone_verif,
                vPhoneNumber,
                login_statement,
                vRegister
            )
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
        viewModel.userSignedIn = true
        waitAndOpenSandbox(2000)
    }

    private fun waitAndOpenSandbox(millisToWait: Long) {
        val timer = object : CountDownTimer(millisToWait, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //stub
            }

            override fun onFinish() {
                if (isAdded) {
                    navigate(
                        R.id.action_nav_login_to_nav_sandbox, null,
                        Builder()
                            .setPopUpTo(
                                R.id.nav_graph,
                                true
                            ).build()
                    )
                }
            }
        }
        timer.start()
    }

    private fun showError(exception: Exception) {
        vError.text = exception.message
        show(vError)
    }

    private fun verifyPhoneNumber() {
        val phoneNumber = vPhoneNumber.text.toString()
        viewModel.state.postValue(AutoVerificationProgress)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            10, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            requireActivity(), // Activity (for callback binding)
            viewModel.verificationCallbacks
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
        val im = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(view.windowToken, 0);
    }
}
